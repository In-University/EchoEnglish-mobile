package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.widget.ScrollView;

import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.DueReviewCountResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.MemoryLevelsResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;


import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpacedRepetitionActivity extends AppCompatActivity {
    private Long currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();

    private static final String TAG = "SpacedRepetitionAct";
    private static final String LOADING_DIALOG_TAG = "SpacedRepetitionLoadingDialog";


    private ImageView backButton;
    private TextView textScreenTitle;

    private TextView textMemoryLevel0;
    private TextView textMemoryLevel1;
    private TextView textMemoryLevel2;
    private TextView textMemoryLevel3;
    private TextView textMemoryLevel4;
    private TextView textMemoryLevelMastered;

    private TextView textDueReviewCount;
    private Button buttonReviewNow;

    private MaterialCardView cardMyDecks;
    private MaterialCardView cardPublicDecks;


    private ScrollView contentScrollView;


    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spaced_repetition);

        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        textMemoryLevel0 = findViewById(R.id.textMemoryLevel0);
        textMemoryLevel1 = findViewById(R.id.textMemoryLevel1);
        textMemoryLevel2 = findViewById(R.id.textMemoryLevel2);
        textMemoryLevel3 = findViewById(R.id.textMemoryLevel3);
        textMemoryLevel4 = findViewById(R.id.textMemoryLevel4);
        textMemoryLevelMastered = findViewById(R.id.textMemoryLevelMastered);

        textDueReviewCount = findViewById(R.id.textDueReviewCount);
        buttonReviewNow = findViewById(R.id.buttonReviewNow);

        cardMyDecks = findViewById(R.id.cardMyDecks);
        cardPublicDecks = findViewById(R.id.cardPublicDecks);


        contentScrollView = findViewById(R.id.contentScrollView);


        apiService = ApiClient.getApiService();

        loadMemoryLevels();
        loadDueReviewCount();


        backButton.setOnClickListener(v -> finish());

        buttonReviewNow.setOnClickListener(v -> startReview());

        cardMyDecks.setOnClickListener(v -> {
            Intent intent = new Intent(SpacedRepetitionActivity.this, MyFlashcardsActivity.class);
            startActivity(intent);
        });

        cardPublicDecks.setOnClickListener(v -> {
            Intent intent = new Intent(SpacedRepetitionActivity.this, PublicCategoriesActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing SRS data.");
        loadMemoryLevels();
        loadDueReviewCount();
    }


    private int loadingApiCount = 0;

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading data...");
            contentScrollView.setVisibility(View.INVISIBLE);
            buttonReviewNow.setEnabled(false);
            cardMyDecks.setEnabled(false);
            cardPublicDecks.setEnabled(false);
            backButton.setEnabled(false);
        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            contentScrollView.setVisibility(View.VISIBLE);
            boolean canReview = textDueReviewCount.getText().toString().contains(" words") ? Integer.parseInt(textDueReviewCount.getText().toString().replace(" words", "").replace("?", "0")) > 0 : false;
            buttonReviewNow.setEnabled(canReview);
            cardMyDecks.setEnabled(true);
            cardPublicDecks.setEnabled(true);
            backButton.setEnabled(true);
        }
    }


    private void loadMemoryLevels() {
        startApiCall();

        apiService.getMemoryLevels(currentUserId).enqueue(new Callback<MemoryLevelsResponse>() {
            @Override
            public void onResponse(Call<MemoryLevelsResponse> call, Response<MemoryLevelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MemoryLevelsResponse levels = response.body();
                    textMemoryLevel0.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel0()));
                    textMemoryLevel1.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel1()));
                    textMemoryLevel2.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel2()));
                    textMemoryLevel3.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel3()));
                    textMemoryLevel4.setText(String.format(Locale.getDefault(), "%d words", levels.getLevel4()));
                    textMemoryLevelMastered.setText(String.format(Locale.getDefault(), "%d words", levels.getMastered()));
                } else {
                    Log.e(TAG, "Failed to load global memory levels: " + response.code() + " - " + response.message());
                    textMemoryLevel0.setText("- words"); textMemoryLevel1.setText("- words");
                    textMemoryLevel2.setText("- words"); textMemoryLevel3.setText("- words");
                    textMemoryLevel4.setText("- words"); textMemoryLevelMastered.setText("- words");
                }
                finishApiCall();
            }

            @Override
            public void onFailure(Call<MemoryLevelsResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading global memory levels", t);
                textMemoryLevel0.setText("- words"); textMemoryLevel1.setText("- words");
                textMemoryLevel2.setText("- words"); textMemoryLevel3.setText("- words");
                textMemoryLevel4.setText("- words"); textMemoryLevelMastered.setText("- words");
                finishApiCall();
            }
        });
    }

    private void loadDueReviewCount() {
        startApiCall();

        apiService.getDueReviewCount(currentUserId).enqueue(new Callback<DueReviewCountResponse>() {
            @Override
            public void onResponse(Call<DueReviewCountResponse> call, Response<DueReviewCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DueReviewCountResponse reviewCount = response.body();
                    int count = reviewCount.getCount();
                    textDueReviewCount.setText(String.format(Locale.getDefault(), "%d words", count));
                } else {
                    Log.e(TAG, "Failed to load due review count: " + response.code() + " - " + response.message());
                    textDueReviewCount.setText("? words");
                }
                finishApiCall();
            }

            @Override
            public void onFailure(Call<DueReviewCountResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading due review count", t);
                textDueReviewCount.setText("? words");
                finishApiCall();
            }
        });
    }


    private void startReview() {
        if (!buttonReviewNow.isEnabled()) {
            Toast.makeText(this, "No words are currently due for review.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        reviewIntent.putExtra(ReviewActivity.USER_ID_EXTRA, currentUserId);
        startActivity(reviewIntent);
    }
}