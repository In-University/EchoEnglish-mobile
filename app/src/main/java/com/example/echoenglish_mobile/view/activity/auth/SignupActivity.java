package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;

public class SignupActivity extends AppCompatActivity {

    private Button btnSignup;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            Toast.makeText(SignupActivity.this, "Sign up success!", Toast.LENGTH_SHORT).show();
        });


    }
}
