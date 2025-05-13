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

    private static final String ACTIVITY_TAG = "CreateFlashcard";
    // Tag for the Loading Dialog Fragment
    private static final String LOADING_DIALOG_TAG = "CreateFlashcardLoadingDialog";


    public static final String EXTRA_EDIT_MODE = "EDIT_MODE";
    public static final String EXTRA_FLASHCARD_ID = "FLASHCARD_ID";
    public static final String EXTRA_FLASHCARD_NAME = "FLASHCARD_NAME";
    public static final String EXTRA_FLASHCARD_IMAGE_URL = "FLASHCARD_IMAGE_URL";

    private ImageView backButton;
    private TextView textScreenTitle;


    private TextInputLayout textFieldLayoutName;
    private TextInputEditText editTextName;
    private TextInputEditText editTextImageUrl;
    private Button buttonCreate;

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
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);


        textFieldLayoutName = findViewById(R.id.textFieldLayoutFlashcardName);
        editTextName = findViewById(R.id.editTextFlashcardName);
        editTextImageUrl = findViewById(R.id.editTextFlashcardImageUrl);
        buttonCreate = findViewById(R.id.buttonCreateFlashcardSubmit);

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

            textScreenTitle.setText("Edit Flashcard Set");
            buttonCreate.setText("Save Changes");
        } else {
            textScreenTitle.setText("Create New Flashcard Set");
            buttonCreate.setText("Create Flashcard Set");
        }

        backButton.setOnClickListener(v -> finish());


        buttonCreate.setOnClickListener(v -> {
            if (isEditMode) {
                attemptUpdateFlashcard();
            } else {
                attemptCreateFlashcard();
            }
        });
    }


    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            String loadingMessage = isEditMode ? "Saving changes..." : "Creating flashcard...";
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, loadingMessage);

            buttonCreate.setEnabled(false);
            editTextName.setEnabled(false);
            editTextImageUrl.setEnabled(false);
            backButton.setEnabled(false);
        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);

            buttonCreate.setEnabled(true);
            editTextName.setEnabled(true);
            editTextImageUrl.setEnabled(true);
            backButton.setEnabled(true);
        }
    }


    private void attemptCreateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Flashcard set name cannot be empty.");
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        FlashcardCreateRequest request = new FlashcardCreateRequest();
        request.setName(name);
        request.setImageUrl(imageUrl);

        startApiCall();

        apiService.createFlashcard(request).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                finishApiCall();
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Flashcard set created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "Failed to create: " + response.code();
                    Log.e(ACTIVITY_TAG, "Failed to create flashcard: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CreateFlashcardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error creating flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Network connection error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptUpdateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Flashcard set name cannot be empty.");
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        if (editingFlashcardId == null || editingFlashcardId == -1L) {
            Log.e(ACTIVITY_TAG, "Attempted to update with invalid Flashcard ID: " + editingFlashcardId);
            Toast.makeText(this, "Error: Cannot update flashcard set.", Toast.LENGTH_SHORT).show();
            return;
        }

        FlashcardUpdateRequest updateRequest = new FlashcardUpdateRequest();
        updateRequest.setName(name);
        updateRequest.setImageUrl(imageUrl);

        startApiCall();

        apiService.updateFlashcard(editingFlashcardId, updateRequest).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                finishApiCall();
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Flashcard set updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "Failed to update: " + response.code();
                    Log.e(ACTIVITY_TAG, "Failed to update flashcard: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CreateFlashcardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error updating flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Network error while updating.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}