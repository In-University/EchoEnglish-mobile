package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest; // Assuming this DTO exists
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
// import java.util.Random; // Not used in this class, can remove

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Game1Activity extends AppCompatActivity {

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game1Activity";

    // TODO: Replace with actual user ID from login/session management
    private static final long CURRENT_USER_ID = 27L; // Example user ID

    private TextView textGameProgress;
    private ImageView imageGameWord;
    private TextView textGameDefinition;
    private LinearLayout layoutAnswer; // Khu vực chứa câu trả lời
    private FlexboxLayout layoutChoices; // Khu vực chứa lựa chọn
    private Button buttonSkip;
    private Button buttonCheck; // Nút kiểm tra

    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String currentCorrectWord;
    // Lưu trữ các TextView của lựa chọn và trạng thái của chúng
    private List<TextView> choiceTextViews = new ArrayList<>();
    private List<Character> currentAnswerChars = new ArrayList<>(); // Chữ cái người dùng đã chọn
    private List<TextView> answerTextViews = new ArrayList<>(); // TextViews trong khu vực trả lời

    private ApiService apiService; // Add ApiService instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);
        setTitle("Game: Sắp xếp chữ");

        // Ánh xạ View
        textGameProgress = findViewById(R.id.textGame1Progress);
        imageGameWord = findViewById(R.id.imageGame1Word);
        textGameDefinition = findViewById(R.id.textGame1Definition);
        layoutAnswer = findViewById(R.id.layoutGame1Answer);
        layoutChoices = findViewById(R.id.layoutGame1Choices);
        buttonSkip = findViewById(R.id.buttonGame1Skip);
        buttonCheck = findViewById(R.id.buttonGame1Check);

        // Khởi tạo ApiService (assuming ApiClient is how you get it)
        apiService = ApiClient.getApiService();

        // Nhận dữ liệu
        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        if (vocabularyList == null || vocabularyList.isEmpty()) {
            Log.e(TAG, "No vocabulary list received for game.");
            Toast.makeText(this, "Lỗi tải dữ liệu game.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xáo trộn danh sách câu hỏi (tùy chọn)
        Collections.shuffle(vocabularyList);

        // Bắt đầu game
        loadQuestion();

        buttonSkip.setOnClickListener(v -> {
            // Khi skip, ghi lại là quên
            if (currentQuestionIndex < vocabularyList.size()) {
                // Lấy vocab ID của câu hỏi hiện tại TRƯỚC khi chuyển câu
                long vocabId = vocabularyList.get(currentQuestionIndex).getId();
                // Ghi lại là quên (isRemembered = false)
                recordLearningProgress(vocabId, false);
            }
            goToNextQuestion(); // Chuyển sang câu hỏi tiếp theo
        });
        buttonCheck.setOnClickListener(v -> checkAnswer());
    }

    // Hiển thị câu hỏi hiện tại
    private void loadQuestion() {
        if (currentQuestionIndex >= vocabularyList.size()) {
            endGame();
            return;
        }

        VocabularyResponse currentVocab = vocabularyList.get(currentQuestionIndex);
        currentCorrectWord = currentVocab.getWord().toUpperCase(Locale.ROOT); // Lưu từ đúng (viết hoa)

        // Cập nhật UI
        textGameProgress.setText(String.format(Locale.getDefault(), "Câu %d / %d",
                currentQuestionIndex + 1, vocabularyList.size()));

        // Hiển thị ảnh (nếu có)
        if (currentVocab.getImageUrl() != null && !currentVocab.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentVocab.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageGameWord);
            imageGameWord.setVisibility(View.VISIBLE);
        } else {
            imageGameWord.setVisibility(View.GONE); // Ẩn nếu không có ảnh
        }

        // Hiển thị nghĩa (ví dụ)
        textGameDefinition.setText("Nghĩa: " + currentVocab.getDefinition());

        // Reset khu vực trả lời và lựa chọn
        layoutAnswer.removeAllViews();
        layoutChoices.removeAllViews();
        choiceTextViews.clear();
        currentAnswerChars.clear();
        answerTextViews.clear();
        buttonCheck.setVisibility(View.GONE); // Ẩn nút Check ban đầu
        buttonCheck.setEnabled(true); // Re-enable check button for the new question
        buttonSkip.setEnabled(true); // Re-enable skip button

        // Tạo các ô chữ cái cho khu vực trả lời (rỗng)
        for (int i = 0; i < currentCorrectWord.length(); i++) {
            TextView answerBox = createLetterTextView(R.style.GameLetterBox_Answer, ' '); // Ký tự rỗng
            answerBox.setBackgroundResource(R.drawable.bg_game_letter_answer); // Đặt background rỗng
            layoutAnswer.addView(answerBox);
            answerTextViews.add(answerBox);

            // Listener để trả chữ về khu lựa chọn
            int finalI = i; // Index của ô trả lời
            answerBox.setOnClickListener(v -> returnLetterToChoices(finalI));
        }

        // Tạo các ô chữ cái lựa chọn (xáo trộn)
        List<Character> shuffledChars = shuffleString(currentCorrectWord);
        for (char c : shuffledChars) {
            TextView choiceBox = createLetterTextView(R.style.GameLetterBox_Choice, c);
            choiceBox.setOnClickListener(v -> selectLetter(choiceBox));
            layoutChoices.addView(choiceBox);
            choiceTextViews.add(choiceBox);
        }
    }

    // Tạo một TextView cho ô chữ cái
    private TextView createLetterTextView(int styleResId, char letter) {
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView textView = (TextView) inflater.inflate(R.layout.item_game1_letter, null, false);

        if (letter != ' ') {
            textView.setText(String.valueOf(letter));
        } else {
            textView.setText(""); // Ensure empty for placeholder
        }

        // Set margin programmatically
        int marginInDp = 4; // hoặc 6 tùy bạn muốn cách xa nhiều hay ít
        int marginInPx = (int) (marginInDp * getResources().getDisplayMetrics().density);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx); // Set đều 4 phía
        textView.setLayoutParams(params);

        return textView;
    }


    // Xáo trộn chữ cái trong từ
    private List<Character> shuffleString(String input) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters);
        return characters;
    }

    // When the user selects a letter from the choices area
    private void selectLetter(TextView choiceTextView) {
        // Find the first empty answer box
        int targetAnswerIndex = -1;
        for (int i = 0; i < answerTextViews.size(); i++) {
            // Check if the text is effectively empty after trimming whitespace
            if (answerTextViews.get(i).getText().toString().trim().isEmpty()) {
                targetAnswerIndex = i;
                break;
            }
        }

        if (targetAnswerIndex != -1) {
            char selectedChar = choiceTextView.getText().charAt(0);

            // Update the target answer box
            TextView targetAnswerBox = answerTextViews.get(targetAnswerIndex);
            targetAnswerBox.setText(String.valueOf(selectedChar));
            // Set background indicating it now has a letter (optional)
            targetAnswerBox.setBackgroundResource(R.drawable.bg_game_letter_selected_answer);


            // Store the selected char in the internal list (ensure list size matches boxes)
            // Fill with nulls up to the required size if needed
            while (currentAnswerChars.size() <= targetAnswerIndex) {
                currentAnswerChars.add(null);
            }
            currentAnswerChars.set(targetAnswerIndex, selectedChar);

            // Store a reference to the original choice TextView in the answer box's tag
            targetAnswerBox.setTag(choiceTextView);


            // Hide the selected choice TextView
            choiceTextView.setVisibility(View.INVISIBLE);
            choiceTextView.setClickable(false); // Make it not clickable when hidden

            // Check if all answer boxes are filled
            checkIfAnswerComplete();
        }
    }

    // When the user clicks an answer box to return the letter to choices
    private void returnLetterToChoices(int answerIndex) {
        if (answerIndex >= 0 && answerIndex < answerTextViews.size()) {
            TextView answerBox = answerTextViews.get(answerIndex);
            // Get the original choice TextView from the tag
            Object tag = answerBox.getTag();

            // Only proceed if the answer box has a letter/tag associated
            if (tag instanceof TextView && !answerBox.getText().toString().trim().isEmpty()) {
                TextView originalChoiceBox = (TextView) tag;

                // Show the original choice TextView again
                originalChoiceBox.setVisibility(View.VISIBLE);
                originalChoiceBox.setClickable(true); // Make it clickable again

                // Clear the answer box text and reset background/tag
                answerBox.setText("");
                answerBox.setBackgroundResource(R.drawable.bg_game_letter_answer); // Reset to empty state background
                answerBox.setTag(null); // Clear the tag

                // Remove the character from the internal answer list at the correct position
                if (answerIndex < currentAnswerChars.size()) {
                    currentAnswerChars.set(answerIndex, null); // Or remove it if structure is different
                }

                // Hide the Check button if the answer is no longer complete
                buttonCheck.setVisibility(View.GONE);
            }
        }
    }

    // Check if all answer boxes have been filled
    private void checkIfAnswerComplete() {
        boolean complete = true;
        // A simple check is to see if any answer TextView is still empty
        for (TextView tv : answerTextViews) {
            if (tv.getText().toString().trim().isEmpty()) {
                complete = false;
                break;
            }
        }

        if (complete) {
            // Optionally build the full string here for logging
            // StringBuilder currentAnswer = new StringBuilder();
            // for (TextView tv : answerTextViews) {
            //     currentAnswer.append(tv.getText().toString());
            // }
            // Log.d(TAG, "Answer complete: " + currentAnswer.toString());
            buttonCheck.setVisibility(View.VISIBLE); // Show the Check button
        } else {
            buttonCheck.setVisibility(View.GONE); // Hide the Check button
        }
    }


    // Check the user's answer
    private void checkAnswer() {
        StringBuilder userAnswerBuilder = new StringBuilder();
        for (TextView tv : answerTextViews) {
            userAnswerBuilder.append(tv.getText().toString());
        }
        String userAnswer = userAnswerBuilder.toString().toUpperCase(Locale.ROOT);

        boolean isCorrect = userAnswer.equals(currentCorrectWord);

        disableInteraction();

        // Lấy vocab ID của câu hỏi hiện tại
        long currentVocabId = vocabularyList.get(currentQuestionIndex).getId();


        if (isCorrect) {
            score++;
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
            // Ghi lại là nhớ (isRemembered = true)
            recordLearningProgress(currentVocabId, true);

        } else {
            Toast.makeText(this, "Sai rồi! Đáp án: " + currentCorrectWord, Toast.LENGTH_LONG).show();
            // Ghi lại là quên (isRemembered = false)
            recordLearningProgress(currentVocabId, false);
            // Optionally highlight the correct answer string or display it more prominently
        }

        // Show feedback (correct/incorrect color) after determining isCorrect
        for (TextView tv : answerTextViews) {
            tv.setBackgroundColor(ContextCompat.getColor(this,
                    isCorrect ? R.color.correct_green : R.color.incorrect_red));
            tv.setClickable(false); // Disable returning letters
        }


        // Wait a bit then go to the next question
        new Handler(Looper.getMainLooper()).postDelayed(this::goToNextQuestion, 2000); // Wait 2 seconds
    }

    // Helper to disable all interactive elements after checking
    private void disableInteraction() {
        for (TextView tv : answerTextViews) {
            tv.setClickable(false);
        }
        for (TextView tv : choiceTextViews) {
            tv.setClickable(false); // Disable selecting letters
        }
        buttonCheck.setEnabled(false);
        buttonSkip.setEnabled(false);
    }


    // Go to the next question
    private void goToNextQuestion() {
        currentQuestionIndex++;
        loadQuestion(); // Load the new question (will re-enable buttons)
    }

    // End the game
    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(ResultActivity.EXTRA_GAME_TYPE, "Game 1: Sắp xếp chữ"); // Send game type
        startActivity(intent);
        finish(); // Close the game activity
    }

    // --- New method to record learning progress ---
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
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.w(TAG, "Ghi nhận học tập thất bại cho vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message() + " Body: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi ghi nhận học tập cho vocab ID: " + vocabularyId, t);
            }
        });
    }
    // ----------------------------------------------
}
