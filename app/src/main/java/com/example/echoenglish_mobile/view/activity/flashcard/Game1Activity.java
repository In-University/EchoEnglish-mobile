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
import androidx.core.content.ContextCompat; // Import ContextCompat
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.flexbox.FlexboxLayout; // Import FlexboxLayout

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Game1Activity extends AppCompatActivity {

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    // public static final String EXTRA_FLASHCARD_ID = "FLASHCARD_ID";

    private static final String TAG = "Game1Activity";

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

        buttonSkip.setOnClickListener(v -> goToNextQuestion());
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
        TextView textView = new TextView(this, null, 0, styleResId);
        // Nếu dùng style trong theme:
        // TextView textView = new TextView(new ContextThemeWrapper(this, styleResId));

        // Cài đặt lại layout params nếu cần (FlexboxLayout thường tự xử lý)
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params); // Áp dụng cho Flexbox
        // Hoặc LinearLayout.LayoutParams cho layoutAnswer

        if (letter != ' ') { // Không hiển thị ký tự cách nếu dùng placeholder
            textView.setText(String.valueOf(letter));
        }
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

    // Khi người dùng chọn một chữ cái từ khu lựa chọn
    private void selectLetter(TextView choiceTextView) {
        // Tìm ô trả lời trống đầu tiên
        int targetAnswerIndex = -1;
        for (int i = 0; i < answerTextViews.size(); i++) {
            if (answerTextViews.get(i).getText().toString().trim().isEmpty()) {
                targetAnswerIndex = i;
                break;
            }
        }

        if (targetAnswerIndex != -1) {
            char selectedChar = choiceTextView.getText().charAt(0);

            // Cập nhật ô trả lời
            TextView targetAnswerBox = answerTextViews.get(targetAnswerIndex);
            targetAnswerBox.setText(String.valueOf(selectedChar));
            targetAnswerBox.setBackgroundResource(R.drawable.bg_game_letter_choice); // Đổi background khi có chữ

            // Lưu trữ ký tự và TextView lựa chọn tương ứng vào vị trí trả lời
            // Đảm bảo currentAnswerChars có đủ size
            while (currentAnswerChars.size() <= targetAnswerIndex) {
                currentAnswerChars.add(null); // Thêm placeholder
            }
            currentAnswerChars.set(targetAnswerIndex, selectedChar); // Lưu ký tự
            // Lưu tham chiếu đến choiceTextView đã chọn vào tag của answerBox
            targetAnswerBox.setTag(choiceTextView);


            // Ẩn ô lựa chọn đã chọn
            choiceTextView.setVisibility(View.INVISIBLE);

            // Kiểm tra xem đã điền hết các ô trả lời chưa
            checkIfAnswerComplete();
        }
    }

    // Khi người dùng click vào ô trả lời để trả chữ về
    private void returnLetterToChoices(int answerIndex) {
        if (answerIndex >= 0 && answerIndex < answerTextViews.size()) {
            TextView answerBox = answerTextViews.get(answerIndex);
            // Lấy TextView lựa chọn gốc từ tag
            Object tag = answerBox.getTag();
            if (tag instanceof TextView) {
                TextView originalChoiceBox = (TextView) tag;
                // Hiển thị lại ô lựa chọn
                originalChoiceBox.setVisibility(View.VISIBLE);

                // Xóa chữ và đặt lại background ô trả lời
                answerBox.setText("");
                answerBox.setBackgroundResource(R.drawable.bg_game_letter_answer);
                answerBox.setTag(null); // Xóa tag

                // Xóa ký tự khỏi list trả lời hiện tại
                if (answerIndex < currentAnswerChars.size()) {
                    currentAnswerChars.set(answerIndex, null); // Đặt lại thành null hoặc ký tự đặc biệt
                }


                // Ẩn nút Check nếu câu trả lời chưa hoàn chỉnh
                buttonCheck.setVisibility(View.GONE);
            }
        }
    }

    // Kiểm tra xem tất cả các ô trả lời đã được điền chưa
    private void checkIfAnswerComplete() {
        boolean complete = true;
        StringBuilder currentAnswer = new StringBuilder();
        for (int i = 0; i < answerTextViews.size(); i++) {
            String text = answerTextViews.get(i).getText().toString();
            if (text.trim().isEmpty()) {
                complete = false;
                break;
            }
            currentAnswer.append(text);
        }

        if (complete) {
            Log.d(TAG, "Answer complete: " + currentAnswer.toString());
            buttonCheck.setVisibility(View.VISIBLE); // Hiển thị nút Check
        } else {
            buttonCheck.setVisibility(View.GONE);
        }
    }


    // Kiểm tra câu trả lời
    private void checkAnswer() {
        StringBuilder userAnswerBuilder = new StringBuilder();
        for (TextView tv : answerTextViews) {
            userAnswerBuilder.append(tv.getText().toString());
        }
        String userAnswer = userAnswerBuilder.toString().toUpperCase(Locale.ROOT);

        boolean isCorrect = userAnswer.equals(currentCorrectWord);

        // Hiển thị phản hồi (đúng/sai)
        for (TextView tv : answerTextViews) {
            tv.setBackgroundColor(ContextCompat.getColor(this,
                    isCorrect ? R.color.correct_green : R.color.incorrect_red)); // Định nghĩa màu trong colors.xml
            tv.setClickable(false); // Không cho trả chữ về nữa
        }
        for (TextView tv : choiceTextViews) {
            tv.setClickable(false); // Không cho chọn nữa
        }
        buttonCheck.setEnabled(false);
        buttonSkip.setEnabled(false);


        if (isCorrect) {
            score++;
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sai rồi! Đáp án: " + currentCorrectWord, Toast.LENGTH_SHORT).show();
            // Có thể hiển thị đáp án đúng ở đâu đó
        }

        // Chờ một chút rồi chuyển câu hỏi
        new Handler(Looper.getMainLooper()).postDelayed(this::goToNextQuestion, 1500); // Chờ 1.5 giây
    }

    // Chuyển sang câu hỏi tiếp theo
    private void goToNextQuestion() {
        currentQuestionIndex++;
        // Kích hoạt lại các nút
        buttonCheck.setEnabled(true);
        buttonSkip.setEnabled(true);
        loadQuestion();
    }

    // Kết thúc game
    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(GameResultActivity.EXTRA_SCORE, score);
        intent.putExtra(GameResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(GameResultActivity.EXTRA_GAME_TYPE, "Game 1: Sắp xếp chữ"); // Gửi loại game
        startActivity(intent);
        finish(); // Đóng màn hình game
    }
}