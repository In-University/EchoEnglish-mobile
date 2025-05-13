package com.example.echoenglish_mobile.view.activity.flashcard;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView; // Still used inside the item layout

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";
    public static final String USER_ID_EXTRA = "USER_ID";
    // Removed LOADING_DIALOG_TAG as DialogFragment is removed

    private long userId = -1L;

    private ImageView backButton;
    private TextView textScreenTitle;
    private TextView textReviewProgress;

    // Changed from ConstraintLayout reviewCardContainer
    private ViewPager2 viewPagerReview; // Using ViewPager2

    private Button buttonForgetReview;
    private Button buttonRememberReview;
    private LinearLayout layoutReviewButtons;

    private TextView textReviewEmptyMessage;

    private ApiService apiService;
    private List<VocabularyReviewResponse> dueWordsList;
    private ReviewCardPagerAdapter pagerAdapter; // Adapter for ViewPager2

    // Removed flip animations as they are handled within the Fragment now
    // private ObjectAnimator flipRightIn;
    // private ObjectAnimator flipRightOut;

    // Removed loadingApiCount field

    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        findViews();
        setupCustomHeader();
        apiService = ApiClient.getApiService();

        userId = getIntent().getLongExtra(USER_ID_EXTRA, -1L);
        if (userId == -1L) {
            Log.e(TAG, "Invalid User ID received.");
            Toast.makeText(this, "Error: Cannot load review data.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initial manual loading state
        setUiState(UiState.LOADING);

        loadDueWordsForReview(userId);

        buttonRememberReview.setOnClickListener(v -> handleRememberForgetClick(true));
        buttonForgetReview.setOnClickListener(v -> handleRememberForgetClick(false));

        // Set up ViewPager2 page change listener to update the progress text
        viewPagerReview.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update progress text when page changes
                updateProgressText(position);
                // Optionally reset card flip state when page changes
                // getFragmentAtPosition(position).resetFlipState(); // This requires adapter/fragment logic
            }
        });

        // Removed reviewCardContainer click listener (flip is handled by Fragment)
        // Removed loadFlipAnimations (handled by Fragment)
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Reloading review list.");
        setUiState(UiState.LOADING);
        loadDueWordsForReview(userId);
    }

    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);
        textReviewProgress = findViewById(R.id.textReviewProgress);

        // Find ViewPager2 instead of the card container
        viewPagerReview = findViewById(R.id.viewPagerReview);

        layoutReviewButtons = findViewById(R.id.layoutReviewButtons);
        buttonForgetReview = findViewById(R.id.buttonForgetReview);
        buttonRememberReview = findViewById(R.id.buttonRememberReview);

        textReviewEmptyMessage = findViewById(R.id.textReviewEmptyMessage);
    }

    private void setupCustomHeader() {
        textScreenTitle.setText("Review Vocabulary");
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        // Check if the list is loaded and not finished
        if (dueWordsList != null && viewPagerReview != null && viewPagerReview.getAdapter() != null &&
                viewPagerReview.getCurrentItem() < viewPagerReview.getAdapter().getItemCount()) {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Review?")
                    .setMessage("Are you sure you want to leave the review session?")
                    .setPositiveButton("Leave", (dialog, which) -> {
                        ReviewActivity.super.onBackPressed();
                    })
                    .setNegativeButton("Stay", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }


    // Enum for managing UI states
    private enum UiState {
        LOADING, CONTENT, EMPTY, FINISHED // FINISHED might be similar to EMPTY but with a different message
    }

    // Method to manage overall UI state manually
    private void setUiState(UiState state) {
        viewPagerReview.setVisibility(View.GONE);
        layoutReviewButtons.setVisibility(View.GONE);
        textReviewProgress.setVisibility(View.GONE);
        textReviewEmptyMessage.setVisibility(View.GONE);
        setButtonsEnabled(false); // Disable buttons by default in all states except CONTENT fully ready
        backButton.setEnabled(true); // Back button usually enabled

        switch (state) {
            case LOADING:
                textReviewProgress.setVisibility(View.VISIBLE);
                textReviewProgress.setText("Loading...");
                backButton.setEnabled(false); // Disable back during initial load
                break;
            case CONTENT:
                viewPagerReview.setVisibility(View.VISIBLE);
                layoutReviewButtons.setVisibility(View.VISIBLE);
                textReviewProgress.setVisibility(View.VISIBLE); // Progress text is part of content state
                setButtonsEnabled(true); // Enable buttons when content is ready
                break;
            case EMPTY:
                textReviewEmptyMessage.setVisibility(View.VISIBLE);
                textReviewEmptyMessage.setText("No words are currently due for review.");
                break;
            case FINISHED:
                textReviewEmptyMessage.setVisibility(View.VISIBLE);
                textReviewEmptyMessage.setText("Review session completed!");
                break;
        }
        Log.d(TAG, "UI State changed to: " + state);
    }

    // Helper to enable/disable Remember/Forget buttons specifically
    private void setButtonsEnabled(boolean enabled) {
        buttonRememberReview.setEnabled(enabled);
        buttonForgetReview.setEnabled(enabled);
    }


    // Update the progress text (e.g., "5 / 20")
    private void updateProgressText(int currentPosition) {
        if (dueWordsList != null) {
            textReviewProgress.setText(String.format(Locale.getDefault(), "%d / %d",
                    currentPosition + 1, dueWordsList.size()));
        }
    }


    private void loadDueWordsForReview(long userId) {
        apiService.getDueVocabulariesForReview(userId).enqueue(new Callback<List<VocabularyReviewResponse>>() {
            @Override
            public void onResponse(Call<List<VocabularyReviewResponse>> call, Response<List<VocabularyReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dueWordsList = response.body();
                    if (!dueWordsList.isEmpty()) {
                        // List loaded successfully and is not empty
                        setupViewPager(dueWordsList); // Setup ViewPager with the list
                        setUiState(UiState.CONTENT); // Show content UI
                        updateProgressText(0); // Set initial progress text
                    } else {
                        // List is empty
                        dueWordsList = new ArrayList<>(); // Ensure it's an empty list
                        setUiState(UiState.EMPTY); // Show empty state UI
                    }
                } else {
                    Log.e(TAG, "Failed to load due review list: " + response.code() + " - " + response.message());
                    dueWordsList = new ArrayList<>(); // Use empty list on failure
                    setUiState(UiState.EMPTY); // Show empty state UI (will display default empty message)
                    Toast.makeText(ReviewActivity.this, "Failed to load review list.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VocabularyReviewResponse>> call, Throwable t) {
                Log.e(TAG, "Network error loading due review list", t);
                dueWordsList = new ArrayList<>(); // Use empty list on network error
                setUiState(UiState.EMPTY); // Show empty state UI (will display default empty message)
                Toast.makeText(ReviewActivity.this, "Network error loading review list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Setup ViewPager2 with the loaded vocabulary list
    private void setupViewPager(List<VocabularyReviewResponse> vocabList) {
        if (vocabList == null || vocabList.isEmpty()) {
            Log.w(TAG, "Attempted to setup ViewPager with empty or null list.");
            setUiState(UiState.EMPTY);
            return;
        }
        pagerAdapter = new ReviewCardPagerAdapter(this, vocabList);
        viewPagerReview.setAdapter(pagerAdapter);
        // Optionally disable swipe if you only want button navigation after seeing back
        // viewPagerReview.setUserInputEnabled(false); // Uncomment to disable swipe
    }


    // Handle click on Remember or Forget button
    private void handleRememberForgetClick(boolean isRemembered) {
        // Ensure buttons are enabled before processing (should be disabled by setUiEnabled)
        if (!buttonRememberReview.isEnabled()) {
            Log.w(TAG, "Remember/Forget click ignored, buttons are disabled.");
            return;
        }

        int currentPosition = viewPagerReview.getCurrentItem();
        if (dueWordsList == null || pagerAdapter == null || currentPosition < 0 || currentPosition >= dueWordsList.size()) {
            Log.w(TAG, "Remember/Forget click ignored, adapter state invalid or index out of bounds.");
            Toast.makeText(this, "Error processing word.", Toast.LENGTH_SHORT).show();
            // Maybe end session if state is bad?
            // endReviewSession();
            return;
        }

        setButtonsEnabled(false); // Disable buttons while processing

        VocabularyReviewResponse currentWord = dueWordsList.get(currentPosition);
        if (currentWord.getId() == null) {
            Log.e(TAG, "Current word ID is null, cannot record progress.");
            Toast.makeText(this, "Error: Cannot record progress for this word.", Toast.LENGTH_SHORT).show();
            // Move to next word visually despite error if needed
            if (currentPosition < dueWordsList.size() - 1) {
                viewPagerReview.setCurrentItem(currentPosition + 1, true);
            } else {
                endReviewSession(); // End if it was the last word
            }
            setButtonsEnabled(true); // Re-enable buttons if moved
            return;
        }

        // Record learning progress via API
        recordLearningApiCall(currentWord.getId(), isRemembered);

        // Move to the next word after a short delay for smoother transition
        // The ViewPager2 page change listener will update the progress text automatically
        mainHandler.postDelayed(() -> {
            if (currentPosition < dueWordsList.size() - 1) {
                viewPagerReview.setCurrentItem(currentPosition + 1, true);
                setButtonsEnabled(true); // Re-enable buttons for the next word
            } else {
                endReviewSession(); // End if it was the last word
            }
        }, 500); // Delay for visual transition
    }

    // Record learning progress via API
    private void recordLearningApiCall(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized. Cannot record learning progress.");
            return;
        }
        // No startApiCall/finishApiCall here - API call runs in background

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(userId);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // API finished
                if (response.isSuccessful()) {
                    Log.i(TAG, "Learning recorded successfully for vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.w(TAG, "Failed to record learning for vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message() + " Body: " + errorBody);
                    // Toast.makeText(ReviewActivity.this, "Failed to record progress.", Toast.LENGTH_SHORT).show(); // Optional
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // API finished (failure)
                Log.e(TAG, "Network error recording learning for vocab ID: " + vocabularyId, t);
                Toast.makeText(ReviewActivity.this, "Network error saving progress.", Toast.LENGTH_SHORT).show(); // Optional
            }
        });
    }

    // End the review session
    private void endReviewSession() {
        Log.d(TAG, "Review session ended.");
        Toast.makeText(this, "Review session completed!", Toast.LENGTH_LONG).show();
        setUiState(UiState.FINISHED); // Show finished state message

        // Finish activity after a delay
        mainHandler.postDelayed(this::finish, 2000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending PostDelayed callbacks
        mainHandler.removeCallbacksAndMessages(null);
        // Unregister ViewPager2 callback to prevent leaks
        if (viewPagerReview != null) {
            viewPagerReview.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {}); // Using empty anonymous callback to unregister
        }
    }
}