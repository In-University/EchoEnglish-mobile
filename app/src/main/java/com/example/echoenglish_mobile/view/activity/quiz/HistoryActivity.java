package com.example.echoenglish_mobile.view.activity.quiz;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // For back button in toolbar
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestHistory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoHistory;
    private ApiService apiService;
    private Toolbar toolbar;

    private static final String TAG = "HistoryActivity";
    private static final Long HARDCODED_USER_ID = 27L; // Gán cứng User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        apiService = ApiClient.getApiService();
        toolbar = findViewById(R.id.toolbar_history);
        recyclerViewHistory = findViewById(R.id.recycler_view_history);
        progressBar = findViewById(R.id.progress_bar_history);
        tvNoHistory = findViewById(R.id.tv_no_history);

        setupToolbar();
        setupRecyclerView();
        fetchHistoryData();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, new ArrayList<>()); // Khởi tạo với list rỗng
        recyclerViewHistory.setAdapter(adapter);
    }

    private void fetchHistoryData() {
        showLoading(true);

        Long userId = HARDCODED_USER_ID; // Sử dụng ID gán cứng
        Log.d(TAG, "Fetching history for User ID: " + userId);

        /*
        // TODO: Thay thế bằng logic lấy userId thực tế khi có đăng nhập
        Long userId = getCurrentUserIdFromPreferencesOrAuth();
        if (userId == null) {
            Log.e(TAG, "User ID is null. Cannot fetch history.");
            showLoading(false);
            tvNoHistory.setText("Could not identify user.");
            tvNoHistory.setVisibility(View.VISIBLE);
            return;
        }
        */

        apiService.getUserTestHistory(userId).enqueue(new Callback<List<TestHistory>>() {
            @Override
            public void onResponse(@NonNull Call<List<TestHistory>> call, @NonNull Response<List<TestHistory>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<TestHistory> histories = response.body();
                    if (histories.isEmpty()) {
                        Log.d(TAG, "No history found for user ID: " + userId);
                        tvNoHistory.setVisibility(View.VISIBLE);
                        recyclerViewHistory.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "Successfully fetched " + histories.size() + " history records.");
                        adapter.updateData(histories);
                        tvNoHistory.setVisibility(View.GONE);
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch history. Code: " + response.code() + ", Message: " + response.message());
                    handleFetchError("Failed to load history. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TestHistory>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching history: " + t.getMessage(), t);
                handleFetchError("Network error. Please check your connection.");
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void handleFetchError(String message) {
        tvNoHistory.setText(message);
        tvNoHistory.setVisibility(View.VISIBLE);
        recyclerViewHistory.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Xử lý sự kiện click nút back trên Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng Activity hiện tại và quay lại màn hình trước đó
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Optional: Thêm hàm lấy User ID thực tế sau này
    /*
    private Long getCurrentUserIdFromPreferencesOrAuth() {
        // Implement logic to get logged-in user's ID (e.g., from SharedPreferences)
        // SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        // long id = prefs.getLong("user_id", -1L);
        // return (id != -1L) ? id : null;
        return null; // Placeholder
    }
    */
}