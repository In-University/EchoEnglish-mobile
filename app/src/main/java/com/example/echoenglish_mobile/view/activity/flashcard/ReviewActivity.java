package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse; // Import DTO review

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";
    public static final String USER_ID_EXTRA = "USER_ID";

    // TODO: Get actual user ID from login/session management
    private long userId = -1L; // Will be set from Intent

    // Views
    private Toolbar toolbar;
    private TextView textReviewProgress;
    private TextView textReviewWord;
    private TextView textReviewPhonetic;
    private ImageView imageReviewVocabulary;
    private TextView textReviewDefinition;
    private TextView textReviewExample;
    private TextView textReviewMemoryLevel; // Optional UI
    private TextView textReviewFlashcardInfo; // Optional UI
    private Button buttonForgetReview;
    private Button buttonRememberReview;
    private ProgressBar progressBarReview; // For loading the list
    private TextView textReviewEmptyMessage; // Message when list is empty
    private View cardReviewVocabulary; // The card view displaying vocabulary info
    private LinearLayout layoutReviewButtons; // Layout containing Remember/Forget buttons


    // Logic
    private ApiService apiService;
    private List<VocabularyReviewResponse> dueWordsList; // Danh sách từ cần ôn tập
    private int currentIndex = 0; // Index của từ hiện tại trong danh sách

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        findViews();
        setupToolbar();
        apiService = ApiClient.getApiService();

        // Get user ID from Intent
        userId = getIntent().getLongExtra(USER_ID_EXTRA, -1L);
        if (userId == -1L) {
            Log.e(TAG, "Invalid User ID received.");
            Toast.makeText(this, "Lỗi: Không thể tải dữ liệu ôn tập.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load the list of words due for review
        loadDueWordsForReview(userId);

        // Set listeners for Remember and Forget buttons
        buttonRememberReview.setOnClickListener(v -> handleRememberForgetClick(true));
        buttonForgetReview.setOnClickListener(v -> handleRememberForgetClick(false));
    }

    // Find all views
    private void findViews() {
        toolbar = findViewById(R.id.toolbarReview);
        textReviewProgress = findViewById(R.id.textReviewProgress);
        textReviewWord = findViewById(R.id.textReviewWord);
        textReviewPhonetic = findViewById(R.id.textReviewPhonetic);
        imageReviewVocabulary = findViewById(R.id.imageReviewVocabulary);
        textReviewDefinition = findViewById(R.id.textReviewDefinition);
        textReviewExample = findViewById(R.id.textReviewExample);
        textReviewMemoryLevel = findViewById(R.id.textReviewMemoryLevel);
        textReviewFlashcardInfo = findViewById(R.id.textReviewFlashcardInfo);
        buttonForgetReview = findViewById(R.id.buttonForgetReview);
        buttonRememberReview = findViewById(R.id.buttonRememberReview);
        progressBarReview = findViewById(R.id.progressBarReview);
        textReviewEmptyMessage = findViewById(R.id.textReviewEmptyMessage);
        cardReviewVocabulary = findViewById(R.id.cardReviewVocabulary); // The main card
        layoutReviewButtons = findViewById(R.id.layoutReviewButtons); // The buttons layout
    }

    // Setup Toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ôn tập từ vựng");
        }
    }

    // Handle back button on Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Show/hide loading indicator
    private void showLoading(boolean isLoading) {
        progressBarReview.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Hide other content while loading
        cardReviewVocabulary.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        layoutReviewButtons.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        textReviewProgress.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        textReviewEmptyMessage.setVisibility(View.GONE); // Hide empty message when loading
    }


    // Load the list of words due for review from API
    private void loadDueWordsForReview(long userId) {
        showLoading(true);

        apiService.getDueVocabulariesForReview(userId).enqueue(new Callback<List<VocabularyReviewResponse>>() {
            @Override
            public void onResponse(Call<List<VocabularyReviewResponse>> call, Response<List<VocabularyReviewResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    dueWordsList = response.body();
                    if (dueWordsList.isEmpty()) {
                        // Show empty message and hide other UI
                        textReviewEmptyMessage.setVisibility(View.VISIBLE);
                        cardReviewVocabulary.setVisibility(View.GONE);
                        layoutReviewButtons.setVisibility(View.GONE);
                        textReviewProgress.setVisibility(View.GONE);
                        Toast.makeText(ReviewActivity.this, "Không có từ nào cần ôn tập lúc này.", Toast.LENGTH_LONG).show();
                    } else {
                        // Display the first word
                        currentIndex = 0; // Start from the beginning
                        displayCurrentWord();
                    }
                } else {
                    Log.e(TAG, "Lỗi tải từ cần ôn tập: " + response.code() + " - " + response.message());
                    Toast.makeText(ReviewActivity.this, "Lỗi tải danh sách ôn tập.", Toast.LENGTH_LONG).show();
                    // Show error state
                    textReviewEmptyMessage.setText("Lỗi tải dữ liệu.");
                    textReviewEmptyMessage.setVisibility(View.VISIBLE);
                    cardReviewVocabulary.setVisibility(View.GONE);
                    layoutReviewButtons.setVisibility(View.GONE);
                    textReviewProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<VocabularyReviewResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi tải từ cần ôn tập", t);
                Toast.makeText(ReviewActivity.this, "Lỗi mạng khi tải danh sách ôn tập.", Toast.LENGTH_LONG).show();
                // Show network error state
                textReviewEmptyMessage.setText("Lỗi kết nối mạng.");
                textReviewEmptyMessage.setVisibility(View.VISIBLE);
                cardReviewVocabulary.setVisibility(View.GONE);
                layoutReviewButtons.setVisibility(View.GONE);
                textReviewProgress.setVisibility(View.GONE);
            }
        });
    }

    // Display the word at the current index
    private void displayCurrentWord() {
        if (dueWordsList == null || currentIndex >= dueWordsList.size()) {
            endReviewSession();
            return;
        }

        VocabularyReviewResponse currentWord = dueWordsList.get(currentIndex);

        // Update progress text
        textReviewProgress.setText(String.format(Locale.getDefault(), "%d / %d",
                currentIndex + 1, dueWordsList.size()));

        // Update UI with word details
        textReviewWord.setText(currentWord.getWord());
        textReviewPhonetic.setText(currentWord.getPhonetic());
        textReviewDefinition.setText(currentWord.getDefinition());
        textReviewExample.setText(currentWord.getExample());

        // Load image (if available)
        if (currentWord.getImageUrl() != null && !currentWord.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentWord.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageReviewVocabulary);
            imageReviewVocabulary.setVisibility(View.VISIBLE);
        } else {
            imageReviewVocabulary.setVisibility(View.GONE);
        }

        // Update optional fields
        textReviewMemoryLevel.setText(String.format(Locale.getDefault(), "Cấp độ ghi nhớ: Level %d", currentWord.getRememberCount()));

        // ** XÓA HOẶC COMMENT CÁC DÒNG NÀY **
        // if (currentWord.getFlashcardName() != null && !currentWord.getFlashcardName().isEmpty()) {
        //     textReviewFlashcardInfo.setText(String.format(Locale.getDefault(), "Thuộc bộ thẻ: %s", currentWord.getFlashcardName()));
        //     textReviewFlashcardInfo.setVisibility(View.VISIBLE);
        // } else {
        //      textReviewFlashcardInfo.setVisibility(View.GONE);
        // }

        // ** ĐẢM BẢO TextView này luôn ẩn nếu không dùng đến **
        // Nếu bạn đã ánh xạ textReviewFlashcardInfo, hãy đảm bảo nó luôn ẩn hoặc remove nó khỏi layout nếu không cần
        if (textReviewFlashcardInfo != null) { // Kiểm tra null an toàn hơn
            textReviewFlashcardInfo.setVisibility(View.GONE);
        }


        // Enable buttons for interaction
        layoutReviewButtons.setVisibility(View.VISIBLE);
        buttonRememberReview.setEnabled(true);
        buttonForgetReview.setEnabled(true);
    }

    // Handle click on Remember or Forget button
    private void handleRememberForgetClick(boolean isRemembered) {
        if (dueWordsList == null || currentIndex >= dueWordsList.size()) {
            // Should not happen if buttons are disabled correctly
            return;
        }

        // Disable buttons while processing
        buttonRememberReview.setEnabled(false);
        buttonForgetReview.setEnabled(false);

        VocabularyReviewResponse currentWord = dueWordsList.get(currentIndex);

        // Record learning progress via API
        recordLearningApiCall(currentWord.getId(), isRemembered);

        // Move to the next word after a short delay (allowing API call to start)
        // A delay provides a smoother transition and doesn't block the UI immediately.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            currentIndex++; // Move to next word index
            displayCurrentWord(); // Display the next word (or end session)
        }, 300); // Delay for 300 milliseconds
    }

    // Call API to record learning (Remember/Forget)
    private void recordLearningApiCall(long vocabularyId, boolean isRemembered) {
        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(userId);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhận ôn tập thành công cho vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                    // Optional: Show a small confirmation message
                    // Toast.makeText(ReviewActivity.this, isRemembered ? "Đã nhớ!" : "Đã quên.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Ghi nhận ôn tập thất bại cho vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message());
                    // Optional: Show an error message
                    // Toast.makeText(ReviewActivity.this, "Lỗi ghi nhận ôn tập.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi ghi nhận ôn tập cho vocab ID: " + vocabularyId, t);
                // Optional: Show an error message
                // Toast.makeText(ReviewActivity.this, "Lỗi mạng khi ghi nhận.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // End the review session
    private void endReviewSession() {
        Log.d(TAG, "Review session ended.");
        Toast.makeText(this, "Hoàn thành buổi ôn tập!", Toast.LENGTH_LONG).show();
        // Optionally, navigate back or show a summary screen
        finish(); // Close the ReviewActivity
    }

    // Optional: Handle back press during review (e.g., show confirmation dialog)
    @Override
    public void onBackPressed() {
        // You might want to show a dialog like "Are you sure you want to exit? Progress will be lost."
        // For now, just call super.onBackPressed()
        super.onBackPressed();
    }
}