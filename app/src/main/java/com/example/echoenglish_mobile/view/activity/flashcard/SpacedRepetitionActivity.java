package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.core.widget.NestedScrollView; // Import nếu dùng NestedScrollView

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.DueReviewCountResponse;
// import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningProgressResponse; // Không dùng nữa
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.MemoryLevelsResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse; // DTO từ backend

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpacedRepetitionActivity extends AppCompatActivity {

    private static final String TAG = "SpacedRepetitionAct";
    // public static final String FLASHCARD_ID_EXTRA = "FLASHCARD_ID"; // Không nhận flashcardId nữa

    // TODO: Get actual user ID from login/session management
    private static final long CURRENT_USER_ID = 27L; // Example user ID


    private Toolbar toolbar;
    // ** REMOVED Learned UI elements **
    // private TextView textLearnedProgress;
    // private ProgressBar progressBarLearnedCircle;


    // Memory Level TextViews
    private TextView textMemoryLevel0;
    private TextView textMemoryLevel1;
    private TextView textMemoryLevel2;
    private TextView textMemoryLevel3;
    private TextView textMemoryLevel4;
    private TextView textMemoryLevelMastered;

    private TextView textDueReviewCount;
    private Button buttonReviewNow;
    private ProgressBar progressBarSRS; // Main loading indicator
    private View contentLayout; // Layout chứa nội dung (ví dụ: ScrollView)


    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spaced_repetition);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbarSpacedRepetition);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tiến độ học & Ôn tập SRS"); // Đặt tiêu đề
        }

        // ** REMOVED Learned UI element references **

        textMemoryLevel0 = findViewById(R.id.textMemoryLevel0);
        textMemoryLevel1 = findViewById(R.id.textMemoryLevel1);
        textMemoryLevel2 = findViewById(R.id.textMemoryLevel2);
        textMemoryLevel3 = findViewById(R.id.textMemoryLevel3);
        textMemoryLevel4 = findViewById(R.id.textMemoryLevel4);
        textMemoryLevelMastered = findViewById(R.id.textMemoryLevelMastered);

        textDueReviewCount = findViewById(R.id.textDueReviewCount);
        buttonReviewNow = findViewById(R.id.buttonReviewNow);
        progressBarSRS = findViewById(R.id.progressBarSRS);
        contentLayout = findViewById(R.id.contentScrollView); // Ánh xạ layout chứa nội dung


        apiService = ApiClient.getApiService();

        // ** BỎ nhận flashcardId từ Intent **

        // Load global SRS data (Memory Levels and Due Review Count)
        loadMemoryLevels();
        loadDueReviewCount();


        // Set listeners
        buttonReviewNow.setOnClickListener(v -> startReview());

        // Gọi API khi Activity quay trở lại foreground để làm mới dữ liệu
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi Activity trở lại foreground
        Log.d(TAG, "onResume: Refreshing SRS data.");
        loadMemoryLevels();
        loadDueReviewCount();
    }


    // Method to show/hide main loading indicator
    private int loadingApiCount = 0; // Đếm số lượng API đang chạy

    private synchronized void startApiCall() {
        loadingApiCount++;
        showLoading(true);
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBarSRS.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Ẩn/hiện nội dung
        contentLayout.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }


    // ** REMOVED loadLearningProgress method **


    // Tải cấp độ ghi nhớ (TOÀN BỘ từ vựng của user)
    private void loadMemoryLevels() {
        startApiCall(); // Bắt đầu đếm API call

        apiService.getMemoryLevels(CURRENT_USER_ID).enqueue(new Callback<MemoryLevelsResponse>() {
            @Override
            public void onResponse(Call<MemoryLevelsResponse> call, Response<MemoryLevelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MemoryLevelsResponse levels = response.body();
                    textMemoryLevel0.setText(String.format(Locale.getDefault(), "%d từ", levels.getLevel0()));
                    textMemoryLevel1.setText(String.format(Locale.getDefault(), "%d từ", levels.getLevel1()));
                    textMemoryLevel2.setText(String.format(Locale.getDefault(), "%d từ", levels.getLevel2()));
                    textMemoryLevel3.setText(String.format(Locale.getDefault(), "%d từ", levels.getLevel3()));
                    textMemoryLevel4.setText(String.format(Locale.getDefault(), "%d từ", levels.getLevel4()));
                    textMemoryLevelMastered.setText(String.format(Locale.getDefault(), "%d từ", levels.getMastered()));
                } else {
                    Log.e(TAG, "Lỗi tải cấp độ ghi nhớ toàn cầu: " + response.code() + " - " + response.message());
                    // Set default text on error
                    textMemoryLevel0.setText("- từ"); textMemoryLevel1.setText("- từ");
                    textMemoryLevel2.setText("- từ"); textMemoryLevel3.setText("- từ");
                    textMemoryLevel4.setText("- từ"); textMemoryLevelMastered.setText("- từ");
                }
                finishApiCall(); // Hoàn thành API call
            }

            @Override
            public void onFailure(Call<MemoryLevelsResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi tải cấp độ ghi nhớ toàn cầu", t);
                // Set default text on error
                textMemoryLevel0.setText("- từ"); textMemoryLevel1.setText("- từ");
                textMemoryLevel2.setText("- từ"); textMemoryLevel3.setText("- từ");
                textMemoryLevel4.setText("- từ"); textMemoryLevelMastered.setText("- từ");
                finishApiCall(); // Hoàn thành API call
            }
        });
    }

    // Tải số lượng từ đến hạn ôn tập (TOÀN BỘ từ vựng của user)
    private void loadDueReviewCount() {
        startApiCall(); // Bắt đầu đếm API call

        apiService.getDueReviewCount(CURRENT_USER_ID).enqueue(new Callback<DueReviewCountResponse>() {
            @Override
            public void onResponse(Call<DueReviewCountResponse> call, Response<DueReviewCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DueReviewCountResponse reviewCount = response.body();
                    int count = reviewCount.getCount();
                    textDueReviewCount.setText(String.format(Locale.getDefault(), "%d từ", count));
                    // Enable/Disable review button based on count
                    buttonReviewNow.setEnabled(count > 0);
                } else {
                    Log.e(TAG, "Lỗi tải số lượng từ đến hạn ôn tập: " + response.code() + " - " + response.message());
                    textDueReviewCount.setText("? từ");
                    buttonReviewNow.setEnabled(false); // Disable if count fails
                }
                finishApiCall(); // Hoàn thành API call
            }

            @Override
            public void onFailure(Call<DueReviewCountResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi tải số lượng từ đến hạn ôn tập", t);
                textDueReviewCount.setText("? từ");
                buttonReviewNow.setEnabled(false); // Disable if network error
                finishApiCall(); // Hoàn thành API call
            }
        });
    }


    // Method to start the review session (calls GET /api/learnings/review/user/{userId})
    private void startReview() {
        // Kiểm tra xem có từ nào đến hạn không
        if (!buttonReviewNow.isEnabled() || textDueReviewCount.getText().toString().contains("0 từ") || textDueReviewCount.getText().toString().contains("? từ")) {
            Toast.makeText(this, "Chưa có từ nào cần ôn tập.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khởi tạo ReviewActivity và truyền user ID
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra(ReviewActivity.USER_ID_EXTRA, CURRENT_USER_ID); // Pass user ID
        startActivity(reviewIntent);
    }

    // TODO: Create ReviewActivity.java to handle displaying and reviewing due words
    /* Example structure for ReviewActivity:
    public class ReviewActivity extends AppCompatActivity {
        public static final String USER_ID_EXTRA = "USER_ID";
        private long userId;
        private List<VocabularyReviewResponse> dueWordsList;
        private int currentIndex = 0; // Track current word being reviewed
        private ApiService apiService;
        // ... UI elements for displaying a word, definition, example, buttons (Remember/Forget) ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Set layout for review screen
            // Find UI elements
            // Get userId from Intent
            // Initialize ApiService
            loadDueWordsForReview(userId); // Call API to get list of words

            // Set button listeners (Remember/Forget)
            // When Remember/Forget button is clicked:
            //   1. Call recordLearning API with isRemembered = true/false for the current word.
            //   2. Move to the next word in dueWordsList.
            //   3. If no more words, end review session.
        }

        private void loadDueWordsForReview(long userId) {
             // Show loading
             apiService.getDueVocabulariesForReview(userId).enqueue(new Callback<List<VocabularyReviewResponse>>() {
                 @Override
                 public void onResponse(...) {
                     // Hide loading
                     if (response.isSuccessful() && response.body() != null) {
                         dueWordsList = response.body();
                         if (dueWordsList.isEmpty()) {
                             // Show message "No words to review" and finish
                         } else {
                             displayCurrentWord(); // Display the first word
                         }
                     } else {
                         // Handle error
                     }
                 }
                 @Override
                 public void onFailure(...) {
                     // Handle network error
                 }
             });
        }

        private void displayCurrentWord() {
            if (dueWordsList != null && currentIndex < dueWordsList.size()) {
                VocabularyReviewResponse currentWord = dueWordsList.get(currentIndex);
                // Update UI with currentWord details (word, definition, example, etc.)
            } else {
                endReviewSession();
            }
        }

        private void handleRememberForgetClick(boolean isRemembered) {
             if (dueWordsList != null && currentIndex < dueWordsList.size()) {
                 VocabularyReviewResponse currentWord = dueWordsList.get(currentIndex);
                 // Call recordLearning API for currentWord.getId() and isRemembered status
                 // recordLearning(userId, currentWord.getId(), isRemembered); // Implement this method
                 currentIndex++; // Move to next word
                 displayCurrentWord(); // Display next word
             }
        }

        private void recordLearning(long userId, long vocabId, boolean isRemembered) {
             LearningRecordRequest request = new LearningRecordRequest();
             request.setUserId(userId);
             request.setVocabularyId(vocabId);
             request.setIsRemembered(isRemembered);
             apiService.recordLearning(request).enqueue(new Callback<Void>() {
                  // Handle API response (log success/failure) - not critical for UI flow here
             });
        }

        private void endReviewSession() {
            // Show completion message
            // Finish this activity
        }
    }
    */


    // TODO: Implement SkippedWordsActivity to fetch and display skipped words (rememberCount = 0)
    // This activity would call an API like GET /api/learnings/skipped-words/user/{userId}
    // Which would require a new endpoint and service method in backend.

}