package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "SCORE";
    public static final String EXTRA_TOTAL_QUESTIONS = "TOTAL_QUESTIONS";
    public static final String EXTRA_GAME_TYPE = "GAME_TYPE"; // Để biết là game nào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textScore = findViewById(R.id.textViewGameResultScore);
        Button buttonFinish = findViewById(R.id.buttonFinishGame);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int totalQuestions = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        String gameType = getIntent().getStringExtra(EXTRA_GAME_TYPE);


        if (totalQuestions > 0) {
            textScore.setText(String.format(Locale.getDefault(), "Result: %d/%d Correct ", score, totalQuestions));
        } else {
            textScore.setText("N/A"); // Trường hợp không có câu hỏi
        }

        buttonFinish.setOnClickListener(v -> {
            finish(); // Đóng màn hình kết quả, quay lại màn hình trước (FlashcardDetail)
        });
    }
}