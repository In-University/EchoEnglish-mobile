package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;

import java.util.Locale;

public class GameResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "SCORE";
    public static final String EXTRA_TOTAL_QUESTIONS = "TOTAL_QUESTIONS";
    public static final String EXTRA_GAME_TYPE = "GAME_TYPE"; // Để biết là game nào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);

        TextView textTitle = findViewById(R.id.textViewGameResultTitle);
        TextView textScore = findViewById(R.id.textViewGameResultScore);
        Button buttonPlayAgain = findViewById(R.id.buttonPlayAgain);
        Button buttonFinish = findViewById(R.id.buttonFinishGame);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int totalQuestions = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        String gameType = getIntent().getStringExtra(EXTRA_GAME_TYPE);

        if (gameType != null) {
            textTitle.setText("Kết quả " + gameType);
        } else {
            textTitle.setText("Kết quả Game");
        }


        if (totalQuestions > 0) {
            textScore.setText(String.format(Locale.getDefault(), "%d / %d", score, totalQuestions));
        } else {
            textScore.setText("N/A"); // Trường hợp không có câu hỏi
        }

        buttonPlayAgain.setOnClickListener(v -> {
            // TODO: Implement play again logic
            // Có thể finish() và startActivity() lại Game1Activity/Game2Activity
            // Cần truyền lại flashcardId hoặc list vocabulary
            Toast.makeText(this, "Chức năng chơi lại chưa hoàn thiện", Toast.LENGTH_SHORT).show();
        });

        buttonFinish.setOnClickListener(v -> {
            finish(); // Đóng màn hình kết quả, quay lại màn hình trước (FlashcardDetail)
        });
    }
}