package com.example.echoenglish_mobile.view.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R; // Thay đổi package nếu cần
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etForgotPasswordEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        apiService = ApiClient.getApiService();

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email");
                etEmail.requestFocus();
            } else {
                sendOtpRequest(email);
            }
        });
    }

    private void sendOtpRequest(String email) {
        // TODO: Show ProgressBar
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);

        apiService.forgotPassword(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                // TODO: Hide ProgressBar
                if (response.isSuccessful()) {
                    try {
                        String successMessage = response.body() != null ? response.body().string() : "OTP sent to your email.";
                        Toast.makeText(ForgotPasswordActivity.this, successMessage, Toast.LENGTH_LONG).show();
                        // Chuyển sang màn hình ResetPasswordActivity, gửi kèm email
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class); // Tạo Activity này ở bước 7
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                    } catch (IOException e) {
                        Log.e("ForgotPass", "Error reading success response", e);
                        Toast.makeText(ForgotPasswordActivity.this, "OTP sent to your email.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                    }

                } else {
                    // Lỗi (ví dụ: email không tồn tại)
                    String errorMessage = "Failed to send OTP.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string(); // VD: "Mail not found"
                        } catch (IOException e) {
                            Log.e("ForgotPass", "Error reading error body", e);
                        }
                    }
                    Log.w("ForgotPass", "Forgot password failed: " + errorMessage);
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                // TODO: Hide ProgressBar
                Log.e("ForgotPass", "API call failed: ", t);
                Toast.makeText(ForgotPasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}