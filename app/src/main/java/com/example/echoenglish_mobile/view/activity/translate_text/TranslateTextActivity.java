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
import android.widget.ProgressBar; // Keep ProgressBar reference
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView; // Import ScrollView
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
    private static final String LOADING_DIALOG_TAG = "LoadingDialog"; // Tag for the dialog fragment

    // --- Views ---
    private EditText editTextSource;
    private RadioGroup radioGroupDirection;
    private RadioButton radioEngToVie;
    private RadioButton radioVieToEng;
    private Button buttonTranslate;
    private Button buttonCaptureImage;
    private ProgressBar progressBar; // Keep reference for manual control if needed, or remove if dialog is sufficient
    private TextView textViewResult;
    private ImageButton buttonCopyResult;
    private ImageView backButton; // Header back button
    private TextView headerTitle; // Header title
    private ScrollView contentScrollView; // ScrollView wrapping the main content

    // --- Logic Components ---
    private ApiService apiService;
    private TextRecognizer textRecognizer;
    private Uri cameraImageUri;

    // --- ActivityResultLaunchers ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    // --- Loading Counter ---
    private int loadingApiCount = 0; // Counter for active asynchronous operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_text);

        // --- Initialize Views ---
        // Header views
        backButton = findViewById(R.id.backButton);
        headerTitle = findViewById(R.id.headerTitle); // Optional: Set title here if not fixed in XML
        contentScrollView = findViewById(R.id.contentScrollView); // Get the ScrollView

        // Content views
        editTextSource = findViewById(R.id.editTextSource);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioEngToVie = findViewById(R.id.radioEngToVie);
        radioVieToEng = findViewById(R.id.radioVieToEng);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        progressBar = findViewById(R.id.progressBar); // Keep this reference, but its visibility is now managed by showLoading
        textViewResult = findViewById(R.id.textViewResult);
        buttonCopyResult = findViewById(R.id.buttonCopyResult);

        // --- Initialization ---
        apiService = ApiClient.getApiService();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        initializeLaunchers();

        // Optional: Set title dynamically
        headerTitle.setText("Translate Text");


        // --- Set Listeners ---
        // Header Back Button
        backButton.setOnClickListener(v -> {
            onBackPressed(); // Navigate back
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

        // Add listener for the copy button
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
                            // Clean up the temp file if capture was cancelled/failed
                            try {
                                getContentResolver().delete(cameraImageUri, null, null);
                                Log.d(TAG, "Deleted temp image file after cancellation/failure.");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to delete temp image file.", e);
                            }
                            cameraImageUri = null; // Reset URI
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
        // Clean up the original temp camera file after cropping is done (success or failure)
        if (cameraImageUri != null) {
            try {
                getContentResolver().delete(cameraImageUri, null, null);
                Log.d(TAG, "Deleted original temp image file after cropping.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete original temp image file after cropping.", e);
            }
            cameraImageUri = null; // Reset URI
        }

        if (result.isSuccessful()) {
            Uri croppedUri = result.getUriContent();

            Log.i(TAG, "Image cropping successful. Cropped URI: " + croppedUri);
            if (croppedUri != null) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(TranslateTextActivity.this, croppedUri);
                    processImageWithMlKit(inputImage); // Start ML Kit processing -> call startApiCall
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create InputImage from cropped URI", e);
                    Toast.makeText(TranslateTextActivity.this, "Error reading cropped image", Toast.LENGTH_SHORT).show();
                    // No need to call finishApiCall here, processImageWithMlKit will handle it
                }
            } else {
                Log.e(TAG, "Crop was successful but getUriContent() returned null!");
                Toast.makeText(TranslateTextActivity.this, "Error getting cropped image URI", Toast.LENGTH_SHORT).show();
                // No async task started, no need to call finishApiCall
            }
        } else {
            Exception error = result.getError();
            Log.e(TAG, "Image cropping failed", error);
            String errorMessage = "Image cropping failed";
            if (error != null) {
                errorMessage += ": " + error.getMessage();
            }
            Toast.makeText(TranslateTextActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            // No async task started, no need to call finishApiCall
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
                // Clean up the temp file if camera app not found
                try {
                    getContentResolver().delete(cameraImageUri, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to delete temp image file.", e);
                }
                cameraImageUri = null; // Reset URI
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
            // No async task started, no need to call finishApiCall
        } catch (Exception e) {
            Log.e(TAG, "Error launching crop activity", e);
            Toast.makeText(this, "Error opening crop screen.", Toast.LENGTH_SHORT).show();
            // No async task started, no need to call finishApiCall
        }
    }


    private Uri createImageFileUri() {
        Uri contentUri = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            // Use getExternalFilesDir for files that are meant for this app only and can be deleted on uninstall
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_PICTURES) returned null.");
                throw new IOException("Cannot access external files directory.");
            }
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            String authority = "com.example.echoenglish_mobile.provider"; // <<< VERIFY THIS AUTHORITY IN YOUR MANIFEST
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

    // processImageWithMlKit() - Now wraps ML Kit call in start/finish
    private void processImageWithMlKit(InputImage image) {
        Log.d(TAG, "Processing CROPPED image with ML Kit Text Recognition.");
        startApiCall(); // Start loading counter for ML Kit
        textViewResult.setText(""); // Clear old result

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    finishApiCall(); // Decrement loading counter on success
                    String extractedText = visionText.getText().trim();
                    Log.i(TAG, "ML Kit Text Recognition Success (from cropped). Extracted Text: \n" + extractedText);

                    if (!extractedText.isEmpty()) {
                        editTextSource.setText(extractedText);
                        // Decide if you want to auto-translate after OCR
                        // If yes: callTranslateApi(extractedText);
                        // If no: let the user click translate button
                        // For this example, let's not auto-translate, just fill the text field.
                        Toast.makeText(this, "Text extracted. Press 'Translate Text'", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "ML Kit found no text in the cropped image.");
                        editTextSource.setText("");
                        Toast.makeText(TranslateTextActivity.this, "No text recognized in the selected area", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    finishApiCall(); // Decrement loading counter on failure
                    Log.e(TAG, "ML Kit Text Recognition Failed (from cropped)", e);
                    editTextSource.setText("");
                    Toast.makeText(TranslateTextActivity.this, "Error scanning text from selected area: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // callTranslateApi() - Now wraps Retrofit call in start/finish
    private void callTranslateApi(String originalText) {
        Log.d(TAG, "Calling translate API for text: " + originalText);
        startApiCall(); // Start loading counter for API call
        textViewResult.setText("Translating..."); // Update UI state

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
                finishApiCall(); // Decrement loading counter on response
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
                finishApiCall(); // Decrement loading counter on failure
                String networkErrorMessage = "Connection or processing error: " + t.getMessage();
                textViewResult.setText(networkErrorMessage);
                Log.e(TAG, "API Call Failure", t);
                Toast.makeText(TranslateTextActivity.this, "Network or processing error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void copyTextToClipboard(String text) {
        if (text == null || text.trim().isEmpty() || textViewResult.getText().toString().equals("Translating...")) { // Don't copy loading text
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Translated Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    // --- Loading Logic using the provided pattern ---

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) { // Only show dialog and disable UI on the first active call
            showLoading(true);
        }
        Log.d(TAG, "startApiCall - current count: " + loadingApiCount);
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0; // Ensure it doesn't go negative
            showLoading(false); // Hide dialog and enable UI when all calls finish
        }
        Log.d(TAG, "finishApiCall - current count: " + loadingApiCount);
    }

    // Modified showLoading to use DialogFragment and control this activity's UI
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "showLoading: " + isLoading);
        if (isLoading) {
            // Show the loading dialog
            // Use try-catch in case activity state is saved/restored awkwardly
            try {
                LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Processing..."); // Pass optional message
            } catch (IllegalStateException e) {
                Log.e(TAG, "Couldn't show dialog, state problem", e);
            }

            // Optionally show the ProgressBar as well (or remove it from XML entirely)
            // progressBar.setVisibility(View.VISIBLE);

            // Hide content by making the ScrollView invisible
            contentScrollView.setVisibility(View.INVISIBLE);

            // Disable interactive elements
            editTextSource.setEnabled(false);
            buttonTranslate.setEnabled(false);
            buttonCaptureImage.setEnabled(false);
            buttonCopyResult.setEnabled(false);
            backButton.setEnabled(false); // Disable back button while loading
            for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
                View child = radioGroupDirection.getChildAt(i);
                if (child instanceof RadioButton) child.setEnabled(false);
            }
        } else {
            // Hide the loading dialog
            try {
                LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Couldn't hide dialog, state problem", e);
            }


            // Optionally hide the ProgressBar
            // progressBar.setVisibility(View.GONE);

            // Show content
            contentScrollView.setVisibility(View.VISIBLE);

            // Re-enable interactive elements
            editTextSource.setEnabled(true);
            buttonTranslate.setEnabled(true);
            buttonCaptureImage.setEnabled(true);
            buttonCopyResult.setEnabled(true);
            backButton.setEnabled(true); // Re-enable back button
            for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
                View child = radioGroupDirection.getChildAt(i);
                if (child instanceof RadioButton) child.setEnabled(true);
            }
            // Re-enable copy button only if there is text to copy
            // buttonCopyResult.setEnabled(!textViewResult.getText().toString().trim().isEmpty()); // Optional check
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateTextActivity onDestroy called.");
        // Ensure dialog is dismissed to prevent leaks if activity is destroyed while loading
        try {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        } catch (IllegalStateException e) {
            // Ignore, state is already messed up or dialog wasn't showing
        }
    }
}