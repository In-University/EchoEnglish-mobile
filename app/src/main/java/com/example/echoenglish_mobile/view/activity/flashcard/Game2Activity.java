package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.graphics.Color; // Import Color
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech; // Import TTS
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout; // Import GridLayout
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Import CardView hoặc MaterialCardView
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Game2Activity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game2Activity";

    private TextView textGameProgress;
    private ImageButton buttonPlaySound;
    private GridLayout gridAnswers;
    private Button buttonSkip;

    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private VocabularyResponse currentCorrectVocab;
    private List<VocabularyResponse> currentOptions = new ArrayList<>(); // 4 lựa chọn hiện tại

    private TextToSpeech tts;
    private boolean ttsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
        setTitle("Game: Nghe & Chọn Ảnh");

        textGameProgress = findViewById(R.id.textGame2Progress);
        buttonPlaySound = findViewById(R.id.buttonGame2PlaySound);
        gridAnswers = findViewById(R.id.gridGame2Answers);
        buttonSkip = findViewById(R.id.buttonGame2Skip);

        // Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, this);

        // Nhận dữ liệu
        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        if (vocabularyList == null || vocabularyList.size() < 4) { // Cần ít nhất 4 từ để tạo đáp án sai
            Log.e(TAG, "Not enough vocabularies received for game (need at least 4).");
            Toast.makeText(this, "Không đủ từ vựng để chơi game này.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Collections.shuffle(vocabularyList); // Xáo trộn câu hỏi
        loadQuestion();

        buttonPlaySound.setOnClickListener(v -> speakWord());
        buttonSkip.setOnClickListener(v -> goToNextQuestion());
    }

    // Xử lý sau khi TTS được khởi tạo
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Chọn ngôn ngữ nói là tiếng Anh (Mỹ)
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS Language not supported");
                Toast.makeText(this, "Ngôn ngữ TTS không được hỗ trợ.", Toast.LENGTH_SHORT).show();
                // Có thể vô hiệu hóa nút loa hoặc thoát
            } else {
                ttsInitialized = true;
                buttonPlaySound.setEnabled(true); // Kích hoạt nút loa
                // Tự động nói từ đầu tiên nếu muốn
                // speakWord();
            }
        } else {
            Log.e(TAG, "TTS Initialization failed");
            Toast.makeText(this, "Khởi tạo TTS thất bại.", Toast.LENGTH_SHORT).show();
        }
    }

    // Hiển thị câu hỏi và các lựa chọn
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
        wrongOptionsPool.remove(currentCorrectVocab); // Xóa đáp án đúng khỏi pool
        Collections.shuffle(wrongOptionsPool); // Xáo trộn pool

        int neededWrong = 3;
        for (int i = 0; i < wrongOptionsPool.size() && currentOptions.size() < 4; i++) {
            // Đảm bảo không bị trùng lặp (mặc dù shuffle thường đã đủ)
            if (!currentOptions.contains(wrongOptionsPool.get(i))) {
                currentOptions.add(wrongOptionsPool.get(i));
            }
        }
        // Nếu list không đủ 4 từ ban đầu thì cần xử lý thêm (ví dụ lấy từ DB)
        if (currentOptions.size() < 4) {
            Log.e(TAG, "Could not generate 4 unique options!");
            // Tạm thời thoát game hoặc lặp lại lựa chọn
            Toast.makeText(this, "Lỗi tạo đáp án.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Collections.shuffle(currentOptions); // Xáo trộn vị trí 4 lựa chọn

        // Tạo và thêm các CardView vào GridLayout
        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyResponse option : currentOptions) {
            // Inflate layout item_game2_answer
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_game2_answer, gridAnswers, false);

            // Lấy các view bên trong card
            ImageView imageView = cardView.findViewById(R.id.imageGame2Answer);
            TextView textView = cardView.findViewById(R.id.textGame2AnswerDefinition);

            // Đặt dữ liệu cho card
            textView.setText(option.getDefinition()); // Hiển thị nghĩa
            Glide.with(this)
                    .load(option.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageView);

            // Đặt listener để kiểm tra đáp án
            cardView.setOnClickListener(v -> checkAnswer(option, cardView));

            // Thêm CardView vào GridLayout
            // Cần đặt LayoutParams để GridLayout chia đều
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; // Để weight hoạt động
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Chia đều cột
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Chia đều hàng
            params.setMargins(5, 5, 5, 5); // Có thể điều chỉnh margin
            cardView.setLayoutParams(params);

            gridAnswers.addView(cardView);
        }

        // Kích hoạt lại nút skip
        buttonSkip.setEnabled(true);
        // Tự động phát âm thanh lần đầu (tùy chọn)
        // speakWord();
    }

    // Phát âm thanh từ tiếng Anh
    private void speakWord() {
        if (ttsInitialized && currentCorrectVocab != null) {
            String wordToSpeak = currentCorrectVocab.getWord();
            if (wordToSpeak != null && !wordToSpeak.isEmpty()) {
                // TTS nói từ
                tts.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "UniqueID");
            }
        } else if (!ttsInitialized) {
            Toast.makeText(this, "TTS chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
        }
    }

    // Kiểm tra đáp án người dùng chọn
    private void checkAnswer(VocabularyResponse selectedOption, CardView selectedCardView) {
        // Vô hiệu hóa các lựa chọn khác và nút skip
        for (int i = 0; i < gridAnswers.getChildCount(); i++) {
            gridAnswers.getChildAt(i).setClickable(false);
        }
        buttonSkip.setEnabled(false);
        buttonPlaySound.setEnabled(false); // Tạm thời vô hiệu hóa nút loa

        boolean isCorrect = selectedOption.getId().equals(currentCorrectVocab.getId());

        // Đổi màu nền CardView được chọn
        if (isCorrect) {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
            score++;
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
        } else {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.incorrect_red));
            Toast.makeText(this, "Sai rồi!", Toast.LENGTH_SHORT).show();
            // Tìm và highlight đáp án đúng
            highlightCorrectAnswer();
        }

        // Chờ một chút rồi chuyển câu
        new Handler(Looper.getMainLooper()).postDelayed(this::goToNextQuestion, 1500); // Chờ 1.5 giây
    }

    // Highlight đáp án đúng nếu trả lời sai
    private void highlightCorrectAnswer() {
        for (int i = 0; i < gridAnswers.getChildCount(); i++) {
            View child = gridAnswers.getChildAt(i);
            if (i < currentOptions.size()) { // Đảm bảo index hợp lệ
                VocabularyResponse option = currentOptions.get(i);
                if (option.getId().equals(currentCorrectVocab.getId()) && child instanceof MaterialCardView) {
                    ((MaterialCardView) child).setStrokeWidth(4); // Thêm viền
                    ((MaterialCardView) child).setStrokeColor(Color.BLUE); // Màu viền highlight
                    break;
                }
            }
        }
    }


    // Chuyển câu hỏi tiếp theo
    private void goToNextQuestion() {
        currentQuestionIndex++;
        // Kích hoạt lại nút loa
        buttonPlaySound.setEnabled(ttsInitialized);
        loadQuestion(); // Tải câu hỏi mới (sẽ tự kích hoạt lại nút skip và các card)
    }

    // Kết thúc game
    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(GameResultActivity.EXTRA_SCORE, score);
        intent.putExtra(GameResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(GameResultActivity.EXTRA_GAME_TYPE, "Game 2: Nghe & Chọn");
        startActivity(intent);
        finish();
    }

    // Dọn dẹp TTS khi Activity bị hủy
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