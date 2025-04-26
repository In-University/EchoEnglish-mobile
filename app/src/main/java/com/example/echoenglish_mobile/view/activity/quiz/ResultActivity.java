package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.echoenglish_mobile.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private ImageView resultImage;
    private TextView tvTitle, tvSubtitle, tvResultScore;
    private CircularProgressIndicator scoreProgressIndicator;
    private TextView scoreProgressText;
    private Button btnShowAnswers, btnFinish;

    private long historyId;
    private double scorePercentage = -1;
    private int totalQuestions = -1;
    private int correctAnswers = -1;


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
        historyId = intent.getLongExtra(Constants.EXTRA_HISTORY_ID, -1);
        // Get score details, check for default values if not provided
        scorePercentage = intent.getDoubleExtra(Constants.EXTRA_SCORE, -1.0);
        totalQuestions = intent.getIntExtra(Constants.EXTRA_TOTAL_QUESTIONS, -1);
        correctAnswers = intent.getIntExtra(Constants.EXTRA_CORRECT_ANSWERS, -1);

        // Need at least historyId and some score info to proceed
        return historyId != -1 && (scorePercentage != -1.0 || (totalQuestions != -1 && correctAnswers != -1));
    }

    private void bindViews() {
        resultImage = findViewById(R.id.activity_result_image);
        tvTitle = findViewById(R.id.activity_result_text_title);
        tvSubtitle = findViewById(R.id.activity_result_text_subtitle);
        tvResultScore = findViewById(R.id.activity_result_text_result);
        scoreProgressIndicator = findViewById(R.id.score_progress_indicator);
        scoreProgressText = findViewById(R.id.score_progress_text);
        btnShowAnswers = findViewById(R.id.activity_result_button_show_answers);
        btnFinish = findViewById(R.id.activity_result_button_finish);
    }

    private void displayResults() {
        // Calculate percentage if only counts are available
        if (scorePercentage == -1.0 && totalQuestions > 0) {
            scorePercentage = ((double) correctAnswers / totalQuestions) * 100.0;
        }

        // Calculate counts if only percentage is available (less precise)
        if (correctAnswers == -1 && totalQuestions > 0 && scorePercentage != -1.0) {
            correctAnswers = (int) Math.round((scorePercentage / 100.0) * totalQuestions);
        }

        int scoreInt = (int) Math.round(scorePercentage);
        scoreProgressIndicator.setProgressCompat(scoreInt, true);
        scoreProgressText.setText(String.format(Locale.getDefault(), "%d%%", scoreInt));

        if (totalQuestions > 0) {
            tvResultScore.setText(String.format(Locale.getDefault(), "Result: %d/%d", correctAnswers, totalQuestions));
        } else {
            tvResultScore.setText(String.format(Locale.getDefault(), "Score: %d%%", scoreInt)); // Fallback if counts unknown
        }


        // Customize title, subtitle, image based on score
        if (scorePercentage >= 60) { // Example passing threshold
            tvTitle.setText("Good Job!");
            tvTitle.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_primary)); // Adjust color
            resultImage.setImageResource(R.drawable.activity_result_image_happy); // Ensure drawable exists
        } else {
            tvTitle.setText("Keep Practicing!");
            tvTitle.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error)); // Adjust color
            resultImage.setImageResource(R.drawable.ic_xml_sentiment_dissatisfied_24px); // Ensure drawable exists (or use a sad one)
        }
        tvSubtitle.setText(String.format(Locale.getDefault(), "You got %d questions correct!", correctAnswers));

    }

    private void setupListeners() {
        btnFinish.setOnClickListener(v -> {
            // Navigate back to Home or Test List
            Intent intent = new Intent(ResultActivity.this, MainQuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnShowAnswers.setOnClickListener(v -> {
            // Start Explanation Activity
            // Intent intent = new Intent(ResultActivity.this, ExplanationActivity.class);
            // intent.putExtra(Constants.EXTRA_HISTORY_ID, historyId);
            // startActivity(intent);
            Toast.makeText(this, "Show Answers feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
}


