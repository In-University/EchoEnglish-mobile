package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText etOtpCode;
    private Button btnVerifyOtp;
    private TextView tvOtpInfo;
    private ApiService apiService;
    private String userEmail;
    private static final String TAG = "OtpVerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        etOtpCode = findViewById(R.id.etOtpCode);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpInfo = findViewById(R.id.tvOtpInfo);
        apiService = ApiClient.getApiService();

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Error: Email not provided.", Toast.LENGTH_LONG).show();
            finish(); return;
        }
        tvOtpInfo.setText("Enter the OTP sent to " + userEmail);

        btnVerifyOtp.setOnClickListener(v -> {
            String otpCode = etOtpCode.getText().toString().trim();
            if (TextUtils.isEmpty(otpCode) || otpCode.length() != 6) {
                etOtpCode.setError("Please enter a valid 6-digit OTP");
            } else {
                verifyOtp(userEmail, otpCode);
            }
        });
    }

    private void verifyOtp(String email, String otpCode) {
        // TODO: Show Progress Indicator
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("code", otpCode);

        apiService.validateOtpRegister(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                // TODO: Hide Progress Indicator
                if (response.isSuccessful() && response.body() != null) { // HTTP 200 OK
                    try {
                        String successMessage = response.body().string();
                        Toast.makeText(OtpVerificationActivity.this, successMessage, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading success response body", e);
                        Toast.makeText(OtpVerificationActivity.this, "Email verified successfully.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else { // Handle errors (400 Bad Request for invalid OTP, etc.)
                    String errorMessage = parseError(response);
                    Log.w(TAG, "OTP validation failed: " + errorMessage);
                    Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                // TODO: Hide Progress Indicator
                Log.e(TAG, "API call failed", t);
                Toast.makeText(OtpVerificationActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
