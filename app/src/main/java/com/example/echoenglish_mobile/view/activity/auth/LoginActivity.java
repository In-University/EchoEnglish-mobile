package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.LoginRequest;
import com.example.echoenglish_mobile.model.response.LoginResponse;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.home.HomeActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private TextView tvForgotPassword;
    private ApiService apiService;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        apiService = ApiClient.getApiService();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

    }

    private void attemptLogin() {
        SharedPrefManager.getInstance(this).clear();
        etEmail.setError(null);
        etPassword.setError(null);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            if (focusView == null) focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
        } else {
            performLogin(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void performLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(LoginActivity.this);

                    sharedPrefManager.saveAuthToken(loginResponse.getToken());
                    sharedPrefManager.saveUserInfo(loginResponse.getUser());

                    Log.d(TAG, "Login successful");
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    navigateToHomeActivity();
                } else {
                    String errorMessage = parseError(response);
                    Log.w(TAG, "Login failed: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack cũ
        startActivity(intent);
    }

    private String parseError(Response<?> response) {
        String message = "An error occurred.";
        if (response.errorBody() != null) {
            try {
                message = response.errorBody().string();
            } catch (IOException e) {
                Log.e(TAG, "Error reading error body", e);
                message = "Error: " + response.code() + " " + response.message();
            }
        } else {
            message = "Error: " + response.code() + " " + response.message();
        }
        return message;
    }
}
