package com.example.echoenglish_mobile.view.activity.quiz; // Thay package phù hợp

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapters.TestListAdapter;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.Constants;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;

import java.util.ArrayList;
import java.util.List;
// Bỏ import stream nếu không dùng filter ở đây nữa
// import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTests;
    private TestListAdapter adapter; // Dùng TestListAdapter
    private ProgressBar progressBar;
    private TextView tvNoTests;
    // private TextView tvListTitle; // Không cần title này nữa nếu dùng Toolbar
    private Toolbar toolbar;
    private ApiService apiService;
    private int partNumber; // Part number người dùng đã chọn từ màn hình trước

    private static final String TAG = "TestListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        // Lấy partNumber từ Intent
        partNumber = getIntent().getIntExtra(Constants.EXTRA_PART_NUMBER, 0);
        if (partNumber == 0) {
            Toast.makeText(this, "Invalid Part Number", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Received partNumber to display tests for: " + partNumber);

        apiService = ApiClient.getApiService();
        toolbar = findViewById(R.id.toolbar_test_list);
        recyclerViewTests = findViewById(R.id.recycler_view_tests);
        progressBar = findViewById(R.id.progress_bar_list);
        tvNoTests = findViewById(R.id.tv_no_tests);
        // tvListTitle = findViewById(R.id.tv_list_title); // Không cần nữa

        setupToolbar();
        setupRecyclerView();
        fetchTests(); // Gọi API lấy danh sách Test
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Đặt tiêu đề dựa trên partNumber
            getSupportActionBar().setTitle("Select Test - Part " + partNumber);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerViewTests.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter với partNumber nhận được
        adapter = new TestListAdapter(this, new ArrayList<>(), partNumber);
        recyclerViewTests.setAdapter(adapter);
    }

    // Hàm gọi API GET /tests
    private void fetchTests() {
        showLoading(true);
        Log.d(TAG, "Fetching all tests...");

        apiService.getAllTests().enqueue(new Callback<List<Test>>() {
            @Override
            public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Test> tests = response.body();
                    if (tests.isEmpty()) {
                        Log.d(TAG, "No tests found from API.");
                        tvNoTests.setText("No tests available."); // Thông báo chung
                        tvNoTests.setVisibility(View.VISIBLE);
                        recyclerViewTests.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "Successfully fetched " + tests.size() + " tests.");
                        // Không cần filter ở đây nữa, hiển thị tất cả Test nhận được
                        adapter.updateData(tests);
                        tvNoTests.setVisibility(View.GONE);
                        recyclerViewTests.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch tests. Code: " + response.code());
                    handleFetchError("Failed to load tests: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching tests: " + t.getMessage(), t);
                handleFetchError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewTests.setVisibility(isLoading ? View.GONE : View.VISIBLE); // Hiển thị RecyclerView khi không load
        tvNoTests.setVisibility(View.GONE); // Ẩn text lỗi khi đang load
    }

    private void handleFetchError(String message) {
        tvNoTests.setText(message);
        tvNoTests.setVisibility(View.VISIBLE);
        recyclerViewTests.setVisibility(View.GONE); // Ẩn RecyclerView khi lỗi
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Quay lại màn hình trước
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}