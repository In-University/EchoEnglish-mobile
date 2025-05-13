package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etRepeatPassword;
    private Button btnSignup;
    private ApiService apiService;
    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        btnSignup = findViewById(R.id.btnSignup);

        apiService = ApiClient.getApiService();
        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) { etName.setError(getString(R.string.error_field_required)); focusView = etName; cancel = true; }
        if (TextUtils.isEmpty(email)) { etEmail.setError(getString(R.string.error_field_required)); if(focusView == null) focusView = etEmail; cancel = true;
        } else if (!isEmailValid(email)) { etEmail.setError(getString(R.string.error_invalid_email)); if(focusView == null) focusView = etEmail; cancel = true; }
        if (TextUtils.isEmpty(password)) { etPassword.setError(getString(R.string.error_field_required)); if(focusView == null) focusView = etPassword; cancel = true;
        } else if (!isPasswordValid(password)) { etPassword.setError(getString(R.string.error_invalid_password)); if(focusView == null) focusView = etPassword; cancel = true; }
        if (TextUtils.isEmpty(repeatPassword)) { etRepeatPassword.setError(getString(R.string.error_field_required)); if(focusView == null) focusView = etRepeatPassword; cancel = true;
        } else if (!password.equals(repeatPassword)) { etRepeatPassword.setError(getString(R.string.error_password_mismatch)); if(focusView == null) focusView = etRepeatPassword; cancel = true; }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
        } else {
            performSignup(name, email, password);
        }
    }

    private boolean isEmailValid(String email) { return Patterns.EMAIL_ADDRESS.matcher(email).matches(); }
    private boolean isPasswordValid(String password) { return password.length() >= 6; }

    private void performSignup(String name, String email, String password) {
        User newUser = new User(name, email, password, false);

        apiService.registerUser(newUser).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String successMessage = response.body().string();
                        Toast.makeText(SignupActivity.this, successMessage, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignupActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading success response body", e);
                        Toast.makeText(SignupActivity.this, "Registration successful, proceed to verify.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignupActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                    }
                } else {
                    String errorMessage = parseError(response);
                    Log.w(TAG, "Signup failed: " + errorMessage);
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseError(Response<?> response) {
        String message = "An error occurred.";
        if (response.errorBody() != null) {
            try {
                message = response.errorBody().string();
            } catch (Exception e) {
                Log.e(TAG, "Error reading/parsing error body", e);
                message = "Error: " + response.code() + " " + response.message();
            }
        } else {
            message = "Error: " + response.code() + " " + response.message();
        }
        return message;
    }
}
