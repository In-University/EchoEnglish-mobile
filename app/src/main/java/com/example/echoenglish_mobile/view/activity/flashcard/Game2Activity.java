package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest; // Assuming this DTO exists
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Game2Activity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game2Activity";

    private static final long CURRENT_USER_ID = 27L; // Example user ID

    // New View references
    private LinearLayout loadingContainer; // Or ConstraintLayout if you used CL
    private ConstraintLayout gameContent; // Or LinearLayout if you wrapped in LL

    private TextView textGameProgress;
    private ImageButton buttonPlaySound;
    private GridLayout gridAnswers;

    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private VocabularyResponse currentCorrectVocab;
    private List<VocabularyResponse> currentOptions = new ArrayList<>();

    private TextToSpeech tts;
    private boolean ttsInitialized = false;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
        setTitle("Game: Nghe & Chọn Ảnh");

        // Initialize loading and game content containers
        loadingContainer = findViewById(R.id.loadingContainer); // Find the loading layout
        gameContent = findViewById(R.id.gameContent); // Find the game content layout

        // Initially show loading, hide game content
        loadingContainer.setVisibility(View.VISIBLE);
        gameContent.setVisibility(View.INVISIBLE);


        // Initialize other game views (these are inside gameContent)
        textGameProgress = gameContent.findViewById(R.id.textGame2Progress); // Use gameContent to find
        buttonPlaySound = gameContent.findViewById(R.id.buttonGame2PlaySound); // Use gameContent to find
        gridAnswers = gameContent.findViewById(R.id.gridGame2Answers); // Use gameContent to find


        // Khởi tạo TextToSpeech (Listener will handle visibility)
        tts = new TextToSpeech(this, this);
        buttonPlaySound.setEnabled(false); // Disable sound button initially until TTS is ready


        // Khởi tạo ApiService
        apiService = ApiClient.getApiService();

        // Nhận dữ liệu
        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        // Check vocab list size early, but don't load game yet
        if (vocabularyList == null || vocabularyList.size() < 4) {
            Log.e(TAG, "Not enough vocabularies received for game (need at least 4).");
            // Show error and finish immediately, no need to wait for TTS
            Toast.makeText(this, "Không đủ từ vựng để chơi game này.", Toast.LENGTH_LONG).show();
            finish();
            return; // Exit onCreate
        }

        Collections.shuffle(vocabularyList); // Xáo trộn danh sách từ vựng

        // Set up listeners (can do this even when game content is invisible)
        buttonPlaySound.setOnClickListener(v -> speakWord());

        // DO NOT call loadQuestion() here. It will be called in onInit().
    }

    // Xử lý sau khi TTS được khởi tạo
    @Override
    public void onInit(int status) {
        // Run on the main thread because UI updates should be there
        runOnUiThread(() -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS Language not supported");
                    Toast.makeText(this, "Ngôn ngữ TTS (tiếng Anh US) không được hỗ trợ.", Toast.LENGTH_LONG).show(); // Changed to LONG
                    // Handle the case where TTS is ready but language isn't
                    // Maybe show game but disable sound features, or exit
                    handleTTSInitializationFailure("Ngôn ngữ không được hỗ trợ.");
                } else {
                    ttsInitialized = true;
                    buttonPlaySound.setEnabled(true); // Enable sound button

                    // TTS is ready, hide loading and show game content
                    loadingContainer.setVisibility(View.GONE);
                    gameContent.setVisibility(View.VISIBLE);

                    // Now load the first question and trigger speech
                    loadQuestion(); // Load the first question and auto-speak

                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status: " + status);
                Toast.makeText(this, "Khởi tạo công cụ TTS thất bại.", Toast.LENGTH_LONG).show(); // Changed to LONG
                handleTTSInitializationFailure("Khởi tạo TTS thất bại.");
            }
        });
    }

    // Handle what happens when TTS initialization fails
    private void handleTTSInitializationFailure(String message) {
        // Still hide loading, but maybe show an error screen instead of the game
        loadingContainer.setVisibility(View.GONE);
        // Option 1: Show a simple error message and finish
        TextView errorText = new TextView(this);
        errorText.setText("Không thể sử dụng âm thanh. Vui lòng kiểm tra cài đặt TTS trên thiết bị.\n" + message);
        errorText.setGravity(android.view.Gravity.CENTER);
        errorText.setPadding(16, 16, 16, 16);
        // Replace gameContent with errorText or add errorText on top
        setContentView(errorText); // Simple way to replace the content
        // You might also want to finish() after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 5000); // Auto-close after 5 seconds


        // Option 2: Show game but disable all audio features
        // This requires more UI logic to hide/disable buttons related to sound
        // gameContent.setVisibility(View.VISIBLE); // Still show UI
        // buttonPlaySound.setVisibility(View.GONE); // Hide sound button
        // Consider if the game is playable without sound

        // For this example, Option 1 (showing error and finishing) is simpler.
    }


    // ... (Rest of loadQuestion, speakWord, checkAnswer, etc. methods remain mostly the same) ...

    // Hiển thị câu hỏi và các lựa chọn - unchanged, except now it's called from onInit
    private void loadQuestion() {
        if (currentQuestionIndex >= vocabularyList.size()) {
            endGame();
            return;
        }

        currentCorrectVocab = vocabularyList.get(currentQuestionIndex);
        currentOptions.clear(); // Xóa các lựa chọn cũ
        gridAnswers.removeAllViews(); // Xóa các view cũ trong grid

        // Cập nhật progress text
        textGameProgress.setText(String.format(Locale.getDefault(), "Câu %d / %d",
                currentQuestionIndex + 1, vocabularyList.size()));

        // Chuẩn bị 4 lựa chọn (1 đúng, 3 sai)
        currentOptions.add(currentCorrectVocab); // Thêm đáp án đúng

        // Lấy 3 đáp án sai ngẫu nhiên từ phần còn lại của danh sách
        List<VocabularyResponse> wrongOptionsPool = new ArrayList<>(vocabularyList);

        // Remove the current correct vocabulary safely.
        boolean removed = false;
        for(int i = 0; i < wrongOptionsPool.size(); i++) {
            if (wrongOptionsPool.get(i).getId().equals(currentCorrectVocab.getId())) {
                wrongOptionsPool.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed && vocabularyList.contains(currentCorrectVocab)) {
            Log.w(TAG, "Could not remove currentCorrectVocab from wrongOptionsPool by ID: " + currentCorrectVocab.getId());
        }

        Collections.shuffle(wrongOptionsPool);

        int neededWrong = 3;
        int addedWrong = 0;
        for (int i = 0; i < wrongOptionsPool.size() && addedWrong < neededWrong; i++) {
            VocabularyResponse wrongOption = wrongOptionsPool.get(i);
            boolean alreadyAdded = false;
            for (VocabularyResponse option : currentOptions) {
                if (option.getId().equals(wrongOption.getId())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                currentOptions.add(wrongOption);
                addedWrong++;
            }
        }

        if (currentOptions.size() < 4) {
            Log.e(TAG, "Could not generate 4 unique options! Needed " + 4 + ", got " + currentOptions.size());
            Toast.makeText(this, "Lỗi tạo đáp án. Không đủ từ vựng duy nhất.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Collections.shuffle(currentOptions);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyResponse option : currentOptions) {
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_game2_answer, gridAnswers, false);

            ImageView imageView = cardView.findViewById(R.id.imageGame2Answer);
            TextView textView = cardView.findViewById(R.id.textGame2AnswerDefinition);

            textView.setText(option.getDefinition());
            Glide.with(this)
                    .load(option.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageView);

            cardView.setTag(option);

            cardView.setOnClickListener(v -> {
                VocabularyResponse selected = (VocabularyResponse) v.getTag();
                if (selected != null) {
                    checkAnswer(selected, (CardView) v);
                } else {
                    Log.e(TAG, "Failed to retrieve VocabularyResponse from tag!");
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin));
            cardView.setLayoutParams(params);

            gridAnswers.addView(cardView);
        }

        // Tự động phát âm thanh khi câu hỏi được tải
        speakWord();
        buttonPlaySound.setEnabled(ttsInitialized);
    }


    // Phát âm thanh từ tiếng Anh - unchanged logic
    private void speakWord() {
        if (ttsInitialized && currentCorrectVocab != null) {
            String wordToSpeak = currentCorrectVocab.getWord();
            if (wordToSpeak != null && !wordToSpeak.isEmpty()) {
                tts.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "Speak_" + currentCorrectVocab.getId());
            }
        } else if (!ttsInitialized) {
            // This toast should ideally not be shown often with the new flow
            // because speakWord is called after ttsInitialized = true
            Log.w(TAG, "speakWord called before TTS initialized."); // Use Log.w instead of toast here?
            // Or remove the else if block if you are sure it won't be called
        }
    }

    private void highlightCorrectAnswer() {
        for (int i = 0; i < gridAnswers.getChildCount(); i++) {
            View child = gridAnswers.getChildAt(i);
            if (child instanceof MaterialCardView) {
                MaterialCardView cardView = (MaterialCardView) child;
                VocabularyResponse option = (VocabularyResponse) cardView.getTag(); // Get option from tag
                if (option != null && option.getId().equals(currentCorrectVocab.getId())) {
                    cardView.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.card_stroke_width)); // Use a dimen resource
                    cardView.setStrokeColor(ContextCompat.getColor(this, R.color.highlight_blue));
                    break; // Found the correct one
                }
            }
        }
    }

    // Kiểm tra đáp án người dùng chọn
    private void checkAnswer(VocabularyResponse selectedOption, CardView selectedCardView) {
        disableAnswerButtons();
        buttonPlaySound.setEnabled(false);

        boolean isCorrect = selectedOption.getId().equals(currentCorrectVocab.getId());

        // Lấy vocab ID của câu hỏi hiện tại (đáp án đúng)
        long currentVocabId = currentCorrectVocab.getId();


        if (isCorrect) {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
            score++;
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
            // Ghi lại là nhớ (isRemembered = true)
            recordLearningProgress(currentVocabId, true);

        } else {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.incorrect_red));
            Toast.makeText(this, "Sai rồi!", Toast.LENGTH_SHORT).show();
            highlightCorrectAnswer();
            // Ghi lại là quên (isRemembered = false)
            recordLearningProgress(currentVocabId, false);
        }

        // Chờ một chút rồi chuyển câu
        new Handler(Looper.getMainLooper()).postDelayed(this::goToNextQuestion, 1500);
    }

    // --- Modify recordLearningProgress to accept isRemembered ---
    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized.");
            return;
        }

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(CURRENT_USER_ID);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered); // ** GỬI TRẠNG THÁI NHỚ/QUÊN **

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhận học tập thành công cho vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                } else {
                    Log.w(TAG, "Ghi nhận học tập thất bại cho vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi ghi nhận học tập cho vocab ID: " + vocabularyId, t);
            }
        });
    }

    // Helper to disable all card buttons
    private void disableAnswerButtons() {
        for (int i = 0; i < gridAnswers.getChildCount(); i++) {
            View child = gridAnswers.getChildAt(i);
            child.setClickable(false); // Disable click
            // Optional: Change alpha or appearance to indicate disabled state
        }
    }





    // Chuyển câu hỏi tiếp theo
    private void goToNextQuestion() {
        currentQuestionIndex++;
        // Kích hoạt lại nút loa và các nút trả lời cho câu hỏi mới
        buttonPlaySound.setEnabled(ttsInitialized);
        loadQuestion(); // Tải câu hỏi mới (sẽ tự kích hoạt lại nút skip và các card)
    }

    // Kết thúc game
    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(ResultActivity.EXTRA_GAME_TYPE, "Game 2: Nghe & Chọn");
        // You might want to pass the vocabulary list back or just finish
        startActivity(intent);
        finish(); // Close this activity
    }

    // --- New method to record learning progress ---
    private void recordLearningProgress(long vocabularyId) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized.");
            // Handle this error, maybe retry initialization or skip recording
            return;
        }

        LearningRecordRequest request = new LearningRecordRequest();
        // TODO: Get actual user ID, not a hardcoded value
        request.setUserId(CURRENT_USER_ID);
        request.setVocabularyId(vocabularyId);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhớ thành công vocab ID: " + vocabularyId);
                    // Optional: Show a small success message if desired
                } else {
                    Log.w(TAG, "Ghi nhớ thất bại: " + response.code() + " - " + response.message());
                    // Optional: Show a message to the user, or retry
                    // Toast.makeText(Game2Activity.this, "Lỗi ghi nhớ.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi khi ghi nhớ", t);
                // Optional: Show a message to the user, or retry
                // Toast.makeText(Game2Activity.this, "Lỗi mạng khi ghi nhớ.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ----------------------------------------------


@Override
protected void onDestroy() {
    if (tts != null) {
        tts.stop();
        tts.shutdown();
        Log.d(TAG, "TTS Shutdown.");
    }
    super.onDestroy();
}
}