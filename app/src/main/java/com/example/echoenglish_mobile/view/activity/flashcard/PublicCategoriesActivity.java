package com.example.echoenglish_mobile.view.activity.flashcard;// Assuming this is the correct package

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
// Removed: import android.widget.ProgressBar; // No longer needed for layout ProgressBar
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// Removed Toolbar import
// import androidx.appcompat.widget.Toolbar; // Removed
// Removed MenuItem import - no longer using onOptionsItemSelected
// import android.view.MenuItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.CategoryAdapter;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.CategoryResponse; // Assuming this is the DTO for categories
// Assuming PublicFlashcardsActivity exists
import com.example.echoenglish_mobile.view.activity.flashcard.PublicFlashcardsActivity;
// Import your Loading Dialog
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implement the correct interface based on your Adapter and DTO
// Assuming CategoryAdapter takes List<CategoryResponse> and has OnCategoryClickListener<CategoryResponse>
public class PublicCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener<CategoryResponse> {

    private static final String ACTIVITY_TAG = "PublicCategories"; // Use a distinct TAG
    // Tag for the Loading Dialog Fragment
    private static final String LOADING_DIALOG_TAG = "PublicCategoriesLoadingDialog";


    // Header Elements (Matches the XML layout)
    private View backButton; // Use View or ImageView as appropriate for finding the clickable element
    private TextView textScreenTitle; // Text view for the screen title


    private RecyclerView recyclerViewCategories;
    // Ensure your CategoryAdapter is generic or uses CategoryResponse type
    private CategoryAdapter<CategoryResponse> categoryAdapter;
    // Removed: private ProgressBar progressBar; // Use dialog instead
    private TextView textViewNoCategories;

    private ApiService apiService;

    // Layout containing the RecyclerView and TextView (to hide/show)
    private View contentFrame;

    // Counter for tracking active API calls



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_categories);

        // Removed Toolbar setup code

        // Ánh xạ View (Header)
        backButton = findViewById(R.id.backButton); // Find the custom back button (ImageView)
        textScreenTitle = findViewById(R.id.textScreenTitle); // Find the custom title TextView
        // Set the title explicitly (or leave it in XML if preferred)
        textScreenTitle.setText("Flashcard Categories"); // Set title explicitly

        // Ánh xạ View (Content)
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        // Removed: progressBar = findViewById(R.id.progressBarCategories);
        textViewNoCategories = findViewById(R.id.textViewNoCategories);
        contentFrame = findViewById(R.id.contentFrame); // Find the FrameLayout wrapping content


        apiService = ApiClient.getApiService(); // Get ApiService instance

        setupRecyclerView(); // Setup RecyclerView and Adapter
        loadPublicCategories(); // Start loading data

        // Set listeners
        // Custom back button listener (Using finish() to go back)
        backButton.setOnClickListener(v -> finish());
    }

    // Removed onOptionsItemSelected and onSupportNavigateUp methods (for standard Toolbar)


    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when Activity returns to foreground
        Log.d(ACTIVITY_TAG, "onResume: Loading public categories...");
        loadPublicCategories();
    }


    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        // Initialize Adapter with correct data type and 'this' as listener
        categoryAdapter = new CategoryAdapter<>(this, new ArrayList<>(), category -> {
            // Listener action: Open PublicFlashcardsActivity for the clicked category
            if (category == null || category.getId() == null) {
                Log.e(ACTIVITY_TAG, "Category or Category ID is null on click.");
                Toast.makeText(this, "Cannot open this category.", Toast.LENGTH_SHORT).show(); // Translated
                return; // Exit if data is invalid
            }
            Log.d(ACTIVITY_TAG, "Category clicked: ID=" + category.getId() + ", Name=" + category.getName());
            Intent intent = new Intent(this, PublicFlashcardsActivity.class);
            // Pass ID and Name of the selected category to the next Activity
            intent.putExtra(PublicFlashcardsActivity.CATEGORY_ID_EXTRA, category.getId());
            intent.putExtra(PublicFlashcardsActivity.CATEGORY_NAME_EXTRA, category.getName());
            startActivity(intent); // Start the new screen
        });
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    // --- Loading Logic using DialogFragment ---

    private int loadingApiCount = 0;

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) { // Only show dialog on the first active call
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0; // Ensure it doesn't go negative
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            // Show the loading dialog
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading categories..."); // Use your dialog fragment with message
            // Hide content and disable interaction
            contentFrame.setVisibility(View.INVISIBLE);
            backButton.setEnabled(false); // Disable back button
            recyclerViewCategories.setEnabled(false); // Disable RecyclerView interaction
        } else {
            // Hide the loading dialog
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG); // Hide your dialog fragment
            // Show content and re-enable interaction
            contentFrame.setVisibility(View.VISIBLE); // Content frame visibility managed here
            backButton.setEnabled(true); // Re-enable back button
            recyclerViewCategories.setEnabled(true); // Re-enable RecyclerView interaction

            // The visibility of textViewNoCategories and recyclerViewCategories
            // inside contentFrame is handled in onResponse/onFailure
        }
    }
    // --- End Loading Logic ---


    private void loadPublicCategories() {
        startApiCall(); // Start counting API calls and show loading

        // Call API to get the list of public categories
        apiService.getPublicCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                finishApiCall(); // Finish counting API calls and hide loading

                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> categories = response.body();
                    // Update data for the Adapter
                    categoryAdapter.updateData(categories);

                    // Show/hide "no data" message and RecyclerView based on list content
                    textViewNoCategories.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewCategories.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);

                    Log.d(ACTIVITY_TAG, "Loaded " + categories.size() + " public categories.");
                } else {
                    // Handle server error
                    Log.e(ACTIVITY_TAG, "Failed to load categories: " + response.code() + " - " + response.message());
                    Toast.makeText(PublicCategoriesActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show(); // Translated

                    // Show "no data" message on error if the list is empty (e.g., first load failed)
                    // If there was existing data, it remains visible and the toast indicates the load failed.
                    if (categoryAdapter.getItemCount() == 0) {
                        textViewNoCategories.setVisibility(View.VISIBLE);
                        recyclerViewCategories.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                finishApiCall(); // Finish counting API calls and hide loading

                // Handle network error
                Log.e(ACTIVITY_TAG, "Network error loading categories", t);
                Toast.makeText(PublicCategoriesActivity.this, "Network error.", Toast.LENGTH_SHORT).show(); // Translated

                // Show "no data" message on network error if the list is empty
                if (categoryAdapter.getItemCount() == 0) {
                    textViewNoCategories.setVisibility(View.VISIBLE);
                    recyclerViewCategories.setVisibility(View.GONE);
                }
            }
        });
    }

    // Implementation of the listener method from CategoryAdapter.OnCategoryClickListener
    // This method is called when a category item is clicked in the RecyclerView
    @Override
    public void onCategoryClick(CategoryResponse category) {
        // The null checks are handled in the lambda within setupRecyclerView,
        // but can be kept here for extra safety if needed.
        // if (category == null || category.getId() == null) { /* handle */ return; }

        // Create Intent to open the public flashcard list screen
        Log.d(ACTIVITY_TAG, "Category clicked: ID=" + category.getId() + ", Name=" + category.getName());
        Intent intent = new Intent(this, PublicFlashcardsActivity.class);
        // Pass ID and Name of the selected category to the next Activity
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_ID_EXTRA, category.getId());
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_NAME_EXTRA, category.getName());
        startActivity(intent); // Start the new screen
    }
}