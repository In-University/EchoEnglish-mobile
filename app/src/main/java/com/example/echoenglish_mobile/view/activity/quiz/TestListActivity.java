package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapters.TestListAdapter;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.Constants;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestListActivity extends AppCompatActivity {

    private static final String TAG = "TestListActivity";
    private static final String LOADING_DIALOG_TAG = "TestListLoadingDialog";

    private ImageView backButton;
    private TextView textScreenTitle;

    private ConstraintLayout contentContainer;
    private RecyclerView recyclerViewTests;
    private TextView tvNoTests;

    private TestListAdapter adapter;
    private ApiService apiService;
    private int partNumber;

    private int loadingApiCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        partNumber = getIntent().getIntExtra(Constants.EXTRA_PART_NUMBER, 0);
        if (partNumber == 0) {
            Toast.makeText(this, "Invalid Part Number", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Received partNumber to display tests for: " + partNumber);

        findViews();
        setupCustomHeader();

        apiService = ApiClient.getApiService();

        setupRecyclerView();

        startApiCall("Loading tests...");

        fetchTests();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        contentContainer = findViewById(R.id.contentContainer);
        recyclerViewTests = findViewById(R.id.recycler_view_tests);
        tvNoTests = findViewById(R.id.tv_no_tests);

    }

    private void setupCustomHeader() {
        textScreenTitle.setText("Select Test - Part " + partNumber);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void setupRecyclerView() {
        recyclerViewTests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TestListAdapter(this, new ArrayList<>(), partNumber);
        recyclerViewTests.setAdapter(adapter);
    }

    private void fetchTests() {
        apiService.getAllTests().enqueue(new Callback<List<Test>>() {
            @Override
            public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    List<Test> tests = response.body();
                    if (tests.isEmpty()) {
                        Log.d(TAG, "No tests found from API.");
                        showEmptyState("No tests available.");
                    } else {
                        Log.d(TAG, "Successfully fetched " + tests.size() + " tests.");
                        adapter.updateData(tests);
                        if (adapter.getItemCount() > 0) {
                            showContent();
                        } else {
                            showEmptyState("No tests available for Part " + partNumber + ".");
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch tests. Code: " + response.code());
                    showErrorState("Failed to load tests: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                finishApiCall();
                Log.e(TAG, "Network error fetching tests: " + t.getMessage(), t);
                showErrorState("Network error: " + t.getMessage());
            }
        });
    }


    private void showContent() {
        recyclerViewTests.setVisibility(View.VISIBLE);
        tvNoTests.setVisibility(View.GONE);
        setUiEnabled(true);
    }

    private void showEmptyState(String message) {
        recyclerViewTests.setVisibility(View.GONE);
        tvNoTests.setText(message);
        tvNoTests.setVisibility(View.VISIBLE);
        setUiEnabled(true);
    }

    private void showErrorState(String message) {
        recyclerViewTests.setVisibility(View.GONE);
        tvNoTests.setText(message);
        tvNoTests.setVisibility(View.VISIBLE);
        setUiEnabled(true);
    }


    private void setUiEnabled(boolean enabled) {
        backButton.setEnabled(enabled);
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false);
            contentContainer.setVisibility(View.GONE);
            tvNoTests.setVisibility(View.GONE);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            setUiEnabled(true);

            contentContainer.setVisibility(View.VISIBLE);

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