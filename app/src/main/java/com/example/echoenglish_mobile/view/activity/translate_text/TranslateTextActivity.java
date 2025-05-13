package com.example.echoenglish_mobile.view.activity.translate_text;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateRequest;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateResponse;

import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TranslateTextActivity extends AppCompatActivity {

    private static final String TAG = "TranslateTextActivity";
    private static final String LOADING_DIALOG_TAG = "LoadingDialog";

    private EditText editTextSource;
    private RadioGroup radioGroupDirection;
    private RadioButton radioEngToVie;
    private RadioButton radioVieToEng;
    private Button buttonTranslate;
    private Button buttonCaptureImage;
    private ProgressBar progressBar;
    private TextView textViewResult;
    private ImageButton buttonCopyResult;
    private ImageView backButton;
    private TextView headerTitle;
    private ScrollView contentScrollView;

    private ApiService apiService;
    private TextRecognizer textRecognizer;
    private Uri cameraImageUri;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    private int loadingApiCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_text);

        backButton = findViewById(R.id.backButton);
        headerTitle = findViewById(R.id.headerTitle);
        contentScrollView = findViewById(R.id.contentScrollView);

        editTextSource = findViewById(R.id.editTextSource);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioEngToVie = findViewById(R.id.radioEngToVie);
        radioVieToEng = findViewById(R.id.radioVieToEng);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        progressBar = findViewById(R.id.progressBar);
        textViewResult = findViewById(R.id.textViewResult);
        buttonCopyResult = findViewById(R.id.buttonCopyResult);

        apiService = ApiClient.getApiService();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        initializeLaunchers();

        headerTitle.setText("Translate Text");


        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        buttonTranslate.setOnClickListener(v -> {
            String textToTranslate = editTextSource.getText().toString().trim();
            if (!textToTranslate.isEmpty()) {
                callTranslateApi(textToTranslate);
            } else {
                Toast.makeText(this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCaptureImage.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

        buttonCopyResult.setOnClickListener(v -> {
            copyTextToClipboard(textViewResult.getText().toString());
        });
    }

    private void initializeLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted.");
                        openCamera();
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Camera permission is required to capture images", Toast.LENGTH_LONG).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Camera returned OK result. Image URI: " + cameraImageUri);
                        if (cameraImageUri != null) {
                            startCropActivity(cameraImageUri);
                        } else {
                            Log.e(TAG, "Camera returned OK but cameraImageUri is null!");
                            Toast.makeText(this, "Error getting captured image URI", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Camera returned Cancelled or Error result: " + result.getResultCode());
                        if (cameraImageUri != null) {
                            try {
                                getContentResolver().delete(cameraImageUri, null, null);
                                Log.d(TAG, "Deleted temp image file after cancellation/failure.");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to delete temp image file.", e);
                            }
                            cameraImageUri = null;
                        }
                        Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        cropImageLauncher = registerForActivityResult(
                new CropImageContract(),
                this::onCropImageResult
        );
    }

    private void onCropImageResult(@NonNull CropImageView.CropResult result) {
        if (cameraImageUri != null) {
            try {
                getContentResolver().delete(cameraImageUri, null, null);
                Log.d(TAG, "Deleted original temp image file after cropping.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete original temp image file after cropping.", e);
            }
            cameraImageUri = null;
        }

        if (result.isSuccessful()) {
            Uri croppedUri = result.getUriContent();

            Log.i(TAG, "Image cropping successful. Cropped URI: " + croppedUri);
            if (croppedUri != null) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(TranslateTextActivity.this, croppedUri);
                    processImageWithMlKit(inputImage);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create InputImage from cropped URI", e);
                    Toast.makeText(TranslateTextActivity.this, "Error reading cropped image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Crop was successful but getUriContent() returned null!");
                Toast.makeText(TranslateTextActivity.this, "Error getting cropped image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            Exception error = result.getError();
            Log.e(TAG, "Image cropping failed", error);
            String errorMessage = "Image cropping failed";
            if (error != null) {
                errorMessage += ": " + error.getMessage();
            }
            Toast.makeText(TranslateTextActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Opening camera.");
            openCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Log.i(TAG, "Showing rationale for camera permission.");
            Toast.makeText(this, "The app needs Camera permission to scan text from images.", Toast.LENGTH_LONG).show();
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d(TAG, "Requesting camera permission.");
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = createImageFileUri();
        if (cameraImageUri != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            Log.d(TAG, "Created cameraImageUri: " + cameraImageUri + ". Starting camera activity.");
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Log.e(TAG, "No camera application found.");
                Toast.makeText(this, "No Camera app found", Toast.LENGTH_SHORT).show();
                try {
                    getContentResolver().delete(cameraImageUri, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to delete temp image file.", e);
                }
                cameraImageUri = null;
            }
        } else {
            Log.e(TAG, "Failed to create image URI for camera.");
            Toast.makeText(this, "Could not create file to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCropActivity(Uri sourceUri) {
        Log.d(TAG, "Starting crop activity for source URI: " + sourceUri);

        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON;
        cropOptions.multiTouchEnabled = true;
        cropOptions.fixAspectRatio = false;

        CropImageContractOptions contractOptions = new CropImageContractOptions(sourceUri, cropOptions);

        try {
            cropImageLauncher.launch(contractOptions);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "CropImageActivity not found (Check Manifest declaration?)", e);
            Toast.makeText(this, "Error: Crop activity not found.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error launching crop activity", e);
            Toast.makeText(this, "Error opening crop screen.", Toast.LENGTH_SHORT).show();
        }
    }


    private Uri createImageFileUri() {
        Uri contentUri = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_PICTURES) returned null.");
                throw new IOException("Cannot access external files directory.");
            }
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            String authority = "com.example.echoenglish_mobile.provider";
            contentUri = FileProvider.getUriForFile(this, authority, imageFile);
            Log.i(TAG, "Created temp image file: " + imageFile.getAbsolutePath() + ", Uri: " + contentUri);

        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Error getting FileProvider URI. Check authority '" + "com.example.echoenglish_mobile.provider" + "' in Manifest?", ex);
            Toast.makeText(this, "FileProvider configuration error", Toast.LENGTH_SHORT).show();
        }
        return contentUri;
    }

    private void processImageWithMlKit(InputImage image) {
        Log.d(TAG, "Processing CROPPED image with ML Kit Text Recognition.");
        startApiCall();
        textViewResult.setText("");

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    finishApiCall();
                    String extractedText = visionText.getText().trim();
                    Log.i(TAG, "ML Kit Text Recognition Success (from cropped). Extracted Text: \n" + extractedText);

                    if (!extractedText.isEmpty()) {
                        editTextSource.setText(extractedText);
                        Toast.makeText(this, "Text extracted. Press 'Translate Text'", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "ML Kit found no text in the cropped image.");
                        editTextSource.setText("");
                        Toast.makeText(TranslateTextActivity.this, "No text recognized in the selected area", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    finishApiCall();
                    Log.e(TAG, "ML Kit Text Recognition Failed (from cropped)", e);
                    editTextSource.setText("");
                    Toast.makeText(TranslateTextActivity.this, "Error scanning text from selected area: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void callTranslateApi(String originalText) {
        Log.d(TAG, "Calling translate API for text: " + originalText);
        startApiCall();
        textViewResult.setText("Translating...");

        String prompt;
        String sourceLang, targetLang;
        if (radioEngToVie.isChecked()) {
            prompt = "Translate the following English text to Vietnamese. Provide only the translated text with no additional information: ";
            sourceLang = "English";
            targetLang = "Vietnamese";
        } else {
            prompt = "Translate the following Vietnamese text to English. Provide only the translated text with no additional information";
            sourceLang = "Vietnamese";
            targetLang = "English";
        }

        String fullMessage = prompt + originalText;

        Log.i(TAG, "Constructed API message (Translate " + sourceLang + " to " + targetLang + "): " + fullMessage);
        TranslateRequest request = new TranslateRequest(fullMessage);

        apiService.translateText(request).enqueue(new Callback<TranslateResponse>() {
            @Override
            public void onResponse(@NonNull Call<TranslateResponse> call, @NonNull Response<TranslateResponse> response) {
                finishApiCall();
                if (response.isSuccessful() && response.body() != null && response.body().getText() != null) {
                    String translatedText = response.body().getText().trim();
                    textViewResult.setText(translatedText);
                    Log.i(TAG, "API Translation Success. Result: " + translatedText);
                } else {
                    String errorBodyString = "Unknown error body";
                    int responseCode = response.code();
                    try {
                        if (response.errorBody() != null) errorBodyString = response.errorBody().string();
                    } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                    String errorMessage = "API Error: " + responseCode + " - " + response.message() + "\nDetails: " + errorBodyString;
                    textViewResult.setText(errorMessage);
                    Log.e(TAG, "API Response Error: " + errorMessage);
                    Toast.makeText(TranslateTextActivity.this, "Error translating (Code: " + responseCode + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslateResponse> call, @NonNull Throwable t) {
                finishApiCall();
                String networkErrorMessage = "Connection or processing error: " + t.getMessage();
                textViewResult.setText(networkErrorMessage);
                Log.e(TAG, "API Call Failure", t);
                Toast.makeText(TranslateTextActivity.this, "Network or processing error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void copyTextToClipboard(String text) {
        if (text == null || text.trim().isEmpty() || textViewResult.getText().toString().equals("Translating...")) {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Translated Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
        Log.d(TAG, "startApiCall - current count: " + loadingApiCount);
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
        Log.d(TAG, "finishApiCall - current count: " + loadingApiCount);
    }

    private void showLoading(boolean isLoading) {
        Log.d(TAG, "showLoading: " + isLoading);
        if (isLoading) {
            try {
                LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Processing...");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Couldn't show dialog, state problem", e);
            }

            contentScrollView.setVisibility(View.INVISIBLE);

            editTextSource.setEnabled(false);
            buttonTranslate.setEnabled(false);
            buttonCaptureImage.setEnabled(false);
            buttonCopyResult.setEnabled(false);
            backButton.setEnabled(false);
            for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
                View child = radioGroupDirection.getChildAt(i);
                if (child instanceof RadioButton) child.setEnabled(false);
            }
        } else {
            try {
                LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Couldn't hide dialog, state problem", e);
            }

            contentScrollView.setVisibility(View.VISIBLE);

            editTextSource.setEnabled(true);
            buttonTranslate.setEnabled(true);
            buttonCaptureImage.setEnabled(true);
            buttonCopyResult.setEnabled(true);
            backButton.setEnabled(true);
            for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
                View child = radioGroupDirection.getChildAt(i);
                if (child instanceof RadioButton) child.setEnabled(true);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateTextActivity onDestroy called.");
        try {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        } catch (IllegalStateException e) {
        }
    }
}