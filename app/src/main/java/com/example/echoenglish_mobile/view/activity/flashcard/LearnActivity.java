package com.example.echoenglish_mobile.view.activity.flashcard;

import android.os.Bundle;
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
    private static final long CURRENT_USER_ID = 1L;

    private ViewPager2 viewPagerLearn;
    private Button buttonKnow;

    private ApiService apiService;
    private LearnPagerAdapter pagerAdapter;
    private List<VocabularyResponse> vocabularies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // Ánh xạ view
        viewPagerLearn = findViewById(R.id.viewPagerLearn);
        buttonKnow = findViewById(R.id.buttonKnow);

        apiService = ApiClient.getApiService();

        long flashcardId = getIntent().getLongExtra(FLASHCARD_ID_EXTRA, -1);

        if (flashcardId == -1) {
            Log.e(TAG, "Invalid Flashcard ID for learning.");
            Toast.makeText(this, "Không thể tải bộ thẻ để học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo adapter rỗng ban đầu
        pagerAdapter = new LearnPagerAdapter(this, null);
        viewPagerLearn.setAdapter(pagerAdapter);

        loadVocabularies(flashcardId);

        buttonKnow.setOnClickListener(v -> handleKnowClick());
    }

    private void loadVocabularies(long flashcardId) {
        apiService.getVocabularies(flashcardId).enqueue(new Callback<List<VocabularyResponse>>() {
            @Override
            public void onResponse(Call<List<VocabularyResponse>> call, Response<List<VocabularyResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    vocabularies = response.body();
                    Collections.shuffle(vocabularies); // Trộn thứ tự

                    // Cập nhật lại adapter với dữ liệu mới
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

    private void handleKnowClick() {
        int currentItem = viewPagerLearn.getCurrentItem();
        if (pagerAdapter != null) {
            VocabularyResponse currentVocab = pagerAdapter.getItem(currentItem);
            if (currentVocab != null) {
                recordLearningProgress(currentVocab.getId());

                if (currentItem < pagerAdapter.getItemCount() - 1) {
                    viewPagerLearn.setCurrentItem(currentItem + 1, true);
                } else {
                    Toast.makeText(this, "Hoàn thành bộ thẻ!", Toast.LENGTH_SHORT).show();
                    // Optional: finish();
                }
            }
        }
    }

    private void recordLearningProgress(long vocabularyId) {
        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(CURRENT_USER_ID);
        request.setVocabularyId(vocabularyId);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhớ thành công vocab ID: " + vocabularyId);
                } else {
                    Log.w(TAG, "Ghi nhớ thất bại: " + response.code());
                    Toast.makeText(LearnActivity.this, "Lỗi ghi nhớ.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi khi ghi nhớ", t);
                Toast.makeText(LearnActivity.this, "Lỗi mạng khi ghi nhớ.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
