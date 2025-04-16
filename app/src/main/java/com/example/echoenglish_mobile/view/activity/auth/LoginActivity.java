package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.dashboard.DashboardActivity;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private TextView tvForgotPassword;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        tvForgotPassword = findViewById(R.id.textView6);

        apiService = ApiClient.getApiService();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        checkIfLoggedIn();
    }

    private void checkIfLoggedIn() {
        String token = sharedPreferences.getString("AUTH_TOKEN", null);
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Token found, navigating to main activity.");
            navigateToMainActivity();
        }
    }

    private void attemptLogin() {
        etEmail.setError(null);
        etPassword.setError(null);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) { etEmail.setError(getString(R.string.error_field_required)); focusView = etEmail; cancel = true;
        } else if (!isEmailValid(email)) { etEmail.setError(getString(R.string.error_invalid_email)); if(focusView == null) focusView = etEmail; cancel = true; }
        if (TextUtils.isEmpty(password)) { etPassword.setError(getString(R.string.error_field_required)); if(focusView == null) focusView = etPassword; cancel = true; }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
        } else {
            performLogin(email, password);
        }
    }

    private boolean isEmailValid(String email) { return Patterns.EMAIL_ADDRESS.matcher(email).matches(); }

    private void performLogin(String email, String password) {
        // TODO: Show Progress Indicator
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                // TODO: Hide Progress Indicator
                if (response.isSuccessful() && response.body() != null) { // HTTP 200 OK
                    Map<String, String> responseBody = response.body();
                    String token = responseBody.get("token");
                    if (token != null && !token.isEmpty()) {
                        Log.d(TAG, "Login successful");
                        saveAuthToken(token);
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        Log.w(TAG, "Login successful but token was null/empty in response.");
                        Toast.makeText(LoginActivity.this, "Login failed: Server response error.", Toast.LENGTH_LONG).show();
                    }
                } else { // Handle errors (401 Unauthorized, etc.)
                    String errorMessage = parseError(response); // Use helper
                    Log.w(TAG, "Login failed: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                // TODO: Hide Progress Indicator
                Log.e(TAG, "API call failed", t);
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAuthToken(String token) {
        sharedPreferences.edit().putString("AUTH_TOKEN", token).apply();
        Log.d(TAG, "Auth token saved.");
    }

    private void navigateToMainActivity() {
     Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
     startActivity(intent);
     finish();
    }

    // Re-use error parsing helper
    private String parseError(Response<?> response) {
        String message = "An error occurred.";
        if (response.errorBody() != null) {
            try {
                message = response.errorBody().string();
                // If error body is JSON like {"message": "..."}
                // try {
                //     JSONObject errorObj = new JSONObject(message);
                //     message = errorObj.optString("message", message); // Get specific field
                // } catch (JSONException jsonE) {
                //     // Keep the raw string if not JSON or field not found
                // }
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
