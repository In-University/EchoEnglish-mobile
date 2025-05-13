package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.View; // Import View
import android.widget.ImageView; // Import ImageView
import android.widget.TextView; // Import TextView
import android.widget.Toast; // Import Toast
import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient; // Example, assume API client might be used later
import com.example.echoenglish_mobile.network.ApiService; // Example, assume API service might be used later
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment; // Import LoadingDialogFragment
import com.google.android.material.card.MaterialCardView; // Import MaterialCardView


public class MainQuizActivity extends AppCompatActivity {

    private static final String TAG = "MainQuizActivity";
    private static final String LOADING_DIALOG_TAG = "MainQuizLoadingDialog"; // Tag for loading dialog

    // Custom Header Views
    private ImageView backButton;
    private TextView textScreenTitle;

    // Main Content Views (Cards)
    private MaterialCardView cardPart1;
    private MaterialCardView cardPart5;

    // Removed: Button btnPart1, btnPart5

    // Loading Logic (assuming API calls might be needed later for counts or data)
    private int loadingApiCount = 0;
    // Added ScrollView reference for show/hide content logic (as in SpacedRepetitionActivity)
    private View contentScrollView; // Use View as type for ScrollView or ConstraintLayout if it's the main content holder


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz);

        findViews();
        setupCustomHeader();

        // Assume you might need to load some initial data or counts later
        // If so, you'd call startApiCall() here and finishApiCall() in API callbacks.
        // For now, since there are no API calls on this screen, we just manage UI visibility.

        // Initial UI state: show content, hide status messages (if any)
        setUiEnabled(true); // Enable interaction
        // Assuming you have a ScrollView or ConstraintLayout wrapping the main content
        contentScrollView.setVisibility(View.VISIBLE);
        // If you have other status messages (like empty state), hide them
        // textStatusMessage.setVisibility(View.GONE);


        // Set listeners for the MaterialCardViews
        cardPart1.setOnClickListener(v -> startTestListActivity(1));
        cardPart5.setOnClickListener(v -> startTestListActivity(5));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: If you need to refresh data on resume, call startApiCall and fetch data.
        // Log.d(TAG, "onResume: MainQuizActivity does not auto-reload data.");
    }


    // Find all views
    private void findViews() {
        // Custom Header
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        // Main Content (Cards)
        cardPart1 = findViewById(R.id.cardPart1);
        cardPart5 = findViewById(R.id.cardPart5);

        // ScrollView wrapping content
        contentScrollView = findViewById(R.id.contentScrollView); // Find the ScrollView

        // Assuming you might have a TextView for general status messages (optional)
        // textStatusMessage = findViewById(R.id.textStatusMessage);
    }

    // Setup custom header
    private void setupCustomHeader() {
        textScreenTitle.setText("Quiz"); // Set screen title
        backButton.setOnClickListener(v -> onBackPressed()); // Use onBackPressed for consistency
    }

    // Override onBackPressed to handle the back button press
    @Override
    public void onBackPressed() {
        // Usually, no confirmation is needed to leave this screen.
        super.onBackPressed();
    }


    // Helper to enable/disable main interactive elements
    private void setUiEnabled(boolean enabled) {
        cardPart1.setEnabled(enabled);
        cardPart5.setEnabled(enabled);
        backButton.setEnabled(enabled);
        // If you have other interactive elements, add them here
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    // Method to start the TestListActivity
    private void startTestListActivity(int partNum) {
        // Optional: If loading data is needed before starting next activity,
        // call startApiCall here and disable UI, then start intent in API callback.
        // For now, assuming no loading is needed before transition.

        Intent intent = new Intent(MainQuizActivity.this, TestListActivity.class);
        // Send partNumber to TestListActivity
        intent.putExtra("EXTRA_PART_NUMBER", partNum); // Use a constant from your Constants file if available
        startActivity(intent);
    }


    // --- Loading Logic using DialogFragment (Adapted from your snippet) ---
    // Kept the structure in case you add API calls later
    private synchronized void startApiCall() {
        startApiCall("Loading..."); // Default message
    }

    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            // Use getSupportFragmentManager() for DialogFragment
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false); // Disable UI during loading
            // Hide content containers manually if not using DialogFragment overlay
            // In this setup, DialogFragment overlays, so hiding content is optional
            // but good practice to prevent interaction/visual glitches.
            if (contentScrollView != null) contentScrollView.setVisibility(View.INVISIBLE);
            // If you have a separate status message TextView for "Loading...", show it here if not using dialog message
            // textStatusMessage.setText(displayMessage);
            // textStatusMessage.setVisibility(View.VISIBLE);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            // Use getSupportFragmentManager() for DialogFragment
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            setUiEnabled(true); // Re-enable UI after loading

            // Show content containers manually
            if (contentScrollView != null) contentScrollView.setVisibility(View.VISIBLE);
            // If you have a separate status message TextView, hide it
            // textStatusMessage.setVisibility(View.GONE);

            // If you had logic to show empty/error state after loading, it would go here
            // e.g., if (dataList == null || dataList.isEmpty()) showEmptyState();
        }
    }
    // --- End Loading Logic ---


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure loading dialog is dismissed if activity is destroyed
        // This check prevents crashing if onDestroy is called while dialog is showing
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }
}