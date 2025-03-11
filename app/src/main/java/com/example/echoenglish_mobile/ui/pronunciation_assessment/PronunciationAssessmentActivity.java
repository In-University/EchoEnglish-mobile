package com.example.echoenglish_mobile.ui.pronunciation_assessment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.WebViewActivity;

public class PronunciationAssessmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pronunciation);


        Button examplesButton = findViewById(R.id.examplesButton);
        examplesButton.setOnClickListener(view -> {
            Intent intent = new Intent(PronunciationAssessmentActivity.this, WebViewActivity.class);
            intent.putExtra("URL", "https://content-media.elsanow.co/_static_/youglish.html?communication");
            startActivity(intent);
        });
    }
}