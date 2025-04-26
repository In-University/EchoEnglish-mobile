package com.example.echoenglish_mobile.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.Constants;
import com.example.echoenglish_mobile.view.activity.quiz.TestListAdapter;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTests;
    private TestListAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoTests;
    private TextView tvListTitle;
    private ApiService apiService;
    private int partType;
    private List<Test> allTests = new ArrayList<>(); // Store all tests fetched


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        partType = getIntent().getIntExtra(Constants.EXTRA_PART_TYPE, 0); // 1 or 5
        if (partType == 0) {
            Toast.makeText(this, "Invalid Part Type", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getApiService();
        recyclerViewTests = findViewById(R.id.recycler_view_tests);
        progressBar = findViewById(R.id.progress_bar_list);
        tvNoTests = findViewById(R.id.tv_no_tests);
        tvListTitle = findViewById(R.id.tv_list_title);

        tvListTitle.setText(String.format("Available Part %d Tests", partType));

        setupRecyclerView();
        fetchTests();
    }

    private void setupRecyclerView() {
        recyclerViewTests.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with empty list first
        adapter = new TestListAdapter(this, new ArrayList<>(), partType);
        recyclerViewTests.setAdapter(adapter);
    }

    private void fetchTests() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoTests.setVisibility(View.GONE);
        recyclerViewTests.setVisibility(View.GONE);

        apiService.getAllTests().enqueue(new Callback<List<Test>>() {
            @Override
            public void onResponse(Call<List<Test>> call, Response<List<Test>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allTests = response.body();
                    // Filter tests that actually contain the target part number
                    List<Test> filteredTests = allTests.stream()
                            .filter(test -> test.getParts() != null && test.getParts().stream()
                                    .anyMatch(part -> part.getPartNumber() != null && part.getPartNumber() == partType))
                            .collect(Collectors.toList());

                    if (!filteredTests.isEmpty()) {
                        adapter.updateData(filteredTests);
                        recyclerViewTests.setVisibility(View.VISIBLE);
                    } else {
                        tvNoTests.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvNoTests.setVisibility(View.VISIBLE);
                    Toast.makeText(TestListActivity.this, "Failed to load tests: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Test>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvNoTests.setVisibility(View.VISIBLE);
                Toast.makeText(TestListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace(); // Log the full error
            }
        });
    }
}