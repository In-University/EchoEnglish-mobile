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
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateFlashcardActivity extends AppCompatActivity {

    private static final String TAG = "CreateFlashcard";

    public static final String EXTRA_EDIT_MODE = "EDIT_MODE";
    public static final String EXTRA_FLASHCARD_ID = "FLASHCARD_ID";
    public static final String EXTRA_FLASHCARD_NAME = "FLASHCARD_NAME";
    public static final String EXTRA_FLASHCARD_IMAGE_URL = "FLASHCARD_IMAGE_URL";

    private TextInputLayout textFieldLayoutName;
    private TextInputEditText editTextName;
    private TextInputEditText editTextImageUrl;
    private Button buttonCreate;
    private ProgressBar progressBar;

    private ApiService apiService;

    private boolean isEditMode = false;
    private Long editingFlashcardId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flashcard);

        textFieldLayoutName = findViewById(R.id.textFieldLayoutFlashcardName);
        editTextName = findViewById(R.id.editTextFlashcardName);
        editTextImageUrl = findViewById(R.id.editTextFlashcardImageUrl);
        buttonCreate = findViewById(R.id.buttonCreateFlashcardSubmit);
        progressBar = findViewById(R.id.progressBarCreateFlashcard);

        apiService = ApiClient.getApiService();

        // Kiểm tra Intent có phải là sửa không
        if (getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false)) {
            isEditMode = true;
            editingFlashcardId = getIntent().getLongExtra(EXTRA_FLASHCARD_ID, -1L);
            String currentName = getIntent().getStringExtra(EXTRA_FLASHCARD_NAME);
            String currentImageUrl = getIntent().getStringExtra(EXTRA_FLASHCARD_IMAGE_URL);

            if (editingFlashcardId == -1L) {
                Toast.makeText(this, "Lỗi: Không tìm thấy ID bộ thẻ để sửa.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            editTextName.setText(currentName);
            editTextImageUrl.setText(currentImageUrl);

            setTitle("Sửa bộ thẻ");
            buttonCreate.setText("Lưu thay đổi");
        } else {
            setTitle("Tạo bộ thẻ mới");
            buttonCreate.setText("Tạo bộ thẻ");
        }

        buttonCreate.setOnClickListener(v -> {
            if (isEditMode) {
                attemptUpdateFlashcard();
            } else {
                attemptCreateFlashcard();
            }
        });
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
        request.setImageUrl(imageUrl);

        showLoading(true);

        apiService.createFlashcard(request).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Tạo bộ thẻ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Lỗi tạo flashcard: " + response.code());
                    Toast.makeText(CreateFlashcardActivity.this, "Tạo thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi tạo flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptUpdateFlashcard() {
        String name = editTextName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            textFieldLayoutName.setError("Tên bộ thẻ không được để trống");
            return;
        } else {
            textFieldLayoutName.setError(null);
        }

        FlashcardUpdateRequest updateRequest = new FlashcardUpdateRequest();
        updateRequest.setName(name);
        updateRequest.setImageUrl(imageUrl);

        showLoading(true);

        apiService.updateFlashcard(editingFlashcardId, updateRequest).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(CreateFlashcardActivity.this, "Đã cập nhật bộ thẻ", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Lỗi cập nhật flashcard: " + response.code());
                    Toast.makeText(CreateFlashcardActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi cập nhật flashcard", t);
                Toast.makeText(CreateFlashcardActivity.this, "Lỗi mạng khi cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonCreate.setEnabled(!isLoading);
    }
}
