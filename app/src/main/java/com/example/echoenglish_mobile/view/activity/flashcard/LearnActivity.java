package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent; // Vẫn cần import Intent
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

import java.io.Serializable; // Import Serializable
import java.util.ArrayList; // Import ArrayList
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnActivity extends AppCompatActivity {

    private static final String TAG = "LearnActivity";
    // Removed: public static final String FLASHCARD_ID_EXTRA = "FLASHCARD_ID"; // Bỏ key này
    public static final String VOCABULARY_LIST_EXTRA = "VOCABULARY_LIST"; // Key nhận danh sách từ vựng
    private static final long CURRENT_USER_ID = 27L; // User ID cứng


    private ViewPager2 viewPagerLearn;
    private Button buttonKnow;

    private ApiService apiService;
    private Button buttonForget;
    private LearnPagerAdapter pagerAdapter;
    private List<VocabularyResponse> vocabularies;

    // Removed: private Long currentFlashcardId = null; // Không lưu Flashcard ID nữa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // Ánh xạ view
        viewPagerLearn = findViewById(R.id.viewPagerLearn);
        buttonKnow = findViewById(R.id.buttonKnow);
        buttonForget = findViewById(R.id.buttonForget);

        apiService = ApiClient.getApiService();

        // ** LOGIC MỚI: Chỉ lấy danh sách từ Intent **
        List<VocabularyResponse> vocabListFromIntent = null;
        // Kiểm tra nếu extra key tồn tại và là Serializable List of VocabularyResponse
        if (getIntent().hasExtra(VOCABULARY_LIST_EXTRA)) {
            try {
                // Sử dụng getSerializableExtra và ép kiểu an toàn
                Object extra = getIntent().getSerializableExtra(VOCABULARY_LIST_EXTRA);
                if (extra instanceof ArrayList) {
                    vocabListFromIntent = (ArrayList<VocabularyResponse>) extra;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error casting VOCABULARY_LIST extra", e);
                // Log lỗi ép kiểu, nhưng vẫn tiếp tục xử lý nếu list null/empty
            }
        }

        // Kiểm tra danh sách lấy được từ Intent
        if (vocabListFromIntent != null && !vocabListFromIntent.isEmpty()) {
            // Có danh sách từ Intent -> Sử dụng nó
            Log.d(TAG, "Loading vocabularies from Intent. Size: " + vocabListFromIntent.size());
            this.vocabularies = vocabListFromIntent;
            Collections.shuffle(this.vocabularies); // Trộn thứ tự

            // Khởi tạo adapter với danh sách đã lấy
            pagerAdapter = new LearnPagerAdapter(this, this.vocabularies);
            viewPagerLearn.setAdapter(pagerAdapter);

            // Không cần lấy Flashcard ID hay log lỗi nếu thiếu ID nữa
            // Logic tải API cũng không còn cần thiết ở đây

        } else {
            // Không có danh sách từ Intent hoặc danh sách rỗng -> Lỗi, không thể học
            Log.e(TAG, "No vocabulary list received via Intent or list is empty.");
            Toast.makeText(this, "Không có từ vựng để học.", Toast.LENGTH_SHORT).show(); // Translated
            finish(); // Đóng activity vì không có gì để học
            return; // Kết thúc hàm onCreate
        }
        // ** KẾT THÚC LOGIC MỚI **


        // Cài đặt listener cho các nút "Biết" và "Quên"
        buttonKnow.setOnClickListener(v -> handleLearningClick(true));
        buttonForget.setOnClickListener(v -> handleLearningClick(false));
    }

    // Removed: private void loadVocabularies(long flashcardId) {...} // Bỏ hẳn hàm tải API

    // Phương thức xử lý chung cho nút Biết và Quên
    private void handleLearningClick(boolean isRemembered) {
        int currentItem = viewPagerLearn.getCurrentItem();
        // Kiểm tra pagerAdapter và danh sách từ vựng trước khi thao tác
        if (pagerAdapter != null && pagerAdapter.getItemCount() > currentItem) {
            VocabularyResponse currentVocab = pagerAdapter.getItem(currentItem);
            if (currentVocab != null && currentVocab.getId() != null) {
                // Ghi nhận học tập với trạng thái isRemembered tương ứng
                // Logic ghi nhận tiến độ vẫn cần Vocabulary ID, nên phải đảm bảo DTO VocabularyResponse có ID.
                recordLearningProgress(currentVocab.getId(), isRemembered);

                // Chuyển sang thẻ tiếp theo SAU KHI gọi API (có thể thêm delay)
                if (currentItem < pagerAdapter.getItemCount() - 1) {
                    // Chuyển trang sau một khoảng delay ngắn (tùy chọn)
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        viewPagerLearn.setCurrentItem(currentItem + 1, true);
                    }, 200); // delay 200ms
                } else {
                    Toast.makeText(this, "Hoàn thành bộ thẻ!", Toast.LENGTH_SHORT).show(); // Translated
                    // Optional: finish();
                }
            } else {
                Log.w(TAG, "handleLearningClick: Current vocabulary or its ID is null at position " + currentItem);
                Toast.makeText(this, "Error: Cannot record learning for this item.", Toast.LENGTH_SHORT).show(); // Translated
            }
        } else {
            Log.w(TAG, "handleLearningClick: Pager adapter or current item position is invalid.");
            Toast.makeText(this, "Error: No vocabulary to process.", Toast.LENGTH_SHORT).show(); // Translated
        }
    }

    // Phương thức ghi nhận tiến độ học tập qua API (Giữ nguyên)
    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized.");
            return;
        }

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(CURRENT_USER_ID); // Sử dụng User ID cứng
        request.setVocabularyId(vocabularyId); // Sử dụng Vocabulary ID từ item
        request.setIsRemembered(isRemembered);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Ghi nhận học tập thành công cho vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                } else {
                    Log.w(TAG, "Ghi nhận học tập thất bại cho vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng khi ghi nhận học tập cho vocab ID: " + vocabularyId, t);
            }
        });
    }
}