package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
// import com.your_app_name.network.RetrofitClientInstance; // Xóa import cũ

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFlashcardActivity extends AppCompatActivity implements
        FlashcardAdapter.OnFlashcardClickListener,
        FlashcardAdapter.OnFlashcardDeleteClickListener {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerViewMyDecks, recyclerViewPublicDecks;
    private FlashcardAdapter myDecksAdapter, publicDecksAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreate;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flashcard);

        recyclerViewMyDecks = findViewById(R.id.recyclerViewMyDecks);
        recyclerViewPublicDecks = findViewById(R.id.recyclerViewPublicDecks);
        progressBar = findViewById(R.id.progressBarMain);
        fabCreate = findViewById(R.id.fabCreateFlashcard);

        apiService = ApiClient.getApiService(); // *** SỬ DỤNG ApiClient ***

        setupRecyclerViews();

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainFlashcardActivity.this, CreateFlashcardActivity.class);
            startActivity(intent);
        });

        loadFlashcards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFlashcards();
    }

    private void setupRecyclerViews() {
        recyclerViewMyDecks.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'this' for BOTH listeners to the adapter constructor
        myDecksAdapter = new FlashcardAdapter(this, new ArrayList<>(), this, this);
        recyclerViewMyDecks.setAdapter(myDecksAdapter);

        recyclerViewPublicDecks.setLayoutManager(new LinearLayoutManager(this));
        // Public decks might not need delete, pass null or handle appropriately
        publicDecksAdapter = new FlashcardAdapter(this, new ArrayList<>(), this, null);
        recyclerViewPublicDecks.setAdapter(publicDecksAdapter);
    }

    // *** Implement Delete Click Handling ***
    @Override
    public void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ thẻ '" + flashcard.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // User confirmed deletion
                    deleteFlashcardApiCall(flashcard.getId(), position);
                })
                .setNegativeButton("Hủy", null) // Do nothing on cancel
                .setIcon(R.drawable.ic_delete) // Optional: Set an icon
                .show();
    }

    // Method to call the delete API
    private void deleteFlashcardApiCall(Long flashcardId, int position) {
        showLoading(true); // Show progress
        apiService.deleteFlashcard(flashcardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) { // Status 204 No Content is success
                    Toast.makeText(MainFlashcardActivity.this, "Đã xóa bộ thẻ", Toast.LENGTH_SHORT).show();
                    // Remove item from the correct adapter based on where it was clicked
                    // For simplicity here, we assume it's always from myDecks.
                    // A more robust solution might check which adapter triggered the delete.
                    myDecksAdapter.removeItem(position);
                } else {
                    Log.e(TAG, "Failed to delete flashcard: " + response.code());
                    Toast.makeText(MainFlashcardActivity.this, "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error deleting flashcard", t);
                Toast.makeText(MainFlashcardActivity.this, "Lỗi mạng khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFlashcards() {
        showLoading(true);
        loadMyDecks();
        loadPublicDecks();
    }

    private void loadMyDecks() {
        apiService.getUserDefinedFlashcards().enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myDecksAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "Failed to load user decks: " + response.code());
                    Toast.makeText(MainFlashcardActivity.this, "Lỗi tải bộ thẻ của bạn", Toast.LENGTH_SHORT).show();
                }
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading user decks", t);
                Toast.makeText(MainFlashcardActivity.this, "Lỗi mạng khi tải bộ thẻ của bạn", Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void loadPublicDecks() {
        apiService.getPublicFlashcards().enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    publicDecksAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "Failed to load public decks: " + response.code());
                    Toast.makeText(MainFlashcardActivity.this, "Lỗi tải bộ thẻ công khai", Toast.LENGTH_SHORT).show();
                }
                checkLoadingComplete();
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading public decks", t);
                Toast.makeText(MainFlashcardActivity.this, "Lỗi mạng khi tải bộ thẻ công khai", Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private int loadingCounter = 0;
    private final int TOTAL_LOAD_REQUESTS = 2;

    private void checkLoadingComplete() {
        loadingCounter++;
        if (loadingCounter >= TOTAL_LOAD_REQUESTS) {
            showLoading(false);
            loadingCounter = 0;
        }
    }

    @Override
    public void onFlashcardClick(FlashcardBasicResponse flashcard) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId()); // Sử dụng constant từ DetailActivity
        startActivity(intent);
    }


}