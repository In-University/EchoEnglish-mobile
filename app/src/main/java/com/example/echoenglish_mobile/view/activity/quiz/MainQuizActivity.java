package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;
import com.google.android.material.card.MaterialCardView;


public class MainQuizActivity extends AppCompatActivity {

    private static final String TAG = "MainQuizActivity";
    private static final String LOADING_DIALOG_TAG = "MainQuizLoadingDialog";

    private ImageView backButton;
    private TextView textScreenTitle;

    private MaterialCardView cardPart1;
    private MaterialCardView cardPart5;


    private int loadingApiCount = 0;
    private View contentScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz);

        findViews();
        setupCustomHeader();


        setUiEnabled(true);
        contentScrollView.setVisibility(View.VISIBLE);


        cardPart1.setOnClickListener(v -> startTestListActivity(1));
        cardPart5.setOnClickListener(v -> startTestListActivity(5));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        cardPart1 = findViewById(R.id.cardPart1);
        cardPart5 = findViewById(R.id.cardPart5);

        contentScrollView = findViewById(R.id.contentScrollView);

    }

    private void setupCustomHeader() {
        textScreenTitle.setText("Quiz");
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void setUiEnabled(boolean enabled) {
        cardPart1.setEnabled(enabled);
        cardPart5.setEnabled(enabled);
        backButton.setEnabled(enabled);
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    private void startTestListActivity(int partNum) {
        Intent intent = new Intent(MainQuizActivity.this, TestListActivity.class);
        intent.putExtra("EXTRA_PART_NUMBER", partNum);
        startActivity(intent);
    }


    private synchronized void startApiCall() {
        startApiCall("Loading...");
    }

    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false);
            if (contentScrollView != null) contentScrollView.setVisibility(View.INVISIBLE);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            setUiEnabled(true);

            if (contentScrollView != null) contentScrollView.setVisibility(View.VISIBLE);

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }
}