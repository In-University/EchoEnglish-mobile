package com.example.echoenglish_mobile.view.activity.quiz; // Thay package phù hợp

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizActivity;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private ImageView resultImage;
    private TextView tvTitle, tvSubtitle, tvResultScore;
    private Button btnShowAnswers, btnFinish;

    private int totalQuestions = 0;
    private int correctAnswers = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (!getIntentData()) {
            Toast.makeText(this, "Could not load results.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        displayResults();
        setupListeners();
    }

    private boolean getIntentData() {
        Intent intent = getIntent();
        totalQuestions = intent.getIntExtra(Constants.EXTRA_TOTAL_QUESTIONS, 0);
        correctAnswers = intent.getIntExtra(Constants.EXTRA_CORRECT_ANSWERS, 0);
        // Chỉ cần totalQuestions > 0
        return totalQuestions > 0;
    }

    private void bindViews() {
        resultImage = findViewById(R.id.activity_result_image);
        tvTitle = findViewById(R.id.activity_result_text_title);
        tvSubtitle = findViewById(R.id.activity_result_text_subtitle);
        tvResultScore = findViewById(R.id.activity_result_text_result);
        btnShowAnswers = findViewById(R.id.activity_result_button_show_answers);
        btnFinish = findViewById(R.id.activity_result_button_finish);
    }

    private void displayResults() {
        // Hiển thị X/Y Correct
        tvResultScore.setText(String.format(Locale.getDefault(),
                "Result: %d/%d Correct", correctAnswers, totalQuestions)); // Chuỗi cứng

        double percentage = (totalQuestions > 0) ? ((double) correctAnswers / totalQuestions) * 100.0 : 0.0;

        if (percentage >= 50) {
            tvTitle.setText("Good Job!"); // Chuỗi cứng
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.green_correct));
            resultImage.setImageResource(R.drawable.activity_result_image_happy);
            tvSubtitle.setText(String.format(Locale.getDefault(),"You answered %d questions correctly!", correctAnswers)); // Chuỗi cứng
        } else {
            tvTitle.setText("Keep Practicing!"); // Chuỗi cứng
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.red_incorrect));
            resultImage.setImageResource(R.drawable.ic_xml_sentiment_dissatisfied_24px); // Đảm bảo icon tồn tại
            tvSubtitle.setText(String.format(Locale.getDefault(),"You got %d out of %d correct.", correctAnswers, totalQuestions)); // Chuỗi cứng
        }
    }

    private void setupListeners() {
        btnFinish.setOnClickListener(v -> {
            // Quay về HomeActivity (Hoặc MainQuizActivity tùy tên bạn đặt)
            Intent intent = new Intent(ResultActivity.this, MainQuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnShowAnswers.setVisibility(View.GONE); // Ẩn nút này đi
    }
}