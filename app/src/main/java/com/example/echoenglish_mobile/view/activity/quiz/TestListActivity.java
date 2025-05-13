package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView; // Import TextView
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout; // Import ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapters.TestListAdapter;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.Constants;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment; // Import LoadingDialogFragment

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestListActivity extends AppCompatActivity {

    private static final String TAG = "TestListActivity";
    private static final String LOADING_DIALOG_TAG = "TestListLoadingDialog"; // Tag for loading dialog

    // Custom Header Views
    private ImageView backButton;
    private TextView textScreenTitle;

    // Main Content Views
    private ConstraintLayout contentContainer; // Container for RecyclerView and status message
    private RecyclerView recyclerViewTests;
    private TextView tvNoTests;

    // Logic
    private TestListAdapter adapter;
    private ApiService apiService;
    private int partNumber;

    // Loading Logic
    private int loadingApiCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        // Get partNumber from Intent
        partNumber = getIntent().getIntExtra(Constants.EXTRA_PART_NUMBER, 0);
        if (partNumber == 0) {
            Toast.makeText(this, "Invalid Part Number", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Received partNumber to display tests for: " + partNumber);

        findViews(); // Find views after setContentView
        setupCustomHeader(); // Setup custom header

        apiService = ApiClient.getApiService();

        setupRecyclerView(); // Setup RecyclerView before fetching data

        // Start loading process using DialogFragment
        startApiCall("Loading tests..."); // Start dialog with message

        fetchTests(); // Call API to fetch tests
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Reload the list on resume if data might change
        // Log.d(TAG, "onResume: TestListActivity does not auto-reload.");
    }


    // Find all views
    private void findViews() {
        // Custom Header
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        // Main Content Container & Views
        contentContainer = findViewById(R.id.contentContainer); // Find the container
        recyclerViewTests = findViewById(R.id.recycler_view_tests);
        tvNoTests = findViewById(R.id.tv_no_tests);

        // ProgressBar is managed by DialogFragment now
        // progressBar = findViewById(R.id.progress_bar_list); // Removed
    }

    // Setup custom header
    private void setupCustomHeader() {
        // Set title based on partNumber
        textScreenTitle.setText("Select Test - Part " + partNumber);
        backButton.setOnClickListener(v -> onBackPressed()); // Use onBackPressed for consistency
    }

    // Override onBackPressed to handle the back button press
    @Override
    public void onBackPressed() {
        // Usually, no confirmation is needed to leave this screen.
        super.onBackPressed();
    }


    private void setupRecyclerView() {
        recyclerViewTests.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with the partNumber received
        adapter = new TestListAdapter(this, new ArrayList<>(), partNumber);
        recyclerViewTests.setAdapter(adapter);
    }

    // Fetch tests from API
    private void fetchTests() {
        // startApiCall is already called before this method
        apiService.getAllTests().enqueue(new Callback<List<Test>>() {
            @Override
            public void onResponse(@NonNull Call<List<Test>> call, @NonNull Response<List<Test>> response) {
                finishApiCall(); // Finish loading regardless of success

                if (response.isSuccessful() && response.body() != null) {
                    List<Test> tests = response.body();
                    if (tests.isEmpty()) {
                        Log.d(TAG, "No tests found from API.");
                        showEmptyState("No tests available."); // Show empty state message
                    } else {
                        Log.d(TAG, "Successfully fetched " + tests.size() + " tests.");
                        // Update adapter with the fetched list (filtered by part number in adapter itself)
                        adapter.updateData(tests);
                        // adapter.getItemCount() now reflects filtered list count
                        if (adapter.getItemCount() > 0) {
                            showContent(); // Show RecyclerView
                        } else {
                            // No tests match the part number after filtering
                            showEmptyState("No tests available for Part " + partNumber + "."); // Specific message
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch tests. Code: " + response.code());
                    showErrorState("Failed to load tests: " + response.message()); // Show error state message
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Test>> call, @NonNull Throwable t) {
                finishApiCall(); // Finish loading on network failure
                Log.e(TAG, "Network error fetching tests: " + t.getMessage(), t);
                showErrorState("Network error: " + t.getMessage()); // Show error state message
            }
        });
    }


    // Helper to show RecyclerView and hide status message
    private void showContent() {
        recyclerViewTests.setVisibility(View.VISIBLE);
        tvNoTests.setVisibility(View.GONE); // Hide status message
        setUiEnabled(true); // Enable interaction
    }

    // Helper to show empty state message and hide RecyclerView
    private void showEmptyState(String message) {
        recyclerViewTests.setVisibility(View.GONE);
        tvNoTests.setText(message);
        tvNoTests.setVisibility(View.VISIBLE);
        setUiEnabled(true); // Enable interaction (back button)
    }

    // Helper to show error state message and hide RecyclerView
    private void showErrorState(String message) {
        recyclerViewTests.setVisibility(View.GONE);
        tvNoTests.setText(message); // Display the error message
        tvNoTests.setVisibility(View.VISIBLE);
        setUiEnabled(true); // Enable interaction (back button)
        // Optional: Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    // Helper to enable/disable main interactive elements (Header back button, RecyclerView items)
    private void setUiEnabled(boolean enabled) {
        backButton.setEnabled(enabled); // Enable/disable back button
        // RecyclerView item clicks are handled by the adapter's OnClickListener
        // You might need to manage RecyclerView's general touch events or overlay
        // if you need to completely block interaction during non-loading states.
        // For simplicity here, we primarily control the back button and show/hide content.
        // The adapter's click listener should check for valid data state before starting activity.
        // The loading dialog itself blocks interaction when visible.
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    // --- Loading Logic using DialogFragment ---
    // Only one startApiCall method accepting String message
    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            // Use getSupportFragmentManager() for DialogFragment
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false); // Disable UI during loading
            // Hide main content elements manually
            contentContainer.setVisibility(View.GONE);
            // Status message should be hidden when dialog is showing
            tvNoTests.setVisibility(View.GONE);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            // Use getSupportFragmentManager() for DialogFragment
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            // UI state (Content, Empty, Error) is set in fetchTests's onResponse/onFailure
            // setUiEnabled is called in start/finishApiCall
            setUiEnabled(true); // Re-enable UI after loading finishes

            // Show content container regardless - its children's visibility is handled by setUiState helpers
            contentContainer.setVisibility(View.VISIBLE);

            // Status message visibility is handled by showContent/showEmptyState/showErrorState
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure loading dialog is dismissed if activity is destroyed
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }
}