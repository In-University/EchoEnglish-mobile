package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.TextView; // Keep if needed
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.ResetPasswordRequest;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etOtpCode, etNewPassword, etConfirmNewPassword;
    private Button btnResetPassword;
    // private TextView tvInfo;
    private ApiService apiService;
    private String userEmail;
    private static final String TAG = "ResetPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etOtpCode = findViewById(R.id.etResetOtpCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        // tvInfo = findViewById(R.id.tvResetPasswordInfo);
        apiService = ApiClient.getApiService();

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Error: Email not provided.", Toast.LENGTH_LONG).show();
            finish(); return;
        }

        btnResetPassword.setOnClickListener(v -> attemptResetPassword());
    }

    private void attemptResetPassword() {
        String otpCode = etOtpCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();
        boolean cancel = false;
        EditText focusView = null;

        if (TextUtils.isEmpty(otpCode) || otpCode.length() != 6) { etOtpCode.setError("Valid 6-digit OTP required"); focusView = etOtpCode; cancel = true; }
        if (TextUtils.isEmpty(newPassword) ) { etNewPassword.setError("New password required"); if(focusView == null) focusView = etNewPassword; cancel = true;
        } else if (newPassword.length() < 6) { etNewPassword.setError("Password must be at least 6 characters"); if(focusView == null) focusView = etNewPassword; cancel = true; } // Match validation
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmNewPassword.setError("Confirm password required"); if(focusView == null) focusView = etConfirmNewPassword; cancel = true;
        } else if (!newPassword.equals(confirmPassword)) { etConfirmNewPassword.setError("Passwords do not match"); if(focusView == null) focusView = etConfirmNewPassword; cancel = true; }

        if (cancel) {
            if(focusView != null) focusView.requestFocus();
        } else {
            performPasswordReset(userEmail, otpCode, newPassword);
        }
    }

    private void performPasswordReset(String email, String otpCode, String newPassword) {
        // TODO: Show Progress Indicator
        ResetPasswordRequest request = new ResetPasswordRequest(email, otpCode, newPassword);

        apiService.resetPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // TODO: Hide Progress Indicator
                if (response.isSuccessful() && response.body() != null) { // HTTP 200 OK
                    try {
                        String successMessage = response.body().string();
                        Toast.makeText(ResetPasswordActivity.this, successMessage, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading success response body", e);
                        Toast.makeText(ResetPasswordActivity.this, "Password reset successfully.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else { // Handle errors (400 Bad Request for invalid OTP, etc.)
                    String errorMessage = parseError(response);
                    Log.w(TAG, "Reset password failed: " + errorMessage);
                    Toast.makeText(ResetPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                // TODO: Hide Progress Indicator
                Log.e(TAG, "API call failed", t);
                Toast.makeText(ResetPasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseError(Response<?> response) {
        String message = "An error occurred.";
        if (response.errorBody() != null) {
            try { message = response.errorBody().string(); }
            catch (IOException e) { Log.e(TAG, "Error reading error body", e); message = "Error: " + response.code() + " " + response.message();}
        } else { message = "Error: " + response.code() + " " + response.message(); }
        return message;
    }
}