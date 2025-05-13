package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
// Removed: import android.widget.ProgressBar; // No longer needed for layout ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.widget.ScrollView;

// Import MaterialCardView
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.DueReviewCountResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.MemoryLevelsResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;


import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpacedRepetitionActivity extends AppCompatActivity {
    private Long currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();

    private static final String TAG = "SpacedRepetitionAct";
    // Tag for the Loading Dialog Fragment
    private static final String LOADING_DIALOG_TAG = "SpacedRepetitionLoadingDialog";


    // TODO: Get actual user ID from login/session management



    // Header Elements
    private ImageView backButton; // Back to ImageView
    private TextView textScreenTitle;

    // Memory Level TextViews
    private TextView textMemoryLevel0;
    private TextView textMemoryLevel1;
    private TextView textMemoryLevel2;
    private TextView textMemoryLevel3;
    private TextView textMemoryLevel4;
    private TextView textMemoryLevelMastered;

    // Review Section
    private TextView textDueReviewCount;
    private Button buttonReviewNow;

    // Navigation Cards
    private MaterialCardView cardMyDecks;
    private MaterialCardView cardPublicDecks;
    // Optional: TextViews for counts


    // Removed: private ProgressBar progressBarSRS; // No longer needed

    // Use the ScrollView reference for showing/hiding content
    private ScrollView contentScrollView;


    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spaced_repetition);

        // Ánh xạ View (Header)
        backButton = findViewById(R.id.backButton); // Find the custom back button (ImageView)
        textScreenTitle = findViewById(R.id.textScreenTitle); // Find the custom title TextView

        // Ánh xạ View (Memory Levels)
        textMemoryLevel0 = findViewById(R.id.textMemoryLevel0);
        textMemoryLevel1 = findViewById(R.id.textMemoryLevel1);
        textMemoryLevel2 = findViewById(R.id.textMemoryLevel2);
        textMemoryLevel3 = findViewById(R.id.textMemoryLevel3);
        textMemoryLevel4 = findViewById(R.id.textMemoryLevel4);
        textMemoryLevelMastered = findViewById(R.id.textMemoryLevelMastered);

        // Ánh xạ View (Review Section)
        textDueReviewCount = findViewById(R.id.textDueReviewCount);
        buttonReviewNow = findViewById(R.id.buttonReviewNow);

        // Ánh xạ View (Navigation Cards)
        cardMyDecks = findViewById(R.id.cardMyDecks);
        cardPublicDecks = findViewById(R.id.cardPublicDecks);
        // Optional: If you added count TextViews, find them here
        // textMyDecksCount = findViewById(R.id.textMyDecksCount);
        // textPublicDecksCount = findViewById(R.id.textPublicDecksCount);


        // Ánh xạ View (Content)
        contentScrollView = findViewById(R.id.contentScrollView); // Reference to the ScrollView


        // Initialize API Service
        apiService = ApiClient.getApiService();

        // Load global SRS data (Memory Levels and Due Review Count)
        loadMemoryLevels();
        loadDueReviewCount();
        // Optional: Load deck counts if you added the TextViews for them
        // loadDeckCounts();


        // Set listeners
        // Custom back button listener (Using finish() instead of deprecated onBackPressed())
        backButton.setOnClickListener(v -> finish());

        // Review button listener
        buttonReviewNow.setOnClickListener(v -> startReview());

        // Set listeners for the navigation cards
        cardMyDecks.setOnClickListener(v -> {
            Intent intent = new Intent(SpacedRepetitionActivity.this, MyFlashcardsActivity.class);
            // Optional: Pass user ID if MyFlashcardsActivity needs it
            // intent.putExtra(MyFlashcardsActivity.USER_ID_EXTRA, CURRENT_USER_ID);
            startActivity(intent);
        });

        cardPublicDecks.setOnClickListener(v -> {
            Intent intent = new Intent(SpacedRepetitionActivity.this, PublicCategoriesActivity.class);
            // Optional: Pass user ID if PublicCategoriesActivity needs it
            // intent.putExtra(PublicCategoriesActivity.USER_ID_EXTRA, CURRENT_USER_ID);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi Activity trở lại foreground
        Log.d(TAG, "onResume: Refreshing SRS data.");
        loadMemoryLevels();
        loadDueReviewCount();
        // Optional: Reload deck counts if you added the TextViews for them
        // loadDeckCounts();
    }


    // Method to show/hide loading using DialogFragment
    private int loadingApiCount = 0; // Đếm số lượng API đang chạy

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) { // Only show dialog on the first active call
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0; // Just in case it goes negative somehow
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            // Show the loading dialog
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading data..."); // Pass optional message
            // Hide content
            contentScrollView.setVisibility(View.INVISIBLE);
            // Disable interactive elements
            buttonReviewNow.setEnabled(false);
            cardMyDecks.setEnabled(false);
            cardPublicDecks.setEnabled(false);
            backButton.setEnabled(false);
        } else {
            // Hide the loading dialog
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            // Show content
            contentScrollView.setVisibility(View.VISIBLE);
            // Re-enable interactive elements based on data/state
            // Re-enable review button only if count is > 0
            boolean canReview = textDueReviewCount.getText().toString().contains(" words") ? Integer.parseInt(textDueReviewCount.getText().toString().replace(" words", "").replace("?", "0")) > 0 : false;
            buttonReviewNow.setEnabled(canReview); // Enable only if review count > 0
            cardMyDecks.setEnabled(true);
            cardPublicDecks.setEnabled(true);
            backButton.setEnabled(true);
        }
    }


    // Tải cấp độ ghi nhớ (TOÀN BỘ từ vựng của user)
    private void loadMemoryLevels() {
        startApiCall(); // Bắt đầu đếm API call

        apiService.getMemoryLevels(currentUserId).enqueue(new Callback<MemoryLevelsResponse>() {
            @Override
            public void onResponse(Call<MemoryLevelsResponse> call, Response<MemoryLevelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MemoryLevelsResponse levels = response.body();
                    textMemoryLevel0.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel0()));
                    textMemoryLevel1.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel1()));
                    textMemoryLevel2.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel2()));
                    textMemoryLevel3.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel3()));
                    textMemoryLevel4.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel4()));
                    textMemoryLevelMastered.setText(String.format(Locale.getDefault(), "%d words", levels.getMastered()));
                } else {
                    Log.e(TAG, "Failed to load global memory levels: " + response.code() + " - " + response.message());
                    // Set default text on error
                    textMemoryLevel0.setText("- words"); textMemoryLevel1.setText("- words");
                    textMemoryLevel2.setText("- words"); textMemoryLevel3.setText("- words");
                    textMemoryLevel4.setText("- words"); textMemoryLevelMastered.setText("- words");
                }
                finishApiCall(); // Hoàn thành API call
            }

            @Override
            public void onFailure(Call<MemoryLevelsResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading global memory levels", t);
                // Set default text on error
                textMemoryLevel0.setText("- words"); textMemoryLevel1.setText("- words");
                textMemoryLevel2.setText("- words"); textMemoryLevel3.setText("- words");
                textMemoryLevel4.setText("- words"); textMemoryLevelMastered.setText("- words");
                finishApiCall(); // Hoàn thành API call
            }
        });
    }

    // Tải số lượng từ đến hạn ôn tập (TOÀN BỘ từ vựng của user)
    private void loadDueReviewCount() {
        startApiCall(); // Bắt đầu đếm API call

        apiService.getDueReviewCount(currentUserId).enqueue(new Callback<DueReviewCountResponse>() {
            @Override
            public void onResponse(Call<DueReviewCountResponse> call, Response<DueReviewCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DueReviewCountResponse reviewCount = response.body();
                    int count = reviewCount.getCount();
                    textDueReviewCount.setText(String.format(Locale.getDefault(), "%d words", count));
                    // Enable/Disable review button based on count AFTER loading finishes
                    // This logic is now inside showLoading(false)
                } else {
                    Log.e(TAG, "Failed to load due review count: " + response.code() + " - " + response.message());
                    textDueReviewCount.setText("? words");
                    // This logic is now inside showLoading(false)
                }
                finishApiCall(); // Hoàn thành API call
            }

            @Override
            public void onFailure(Call<DueReviewCountResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading due review count", t);
                textDueReviewCount.setText("? words");
                // This logic is now inside showLoading(false)
                finishApiCall(); // Hoàn thành API call
            }
        });
    }

    // Optional: Method to load deck counts if you added those TextViews
    /*
    private void loadDeckCounts() {
         startApiCall();
         // You'll need new API endpoints and DTOs for this
         // Example:
         // apiService.getUserDeckCounts(CURRENT_USER_ID).enqueue(new Callback<DeckCountsResponse>() {
         //     @Override
         //     public void onResponse(...) {
         //         if (response.isSuccessful() && response.body() != null) {
         //             DeckCountsResponse counts = response.body();
         //             // textMyDecksCount.setText(String.format(Locale.getDefault(), "%d decks", counts.getMyDecksCount()));
         //             // textPublicDecksCount.setText(String.format(Locale.getDefault(), "%d decks", counts.getPublicDecksCount()));
         //         } else {
         //             Log.e(TAG, "Failed to load deck counts: " + response.code() + " - " + response.message());
         //             // textMyDecksCount.setText("- decks");
         //             // textPublicDecksCount.setText("- decks");
         //         }
         //         finishApiCall();
         //     }
         //     @Override
         //     public void onFailure(...) {
         //         Log.e(TAG, "Network error loading deck counts", t);
         //         // textMyDecksCount.setText("- decks");
         //         // textPublicDecksCount.setText("- decks");
         //         finishApiCall();
         //     }
         // });
    }
    */


    // Method to start the review session (calls GET /api/learnings/review/user/{userId})
    private void startReview() {
        // The buttonReviewNow is enabled/disabled by showLoading(false) based on count.
        if (!buttonReviewNow.isEnabled()) {
            Toast.makeText(this, "No words are currently due for review.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khởi tạo ReviewActivity và truyền user ID
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra(ReviewActivity.USER_ID_EXTRA, currentUserId); // Pass user ID
        startActivity(reviewIntent);
    }
}