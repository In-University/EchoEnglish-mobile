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
import android.widget.ImageButton; // Import ImageButton
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
// Import necessary clipboard classes
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;


// Import required classes from CanHub Cropper library
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
// Import your specific Request/Response classes
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateRequest;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateResponse;


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

    // --- Views ---
    private EditText editTextSource;
    private RadioGroup radioGroupDirection;
    private RadioButton radioEngToVie;
    private RadioButton radioVieToEng;
    private Button buttonTranslate;
    private Button buttonCaptureImage;
    private ProgressBar progressBar;
    private TextView textViewResult;
    private ImageButton buttonCopyResult; // Add ImageButton for copy

    // --- Logic Components ---
    private ApiService apiService;
    private TextRecognizer textRecognizer;
    private Uri cameraImageUri;

    // --- ActivityResultLaunchers ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    // Updated type for crop launcher
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_text);

        // --- Initialize Views ---
        editTextSource = findViewById(R.id.editTextSource);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioEngToVie = findViewById(R.id.radioEngToVie);
        radioVieToEng = findViewById(R.id.radioVieToEng);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        progressBar = findViewById(R.id.progressBar);
        textViewResult = findViewById(R.id.textViewResult);
        buttonCopyResult = findViewById(R.id.buttonCopyResult); // Find the new copy button

        // --- Initialization ---
        apiService = ApiClient.getApiService(); // Ensure ApiClient.getApiService() works correctly
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        initializeLaunchers();

        // --- Set Listeners ---
        buttonTranslate.setOnClickListener(v -> {
            String textToTranslate = editTextSource.getText().toString().trim();
            if (!textToTranslate.isEmpty()) {
                callTranslateApi(textToTranslate);
            } else {
                // Changed Toast text to English
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
        // 1. Camera Permission Launcher (Keep)
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted.");
                        openCamera();
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        // Changed Toast text to English
                        Toast.makeText(this, "Camera permission is required to capture images", Toast.LENGTH_LONG).show();
                    }
                });

        // 2. Take Picture Launcher (Keep)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Camera returned OK result. Image URI: " + cameraImageUri);
                        if (cameraImageUri != null) {
                            // Launch image cropping activity with the captured URI
                            startCropActivity(cameraImageUri);
                        } else {
                            Log.e(TAG, "Camera returned OK but cameraImageUri is null!");
                            // Changed Toast text to English
                            Toast.makeText(this, "Error getting captured image URI", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Camera returned Cancelled or Error result: " + result.getResultCode());
                        if (cameraImageUri != null) cameraImageUri = null; // Reset URI
                        // Changed Toast text to English
                        Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        // 3. Launcher for the Image Cropping Activity
        // Use CropImageContract and receive CropImageView.CropResult directly
        cropImageLauncher = registerForActivityResult(
                new CropImageContract(), // <<--- Correct Contract
                this::onCropImageResult // <<--- Call the result handling method
        );
    }

    // Method to handle result from CropImageContract
    private void onCropImageResult(@NonNull CropImageView.CropResult result) {
        if (result.isSuccessful()) {
            // Successfully got the cropped URI
            Uri croppedUri = result.getUriContent(); // <<--- Get the content URI of the cropped image

            Log.i(TAG, "Image cropping successful. Cropped URI: " + croppedUri);
            if (croppedUri != null) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(TranslateTextActivity.this, croppedUri);
                    processImageWithMlKit(inputImage);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create InputImage from cropped URI", e);
                    // Changed Toast text to English
                    Toast.makeText(TranslateTextActivity.this, "Error reading cropped image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Crop was successful but getUriContent() returned null!");
                // Changed Toast text to English
                Toast.makeText(TranslateTextActivity.this, "Error getting cropped image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle cropping errors
            Exception error = result.getError(); // <<--- Get the error from the result
            Log.e(TAG, "Image cropping failed", error);
            // Changed error message to English
            String errorMessage = "Image cropping failed";
            if (error != null) {
                errorMessage += ": " + error.getMessage();
            }
            // Changed Toast text to English
            Toast.makeText(TranslateTextActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    // checkCameraPermissionAndOpenCamera() (Keep)
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Opening camera.");
            openCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Log.i(TAG, "Showing rationale for camera permission.");
            // Changed Toast text to English
            Toast.makeText(this, "The app needs Camera permission to scan text from images.", Toast.LENGTH_LONG).show();
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d(TAG, "Requesting camera permission.");
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // openCamera() (Keep)
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
                // Changed Toast text to English
                Toast.makeText(this, "No Camera app found", Toast.LENGTH_SHORT).show();
                cameraImageUri = null;
            }
        } else {
            Log.e(TAG, "Failed to create image URI for camera.");
            // Changed Toast text to English
            Toast.makeText(this, "Could not create file to save image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Launches the image cropping activity using cropImageLauncher and CropImageContractOptions.
     * @param sourceUri URI of the original image to crop (from camera).
     */
    private void startCropActivity(Uri sourceUri) {
        Log.d(TAG, "Starting crop activity for source URI: " + sourceUri);

        // Create detailed Options object (optional)
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON; // Enable guidelines
        cropOptions.multiTouchEnabled = true; // Enable multi-touch zoom/rotate
        // cropOptions.aspectRatioX = 1; // Aspect ratio X (example)
        // cropOptions.aspectRatioY = 1; // Aspect ratio Y (example)
        cropOptions.fixAspectRatio = false; // Allow free aspect ratio
        // cropOptions.outputCompressFormat = Bitmap.CompressFormat.PNG; // Output format
        // cropOptions.outputCompressQuality = 90; // Compression quality
        // ... many other options in CropImageOptions

        // Create Contract Options object, passing the source URI and detailed options
        CropImageContractOptions contractOptions = new CropImageContractOptions(sourceUri, cropOptions); // <<--- Correct Contract Options

        // Launch the launcher with contract options
        try {
            cropImageLauncher.launch(contractOptions); // <<--- Correct Launch call
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "CropImageActivity not found (Check Manifest declaration?)", e);
            // Changed Toast text to English
            Toast.makeText(this, "Error: Crop activity not found.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error launching crop activity", e);
            // Changed Toast text to English
            Toast.makeText(this, "Error opening crop screen.", Toast.LENGTH_SHORT).show();
        }
    }


    // createImageFileUri() (Keep, re-check BuildConfig import)
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

            // Ensure you are importing the correct BuildConfig from your application's package
            String authority = "com.example.echoenglish_mobile.provider"; // <<--- VERIFY THIS AUTHORITY IN YOUR MANIFEST
            contentUri = FileProvider.getUriForFile(this, authority, imageFile);
            Log.i(TAG, "Created temp image file: " + imageFile.getAbsolutePath() + ", Uri: " + contentUri);

        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            // Changed Toast text to English
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Error getting FileProvider URI. Check authority '" + "com.example.echoenglish_mobile.provider" + "' in Manifest?", ex);
            // Changed Toast text to English
            Toast.makeText(this, "FileProvider configuration error", Toast.LENGTH_SHORT).show();
        }
        return contentUri;
    }


    // processImageWithMlKit() (Keep)
    private void processImageWithMlKit(InputImage image) {
        Log.d(TAG, "Processing CROPPED image with ML Kit Text Recognition.");
        showLoading(true);
        textViewResult.setText(""); // Clear old result

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    showLoading(false);
                    String extractedText = visionText.getText().trim();
                    Log.i(TAG, "ML Kit Text Recognition Success (from cropped). Extracted Text: \n" + extractedText);

                    if (!extractedText.isEmpty()) {
                        editTextSource.setText(extractedText);
                        callTranslateApi(extractedText);
                    } else {
                        Log.i(TAG, "ML Kit found no text in the cropped image.");
                        editTextSource.setText("");
                        // Changed Toast text to English
                        Toast.makeText(TranslateTextActivity.this, "No text recognized in the selected area", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "ML Kit Text Recognition Failed (from cropped)", e);
                    editTextSource.setText("");
                    // Changed Toast text to English
                    Toast.makeText(TranslateTextActivity.this, "Error scanning text from selected area: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // callTranslateApi() (Keep logic, update prompts and Toast messages)
    private void callTranslateApi(String originalText) {
        Log.d(TAG, "Calling translate API for text: " + originalText);
        showLoading(true);
        // Changed loading text to English
        textViewResult.setText("Translating...");

        String prompt;
        String sourceLang, targetLang; // Keep track of languages for logging
        if (radioEngToVie.isChecked()) { // Using the radioEngToVie view
            // Changed prompt to English
            prompt = "Translate the following English text to Vietnamese. Provide only the translated text with no additional information: ";
            sourceLang = "English";
            targetLang = "Vietnamese";
        } else { // Default to Vietnamese -> English if EngToVie is not selected
            // Changed prompt to English
            prompt = "Translate the following Vietnamese text to English. Provide only the translated text with no additional information";
            sourceLang = "Vietnamese";
            targetLang = "English";
        }

        String fullMessage = prompt + originalText;

        Log.i(TAG, "Constructed API message (Translate " + sourceLang + " to " + targetLang + "): " + fullMessage);
        TranslateRequest request = new TranslateRequest(fullMessage); // Ensure this class exists and has the correct structure

        apiService.translateText(request).enqueue(new Callback<TranslateResponse>() { // Ensure ApiService has translateText method
            @Override
            public void onResponse(@NonNull Call<TranslateResponse> call, @NonNull Response<TranslateResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getText() != null) {
                    String translatedText = response.body().getText().trim(); // Ensure TranslateResponse has getText()
                    textViewResult.setText(translatedText);
                    Log.i(TAG, "API Translation Success. Result: " + translatedText);
                } else {
                    String errorBodyString = "Unknown error body";
                    int responseCode = response.code();
                    try {
                        if (response.errorBody() != null) errorBodyString = response.errorBody().string();
                    } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                    // Changed error message logging/display to English
                    String errorMessage = "API Error: " + responseCode + " - " + response.message() + "\nDetails: " + errorBodyString;
                    textViewResult.setText(errorMessage);
                    Log.e(TAG, "API Response Error: " + errorMessage);
                    // Changed Toast text to English
                    Toast.makeText(TranslateTextActivity.this, "Error translating (Code: " + responseCode + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslateResponse> call, @NonNull Throwable t) {
                showLoading(false);
                // Changed network error message logging/display to English
                String networkErrorMessage = "Connection or processing error: " + t.getMessage();
                textViewResult.setText(networkErrorMessage);
                Log.e(TAG, "API Call Failure", t);
                // Changed Toast text to English
                Toast.makeText(TranslateTextActivity.this, "Network or processing error", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method to copy text to clipboard
    private void copyTextToClipboard(String text) {
        if (text == null || text.trim().isEmpty()) {
            // Changed Toast text to English
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // Using a meaningful label for the clip data
        ClipData clip = ClipData.newPlainText("Translated Text", text);
        clipboard.setPrimaryClip(clip);

        // Changed Toast text to English
        Toast.makeText(this, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    // showLoading() (Update to include copy button)
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonTranslate.setEnabled(!isLoading);
        buttonCaptureImage.setEnabled(!isLoading);
        editTextSource.setEnabled(!isLoading);
        buttonCopyResult.setEnabled(!isLoading); // Enable/disable copy button
        for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
            View child = radioGroupDirection.getChildAt(i);
            if (child instanceof RadioButton) child.setEnabled(!isLoading);
        }
    }

    // onDestroy() (Keep)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateTextActivity onDestroy called.");
    }
}