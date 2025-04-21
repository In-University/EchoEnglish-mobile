package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
// import com.your_app_name.network.RetrofitClientInstance; // Xóa import cũ

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateFlashcardActivity extends AppCompatActivity {

    private static final String TAG = "CreateFlashcard";

    private TextInputLayout textFieldLayoutName;
    private TextInputEditText editTextName;
    private TextInputEditText editTextImageUrl;
    private Button buttonCreate;
    private ProgressBar progressBar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flashcard);

        textFieldLayoutName = findViewById(R.id.textFieldLayoutFlashcardName);
        editTextName = findViewById(R.id.editTextFlashcardName);
        editTextImageUrl = findViewById(R.id.editTextFlashcardImageUrl);
        buttonCreate = findViewById(R.id.buttonCreateFlashcardSubmit);
        progressBar = findViewById(R.id.progressBarCreateFlashcard);

        apiService = ApiClient.getApiService(); // *** SỬ DỤNG ApiClient ***

        buttonCreate.setOnClickListener(v -> attemptCreateFlashcard());
    }

    private void attemptCreateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Tên bộ thẻ không được để trống");
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        FlashcardCreateRequest request = new FlashcardCreateRequest();
        request.setName(name);
        if (!TextUtils.isEmpty(imageUrl)) {
            request.setImageUrl(imageUrl);
        }

        showLoading(true);

        apiService.createFlashcard(request).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateFlashcardActivity.this, "Tạo bộ thẻ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to create flashcard: " + response.code() + " - " + response.message());
                    Toast.makeText(CreateFlashcardActivity.this, "Lỗi tạo bộ thẻ: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error creating flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Lỗi mạng khi tạo bộ thẻ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonCreate.setEnabled(!isLoading);
    }
}