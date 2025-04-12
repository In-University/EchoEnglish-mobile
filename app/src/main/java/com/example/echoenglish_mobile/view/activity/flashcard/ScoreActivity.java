package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        setTitle(getFolderNameFromSharedPreferences());

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int incorrectCount = getIntent().getIntExtra("incorrectCount", 0);

        TextView correctCountTextView = findViewById(R.id.correctCount);
        TextView incorrectCountTextView = findViewById(R.id.incorrectCount);

        correctCountTextView.setText(String.valueOf(correctCount));
        incorrectCountTextView.setText(String.valueOf(incorrectCount));

        Button btnReviewAgain = findViewById(R.id.reviewAgainButton);

        btnReviewAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScoreActivity.this, CardFlip.class);
                startActivity(intent);
            }
        });
    }

    private String getFolderNameFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.folder_preferences), MODE_PRIVATE);
        return sharedPreferences.getString(getString(R.string.current_folder_name), "Words");
    }
}