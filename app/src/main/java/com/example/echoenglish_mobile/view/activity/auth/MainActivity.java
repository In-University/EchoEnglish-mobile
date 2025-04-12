package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.PronunciationAssessmentActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String targetWord = "about";
        if (!targetWord.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, PronunciationAssessmentActivity.class);
            intent.putExtra("targetWord", targetWord);
            startActivity(intent);
        }

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

    }
}