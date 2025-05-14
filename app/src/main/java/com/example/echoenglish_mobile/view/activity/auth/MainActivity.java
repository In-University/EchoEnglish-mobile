package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnSignup;
    private Button btnLoginGuest; // Changed variable name to match XML ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnLoginGuest = findViewById(R.id.btnLoginGuest); // Changed ID to match XML

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        btnLoginGuest.setOnClickListener(v -> {
            navigateToHomeActivity();
        });

        checkIfLoggedIn();

    }

    private void checkIfLoggedIn() {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token != null && !token.isEmpty()) {
            navigateToHomeActivity();
        }
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}