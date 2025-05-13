package com.example.echoenglish_mobile.view.activity.flashcard;

// Removed static import
// import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
// Removed: import android.widget.ProgressBar; // Use dialog instead
import android.widget.Button;
import android.widget.TextView; // Added TextView for custom title
import android.widget.ImageView; // Added ImageView for custom back button
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
// Import your Loading Dialog
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateFlashcardActivity extends AppCompatActivity {

    private static final String ACTIVITY_TAG = "CreateFlashcard"; // Use a distinct TAG
    // Tag for the Loading Dialog Fragment
    private static final String LOADING_DIALOG_TAG = "CreateFlashcardLoadingDialog";


    public static final String EXTRA_EDIT_MODE = "EDIT_MODE";
    public static final String EXTRA_FLASHCARD_ID = "FLASHCARD_ID";
    public static final String EXTRA_FLASHCARD_NAME = "FLASHCARD_NAME";
    public static final String EXTRA_FLASHCARD_IMAGE_URL = "FLASHCARD_IMAGE_URL";

    // Header Elements (Matches the XML layout)
    private ImageView backButton; // Or View
    private TextView textScreenTitle;


    private TextInputLayout textFieldLayoutName;
    private TextInputEditText editTextName;
    private TextInputEditText editTextImageUrl;
    private Button buttonCreate;
    // Removed: private ProgressBar progressBar; // Use dialog instead

    private ApiService apiService;

    private boolean isEditMode = false;
    private Long editingFlashcardId = null;

    // Counter for tracking active API calls
    private int loadingApiCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flashcard);

        // Ánh xạ View (Header)
        backButton = findViewById(R.id.backButton); // Find the custom back button
        textScreenTitle = findViewById(R.id.textScreenTitle); // Find the custom title TextView


        textFieldLayoutName = findViewById(R.id.textFieldLayoutFlashcardName);
        editTextName = findViewById(R.id.editTextFlashcardName);
        editTextImageUrl = findViewById(R.id.editTextFlashcardImageUrl);
        buttonCreate = findViewById(R.id.buttonCreateFlashcardSubmit);
        // Removed: progressBar = findViewById(R.id.progressBarCreateFlashcard);

        apiService = ApiClient.getApiService();

        // Check Intent for edit mode
        if (getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false)) {
            isEditMode = true;
            editingFlashcardId = getIntent().getLongExtra(EXTRA_FLASHCARD_ID, -1L);
            String currentName = getIntent().getStringExtra(EXTRA_FLASHCARD_NAME);
            String currentImageUrl = getIntent().getStringExtra(EXTRA_FLASHCARD_IMAGE_URL);

            if (editingFlashcardId == -1L) {
                Toast.makeText(this, "Error: Flashcard ID not found for editing.", Toast.LENGTH_LONG).show(); // Translated
                finish();
                return;
            }

            editTextName.setText(currentName);
            editTextImageUrl.setText(currentImageUrl);

            // Set title and button text for edit mode
            textScreenTitle.setText("Edit Flashcard Set"); // Translated title
            buttonCreate.setText("Save Changes"); // Translated button text
        } else {
            // Set title and button text for create mode
            textScreenTitle.setText("Create New Flashcard Set"); // Translated title
            buttonCreate.setText("Create Flashcard Set"); // Translated button text
        }

        // Set listener for custom back button
        backButton.setOnClickListener(v -> finish());


        buttonCreate.setOnClickListener(v -> {
            if (isEditMode) {
                attemptUpdateFlashcard();
            } else {
                attemptCreateFlashcard();
            }
        });
    }

    // --- Loading Logic using DialogFragment ---

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) { // Only show dialog on the first active call
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0; // Ensure it doesn't go negative
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            // Show the loading dialog with message based on mode
            String loadingMessage = isEditMode ? "Saving changes..." : "Creating flashcard..."; // Translated messages
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, loadingMessage);

            // Disable interactive elements
            buttonCreate.setEnabled(false);
            editTextName.setEnabled(false);
            editTextImageUrl.setEnabled(false);
            backButton.setEnabled(false);
        } else {
            // Hide the loading dialog
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);

            // Re-enable interactive elements
            buttonCreate.setEnabled(true);
            editTextName.setEnabled(true);
            editTextImageUrl.setEnabled(true);
            backButton.setEnabled(true);
        }
    }
    // --- End Loading Logic ---


    private void attemptCreateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Flashcard set name cannot be empty."); // Translated error
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        FlashcardCreateRequest request = new FlashcardCreateRequest();
        request.setName(name);
        request.setImageUrl(imageUrl);

        startApiCall(); // Start counting API calls and show loading

        apiService.createFlashcard(request).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                finishApiCall(); // Finish counting API calls and hide loading
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Flashcard set created successfully!", Toast.LENGTH_SHORT).show(); // Translated
                    finish(); // Close the activity
                } else {
                    String errorMessage = "Failed to create: " + response.code(); // Translated error
                    Log.e(ACTIVITY_TAG, "Failed to create flashcard: " + response.code());
                    // Attempt to read error body if available
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                        // You might parse errorBody for a user-friendly message if backend provides one
                        // errorMessage += "\n" + errorBody; // Example: append error body details
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CreateFlashcardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                finishApiCall(); // Finish counting API calls and hide loading
                Log.e(ACTIVITY_TAG, "Network error creating flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Network connection error.", Toast.LENGTH_SHORT).show(); // Translated
            }
        });
    }

    private void attemptUpdateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Flashcard set name cannot be empty."); // Translated error
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        // Ensure we have a valid ID for updating
        if (editingFlashcardId == null || editingFlashcardId == -1L) {
            Log.e(ACTIVITY_TAG, "Attempted to update with invalid Flashcard ID: " + editingFlashcardId);
            Toast.makeText(this, "Error: Cannot update flashcard set.", Toast.LENGTH_SHORT).show(); // Translated
            return;
        }

        FlashcardUpdateRequest updateRequest = new FlashcardUpdateRequest();
        updateRequest.setName(name);
        updateRequest.setImageUrl(imageUrl);

        startApiCall(); // Start counting API calls and show loading

        apiService.updateFlashcard(editingFlashcardId, updateRequest).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                finishApiCall(); // Finish counting API calls and hide loading
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Flashcard set updated successfully.", Toast.LENGTH_SHORT).show(); // Translated
                    finish(); // Close the activity
                } else {
                    String errorMessage = "Failed to update: " + response.code(); // Translated error
                    Log.e(ACTIVITY_TAG, "Failed to update flashcard: " + response.code());
                    // Attempt to read error body if available
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                        // You might parse errorBody for a user-friendly message if backend provides one
                        // errorMessage += "\n" + errorBody; // Example: append error body details
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CreateFlashcardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                finishApiCall(); // Finish counting API calls and hide loading
                Log.e(ACTIVITY_TAG, "Network error updating flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Network error while updating.", Toast.LENGTH_SHORT).show(); // Translated
            }
        });
    }
}