package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView; // Added for custom back button
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse; // Correct DTO for Flashcard Sets
// Import your Loading Dialog
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Make sure PublicFlashcardAdapter.OnPublicFlashcardClickListener interface takes FlashcardBasicResponse
public class PublicFlashcardsActivity extends AppCompatActivity implements PublicFlashcardAdapter.OnPublicFlashcardClickListener {

    private static final String ACTIVITY_TAG = "PublicFlashcards";
    public static final String CATEGORY_ID_EXTRA = "CATEGORY_ID";

    public static final String CATEGORY_NAME_EXTRA = "CATEGORY_NAME"; // Fix typo from prev versions? Using CATEGORY_NAME_EXTRA
    // Tag for the Loading Dialog Fragment
    private static final String LOADING_DIALOG_TAG = "PublicFlashcardsLoadingDialog";


    // Header Elements
    private ImageView backButton; // Custom back button
    private TextView textScreenTitle; // Custom title TextView

    private RecyclerView recyclerViewPublicFlashcards;
    // Adapter for listing Flashcard Sets (FlashcardBasicResponse)
    private PublicFlashcardAdapter adapter;
    private TextView textViewNoFlashcards;
    private ApiService apiService;

    private Long categoryId;
    private String categoryName;

    // Counter for tracking active API calls
    private int loadingApiCount = 0;
    // Layout containing the RecyclerView and TextView (to hide/show)
    private View contentFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_flashcards); // Using the layout with custom header

        // Ánh xạ View (Header)
        backButton = findViewById(R.id.backButton); // Find the custom back button
        textScreenTitle = findViewById(R.id.textScreenTitle); // Find the custom title TextView

        // Ánh xạ View (Content)
        recyclerViewPublicFlashcards = findViewById(R.id.recyclerViewPublicFlashcards);
        textViewNoFlashcards = findViewById(R.id.textViewNoPublicFlashcards);
        contentFrame = findViewById(R.id.contentFrame); // Find the FrameLayout wrapping content


        apiService = ApiClient.getApiService();

        categoryId = getIntent().getLongExtra(CATEGORY_ID_EXTRA, -1L);
        categoryName = getIntent().getStringExtra(CATEGORY_NAME_EXTRA);

        if (categoryId == -1L) {
            Log.e(ACTIVITY_TAG, "Invalid Category ID received.");
            Toast.makeText(this, "Invalid category.", Toast.LENGTH_LONG).show(); // Translated
            finish();
            return;
        }

        // Set the title in the custom header
        if (categoryName != null) {
            textScreenTitle.setText(categoryName);
        } else {
            textScreenTitle.setText("Public Flashcards"); // Default title
        }


        setupRecyclerView();
        loadPublicFlashcards();

        // Set listener for custom back button
        backButton.setOnClickListener(v -> finish());
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when Activity returns to foreground
        Log.d(ACTIVITY_TAG, "onResume: Loading public flashcards...");
        loadPublicFlashcards();
    }


    private void setupRecyclerView() {
        recyclerViewPublicFlashcards.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter for listing Flashcard Sets (FlashcardBasicResponse)
        // Assuming PublicFlashcardAdapter takes List<FlashcardBasicResponse> and listener
        adapter = new PublicFlashcardAdapter(this, new ArrayList<>(), this); // PublicFlashcardAdapter doesn't need isPublicContext flag itself
        recyclerViewPublicFlashcards.setAdapter(adapter);
    }

    // --- Loading Logic using DialogFragment ---

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
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading flashcards..."); // Use your dialog fragment
            // Hide content and disable interaction
            contentFrame.setVisibility(View.INVISIBLE); // Hide the content frame
            recyclerViewPublicFlashcards.setEnabled(false); // Disable RecyclerView interaction
            backButton.setEnabled(false); // Disable back button


        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG); // Hide your dialog fragment
            // Show content and re-enable interaction
            contentFrame.setVisibility(View.VISIBLE); // Show the content frame
            recyclerViewPublicFlashcards.setEnabled(true); // Re-enable RecyclerView interaction
            backButton.setEnabled(true); // Re-enable back button


            // Visibility of recyclerViewPublicFlashcards and textViewNoPublicFlashcards
            // inside contentFrame is handled in onResponse/onFailure by the original logic
        }
    }
    // --- End Loading Logic ---


    // Handle public flashcard set item click
    @Override
    public void onPublicFlashcardClick(FlashcardBasicResponse flashcard) { // Listener signature takes FlashcardBasicResponse
        // Open the detail screen for this flashcard set
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId());
        // <-- Truyền flag cho biết đây là bộ thẻ CÔNG KHAI -->
        intent.putExtra(FlashcardDetailActivity.EXTRA_IS_PUBLIC, true);
        // --> Kết thúc truyền flag -->
        startActivity(intent);
    }

    // API call to load Public Flashcard Sets
    private void loadPublicFlashcards() {
        startApiCall();

        apiService.getPublicFlashcardsByCategory(categoryId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                finishApiCall();
                if (response.isSuccessful() && response.body() != null) {
                    List<FlashcardBasicResponse> flashcards = response.body();
                    adapter.updateData(flashcards); // Assumes updateData takes List<FlashcardBasicResponse>
                    textViewNoFlashcards.setVisibility(flashcards.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewPublicFlashcards.setVisibility(flashcards.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load flashcards: " + response.code());
                    Toast.makeText(PublicFlashcardsActivity.this, "Failed to load flashcards.", Toast.LENGTH_SHORT).show();
                    textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    recyclerViewPublicFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error loading flashcards", t);
                Toast.makeText(PublicFlashcardsActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerViewPublicFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    // Removed showPurchaseDialog method
    // Removed findFlashcardPosition helper function
}