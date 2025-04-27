package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnActivity extends AppCompatActivity {

    private static final String TAG = "LearnActivity";
    public static final String FLASHCARD_ID_EXTRA = "FLASHCARD_ID";
    private static final long CURRENT_USER_ID = 27L;

    private ViewPager2 viewPagerLearn;
    private Button buttonKnow;

    private ApiService apiService;
    private Button buttonForget;
    private LearnPagerAdapter pagerAdapter;
    private List<VocabularyResponse> vocabularies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // Ánh xạ view
        viewPagerLearn = findViewById(R.id.viewPagerLearn);
        buttonKnow = findViewById(R.id.buttonKnow);
        buttonForget = findViewById(R.id.buttonForget); // ** ÁNH XẠ NÚT MỚI **

        apiService = ApiClient.getApiService();

        long flashcardId = getIntent().getLongExtra(FLASHCARD_ID_EXTRA, -1);

        if (flashcardId == -1) {
            Log.e(TAG, "Invalid Flashcard ID for learning.");
            Toast.makeText(this, "Không thể tải bộ thẻ để học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pagerAdapter = new LearnPagerAdapter(this, null);
        viewPagerLearn.setAdapter(pagerAdapter);

        loadVocabularies(flashcardId);

        // ** CẬP NHẬT LISTENER **
        buttonKnow.setOnClickListener(v -> handleLearningClick(true)); // Xử lý khi bấm Biết (true)
        buttonForget.setOnClickListener(v -> handleLearningClick(false)); // Xử lý khi bấm Quên (false)
    }

    private void loadVocabularies(long flashcardId) {
        apiService.getVocabularies(flashcardId).enqueue(new Callback<List<VocabularyResponse>>() {
            @Override
            public void onResponse(Call<List<VocabularyResponse>> call, Response<List<VocabularyResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    vocabularies = response.body();
                    Collections.shuffle(vocabularies); // Trộn thứ tự

                    pagerAdapter = new LearnPagerAdapter(LearnActivity.this, vocabularies);
                    viewPagerLearn.setAdapter(pagerAdapter);
                } else {
                    Log.e(TAG, "Không có dữ liệu hoặc lỗi tải: " + response.code());
                    Toast.makeText(LearnActivity.this, "Không có từ vựng để học hoặc lỗi tải.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<VocabularyResponse>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi tải dữ liệu từ vựng", t);
                Toast.makeText(LearnActivity.this, "Lỗi mạng khi tải từ vựng.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }




    // ** PHƯƠNG THỨC XỬ LÝ CHUNG CHO NÚT BIẾT VÀ QUÊN **
    private void handleLearningClick(boolean isRemembered) {
        int currentItem = viewPagerLearn.getCurrentItem();
        if (pagerAdapter != null) {
            VocabularyResponse currentVocab = pagerAdapter.getItem(currentItem);
            if (currentVocab != null) {
                // Ghi nhận học tập với trạng thái isRemembered tương ứng
                recordLearningProgress(currentVocab.getId(), isRemembered);

                // Chuyển sang thẻ tiếp theo SAU KHI gọi API
                // Có thể thêm handler/delay nếu muốn hiệu ứng hoặc chờ phản hồi API
                if (currentItem < pagerAdapter.getItemCount() - 1) {
                    // Chuyển trang sau một khoảng delay ngắn để API kịp gọi (tùy chọn)
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        viewPagerLearn.setCurrentItem(currentItem + 1, true);
                    }, 200); // delay 200ms
                } else {
                    Toast.makeText(this, "Hoàn thành bộ thẻ!", Toast.LENGTH_SHORT).show();
                    // Optional: finish();
                }
            }
        }
    }

    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized.");
            return;
        }
        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(CURRENT_USER_ID);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered); // ** GỬI TRẠNG THÁI NHỚ/QUÊN **


        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhận học tập thành công cho vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                    // Toast.makeText(LearnActivity.this, isRemembered ? "Đã ghi nhớ!" : "Đã đánh dấu cần ôn lại.", Toast.LENGTH_SHORT).show(); // Optional feedback
                } else {
                    Log.w(TAG, "Ghi nhận học tập thất bại cho vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message());
                    // Optional: Show error message
                    // Toast.makeText(LearnActivity.this, "Lỗi ghi nhận học tập.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi ghi nhận học tập cho vocab ID: " + vocabularyId, t);
                // Optional: Show error message
                // Toast.makeText(LearnActivity.this, "Lỗi mạng.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
