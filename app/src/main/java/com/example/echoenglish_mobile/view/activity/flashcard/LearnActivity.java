package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnActivity extends AppCompatActivity {
    private Long currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();
    private static final String TAG = "LearnActivity";
    public static final String VOCABULARY_LIST_EXTRA = "VOCABULARY_LIST";

    private ViewPager2 viewPagerLearn;
    private Button buttonKnow;
    private Button buttonForget;
    private LearnPagerAdapter pagerAdapter;
    private List<VocabularyResponse> vocabularies;

    private ApiService apiService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        viewPagerLearn = findViewById(R.id.viewPagerLearn);
        buttonKnow = findViewById(R.id.buttonKnow);
        buttonForget = findViewById(R.id.buttonForget);

        apiService = ApiClient.getApiService();

        List<VocabularyResponse> vocabListFromIntent = null;
        if (getIntent().hasExtra(VOCABULARY_LIST_EXTRA)) {
            try {
                Object extra = getIntent().getSerializableExtra(VOCABULARY_LIST_EXTRA);
                if (extra instanceof ArrayList) {
                    vocabListFromIntent = (ArrayList<VocabularyResponse>) extra;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error casting VOCABULARY_LIST extra", e);
            }
        }

        if (vocabListFromIntent != null && !vocabListFromIntent.isEmpty()) {
            Log.d(TAG, "Loading vocabularies from Intent. Size: " + vocabListFromIntent.size());
            this.vocabularies = vocabListFromIntent;
            Collections.shuffle(this.vocabularies);

            pagerAdapter = new LearnPagerAdapter(this, this.vocabularies);
            viewPagerLearn.setAdapter(pagerAdapter);

        } else {
            Log.e(TAG, "No vocabulary list received via Intent or list is empty.");
            Toast.makeText(this, "Không có từ vựng để học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buttonKnow.setOnClickListener(v -> handleLearningClick(true));
        buttonForget.setOnClickListener(v -> handleLearningClick(false));
    }

    private void handleLearningClick(boolean isRemembered) {
        int currentItemIndex = viewPagerLearn.getCurrentItem();

        if (pagerAdapter != null && pagerAdapter.getItemCount() > currentItemIndex) {
            VocabularyResponse currentVocab = pagerAdapter.getItem(currentItemIndex);

            if (currentVocab != null && currentVocab.getId() != null) {
                recordLearningProgress(currentVocab.getId(), isRemembered);

                if (currentItemIndex == pagerAdapter.getItemCount() - 1) {

                    buttonKnow.setEnabled(false);
                    buttonForget.setEnabled(false);

                    Toast.makeText(this, "Hoàn thành bộ thẻ!", Toast.LENGTH_SHORT).show();

                    mainHandler.postDelayed(() -> {
                        finish();
                    }, 800);

                } else {
                    viewPagerLearn.setCurrentItem(currentItemIndex + 1, true);
                }
            } else {
                Log.w(TAG, "handleLearningClick: Current vocabulary or its ID is null at position " + currentItemIndex);
                Toast.makeText(this, "Lỗi: Không thể xử lý từ này.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "handleLearningClick: Pager adapter state is invalid. Current index: " + currentItemIndex + ", Item count: " + (pagerAdapter != null ? pagerAdapter.getItemCount() : "null"));
            Toast.makeText(this, "Lỗi: Không có từ vựng để xử lý.", Toast.LENGTH_SHORT).show();
        }
    }

    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized.");
            return;
        }

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(currentUserId);
        request.setVocabularyId(vocabularyId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }
}