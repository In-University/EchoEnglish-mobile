package com.example.echoenglish_mobile.view.activity.flashcard;

// Removed static import - Good practice
// import static android.content.ContentValues.TAG; // Removed

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
// Removed: import android.widget.ProgressBar; // Use dialog instead
import android.widget.ImageView; // Added ImageView for custom header back button
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Added import
import androidx.activity.result.contract.ActivityResultContracts; // Added import
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
// Removed Toolbar import
// import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
// Import CreateFlashcardActivity and FlashcardDetailActivity
import com.example.echoenglish_mobile.view.activity.flashcard.CreateFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.FlashcardDetailActivity;
// Import your Loading Dialog
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Adapter interface signatures should match FlashcardAdapter
public class MyFlashcardsActivity extends AppCompatActivity implements
        FlashcardAdapter.OnFlashcardClickListener,
        FlashcardAdapter.OnFlashcardDeleteClickListener,
        FlashcardAdapter.OnFlashcardEditClickListener {

    private static final String ACTIVITY_TAG = "MyFlashcardsActivity";
    private static final String LOADING_DIALOG_TAG = "MyFlashcardsLoadingDialog"; // Tag for Loading Dialog

    // Header Elements
    private ImageView backButton; // Custom back button
    private TextView textScreenTitle; // Custom title TextView


    private RecyclerView recyclerViewMyDecks;
    private FlashcardAdapter myDecksAdapter;
    // Removed: private ProgressBar progressBar; // Use dialog instead
    private FloatingActionButton fabCreate;
    private TextView textViewNoDecks;

    private ApiService apiService;

    private View contentConstraintLayout; // Layout containing RecyclerView and TextView

    private int loadingApiCount = 0; // Counter for tracking active API calls

    // Launcher to get result from Create/Edit Flashcard Activity
    private final ActivityResultLauncher<Intent> flashcardActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(ACTIVITY_TAG, "Returned from Create/Edit Flashcard with RESULT_OK. Reloading list...");
                    loadMyFlashcards(); // Reload list when done
                } else {
                    Log.d(ACTIVITY_TAG, "Returned from Create/Edit Flashcard without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);

        // Removed Toolbar setup code
        // Toolbar toolbar = findViewById(R.id.toolbarMyFlashcards);
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setTitle("My Flashcards");
        // }

        // Ánh xạ View (Header)
        backButton = findViewById(R.id.backButton); // Find the custom back button
        textScreenTitle = findViewById(R.id.textScreenTitle); // Find the custom title TextView


        recyclerViewMyDecks = findViewById(R.id.recyclerViewMyDecks);
        // Removed: progressBar = findViewById(R.id.progressBarMyFlashcards);
        fabCreate = findViewById(R.id.fabCreateFlashcard);
        textViewNoDecks = findViewById(R.id.textViewNoDecks);
        // Assuming ConstraintLayout wraps content
        contentConstraintLayout = findViewById(R.id.contentConstraintLayout); // Find content layout


        apiService = ApiClient.getApiService();

        setupRecyclerView();

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
            // Use launcher to start and receive result
            flashcardActivityResultLauncher.launch(intent);
        });

        loadMyFlashcards();

        // Set title in the custom header
        textScreenTitle.setText("My Flashcards"); // Set title

        // Set listener for custom back button
        backButton.setOnClickListener(v -> finish());
    }

    // Removed onSupportNavigateUp method (for standard Toolbar)

    @Override
    protected void onResume() {
        super.onResume();
        // Reload when returning to update after adding/editing
        Log.d(ACTIVITY_TAG, "onResume: Loading my flashcards...");
        loadMyFlashcards();
    }


    private void setupRecyclerView() {
        recyclerViewMyDecks.setLayoutManager(new LinearLayoutManager(this));
        // Pass all 3 listeners into Adapter.
        // Note: This adapter (FlashcardAdapter) lists Flashcard Sets, not Vocabularies.
        // It needs to trigger FlashcardDetailActivity, CreateFlashcardActivity (for edit), and delete API.
        // It does NOT need the isPublicContext flag as it's only used for MY flashcards.
        myDecksAdapter = new FlashcardAdapter(this, new ArrayList<>(), this, this, this);
        recyclerViewMyDecks.setAdapter(myDecksAdapter);
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
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading your flashcards..."); // Use your dialog fragment
            // Hide content and disable interaction
            contentConstraintLayout.setVisibility(View.INVISIBLE); // Hide the layout holding RecyclerView and TextView
            fabCreate.setEnabled(false); // Disable FAB
            backButton.setEnabled(false); // Disable custom back button


        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG); // Hide your dialog fragment
            // Show content and re-enable interaction
            contentConstraintLayout.setVisibility(View.VISIBLE); // Show the layout holding RecyclerView and TextView
            fabCreate.setEnabled(true); // Re-enable FAB
            backButton.setEnabled(true); // Re-enable custom back button

            // The visibility of recyclerViewMyDecks and textViewNoDecks
            // inside contentConstraintLayout is handled in onResponse/onFailure
            // RecyclerView interaction is usually fine when dialog is gone
        }
    }
    // --- End Loading Logic ---


    private void loadMyFlashcards() {
        startApiCall(); // Start counting API calls and show loading

        apiService.getUserDefinedFlashcards().enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                finishApiCall(); // Finish counting API calls and hide loading
                if (response.isSuccessful() && response.body() != null) {
                    List<FlashcardBasicResponse> flashcards = response.body();
                    myDecksAdapter.updateData(flashcards); // Assumes updateData takes List<FlashcardBasicResponse>
                    // Hiển thị thông báo nếu danh sách rỗng
                    textViewNoDecks.setVisibility(flashcards.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewMyDecks.setVisibility(flashcards.isEmpty() ? View.GONE : View.VISIBLE); // Show list if not empty
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load flashcards: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Failed to load your flashcards.", Toast.LENGTH_SHORT).show(); // Translated
                    // Hiển thị nếu vẫn rỗng sau khi tải lỗi
                    textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    recyclerViewMyDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE); // Hide list on error if no items
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(ACTIVITY_TAG, "Network error loading flashcards", t);
                Toast.makeText(MyFlashcardsActivity.this, "Network error loading your flashcards.", Toast.LENGTH_SHORT).show(); // Translated
                // Hiển thị nếu vẫn rỗng sau lỗi mạng
                textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerViewMyDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE); // Hide list on error if no items
            }
        });
    }

    // Handle Flashcard item click (View Details)
    @Override
    public void onFlashcardClick(FlashcardBasicResponse flashcard) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId());
        // <-- Truyền flag cho biết đây là bộ thẻ CỦA MÌNH -->
        intent.putExtra(FlashcardDetailActivity.EXTRA_IS_PUBLIC, false);
        // --> Kết thúc truyền flag <--
        startActivity(intent);
    }

    // Handle Flashcard delete button click
    @Override
    public void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position) {
        // Logic xóa vẫn giữ nguyên cho My Flashcards
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete") // Translated
                .setMessage("Are you sure you want to delete the flashcard set '" + flashcard.getName() + "'?") // Translated
                .setPositiveButton("Delete", (dialog, which) -> deleteFlashcardApiCall(flashcard.getId(), position)) // Translated
                .setNegativeButton("Cancel", null) // Translated
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    // Handle Flashcard edit button click
    @Override
    public void onFlashcardEditClick(FlashcardBasicResponse flashcard, int position) {
        // Logic sửa vẫn giữ nguyên cho My Flashcards
        Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
        // Truyền dữ liệu cần sửa sang CreateFlashcardActivity
        intent.putExtra(CreateFlashcardActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_ID, flashcard.getId());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_NAME, flashcard.getName());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_IMAGE_URL, flashcard.getImageUrl());
        // Use launcher to start and receive result
        flashcardActivityResultLauncher.launch(intent);
    }

    private void deleteFlashcardApiCall(Long flashcardId, int position) {
        startApiCall(); // Start counting API calls and show loading

        apiService.deleteFlashcard(flashcardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishApiCall(); // Finish counting API calls and hide loading

                if (response.isSuccessful()) {
                    Toast.makeText(MyFlashcardsActivity.this, "Flashcard set deleted successfully.", Toast.LENGTH_SHORT).show(); // Translated
                    // Xóa item khỏi adapter
                    if (myDecksAdapter != null) {
                        // Assuming removeItem in FlashcardAdapter works with filtered position
                        // Reloading is safer than manual removal, especially with filtering/async
                        loadMyFlashcards(); // Reload the list
                    }
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to delete flashcard: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show(); // Translated
                    // If delete fails, reload the list to ensure UI reflects backend state
                    loadMyFlashcards();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                finishApiCall(); // Finish counting API calls and hide loading
                Log.e(ACTIVITY_TAG, "Network error deleting flashcard", t);
                Toast.makeText(MyFlashcardsActivity.this, "Network error while deleting.", Toast.LENGTH_SHORT).show(); // Translated
                // Reload the list on network error
                loadMyFlashcards();
            }
        });
    }
}