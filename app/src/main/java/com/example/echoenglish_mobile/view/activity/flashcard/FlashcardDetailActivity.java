package com.example.echoenglish_mobile.view.activity.flashcard;

// Import
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlashcardDetailActivity extends AppCompatActivity implements
        VocabularyAdapter.OnVocabularyDeleteClickListener {

    private static final String TAG = "FlashcardDetail";
    public static final String FLASHCARD_ID = "FLASHCARD_ID";

    private ImageView imageViewHeader;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private RecyclerView recyclerViewVocabularies;
    private VocabularyAdapter vocabularyAdapter;
    private ProgressBar progressBar;
    private Button buttonLearn, buttonAddVocabulary;

    private ApiService apiService;
    private Long flashcardId;
    private List<VocabularyResponse> currentVocabList = new ArrayList<>();

    // --- KHAI BÁO ActivityResultLauncher ---
    private final ActivityResultLauncher<Intent> addVocabularyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Returned from AddVocabularyActivity with RESULT_OK. Reloading details...");
                        loadFlashcardDetails(); // Load lại chi tiết flashcard
                    } else {
                        Log.d(TAG, "Returned from AddVocabularyActivity without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);

        // Ánh xạ view
        imageViewHeader = findViewById(R.id.imageViewDetailHeader);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbarDetail);
        recyclerViewVocabularies = findViewById(R.id.recyclerViewVocabularies);
        progressBar = findViewById(R.id.progressBarDetail);
        buttonLearn = findViewById(R.id.buttonLearn);
        buttonAddVocabulary = findViewById(R.id.buttonAddVocabulary);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = ApiClient.getApiService();
        flashcardId = getIntent().getLongExtra(FLASHCARD_ID, -1);

        if (flashcardId == -1) {
            Log.e(TAG, "Invalid Flashcard ID received.");
            Toast.makeText(this, "ID bộ thẻ không hợp lệ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupRecyclerView();
        loadFlashcardDetails();

        buttonLearn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LearnActivity.class);
            intent.putExtra(LearnActivity.FLASHCARD_ID_EXTRA, flashcardId);
            startActivity(intent);
        });

        buttonAddVocabulary.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVocabularyActivity.class);
            intent.putExtra(AddVocabularyActivity.FLASHCARD_ID_EXTRA, flashcardId);
            addVocabularyLauncher.launch(intent); // Sử dụng launcher để nhận kết quả
        });
    }

    private void setupRecyclerView() {
        recyclerViewVocabularies.setLayoutManager(new LinearLayoutManager(this));
        vocabularyAdapter = new VocabularyAdapter(this, currentVocabList, this);
        recyclerViewVocabularies.setAdapter(vocabularyAdapter);
    }

    private void loadFlashcardDetails() {
        showLoading(true);
        apiService.getFlashcardDetails(flashcardId).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Log.e(TAG, "Lỗi tải dữ liệu: " + response.code());
                    Toast.makeText(FlashcardDetailActivity.this, "Lỗi tải chi tiết bộ thẻ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi tải chi tiết", t);
                Toast.makeText(FlashcardDetailActivity.this, "Lỗi mạng khi tải chi tiết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FlashcardDetailResponse details) {
        collapsingToolbar.setTitle(details.getName());

        Glide.with(this)
                .load(details.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(imageViewHeader);

        if (details.getVocabularies() != null) {
            currentVocabList.clear();
            currentVocabList.addAll(details.getVocabularies());
            vocabularyAdapter.updateData(currentVocabList);

            buttonLearn.setEnabled(!currentVocabList.isEmpty());
            buttonLearn.setAlpha(currentVocabList.isEmpty() ? 0.5f : 1.0f);
        } else {
            buttonLearn.setEnabled(false);
            buttonLearn.setAlpha(0.5f);
        }

        boolean canAddVocab = details.getCategoryId() != null && details.getCategoryId() == 1L;
        buttonAddVocabulary.setEnabled(canAddVocab);
        buttonAddVocabulary.setVisibility(canAddVocab ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onVocabularyDeleteClick(VocabularyResponse vocabulary, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa từ vựng '" + vocabulary.getWord() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteVocabularyApiCall(vocabulary.getId(), position);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteVocabularyApiCall(Long vocabularyId, int position) {
        showLoading(true);
        apiService.deleteVocabulary(vocabularyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(FlashcardDetailActivity.this, "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();

                    currentVocabList.remove(position);
                    vocabularyAdapter.notifyItemRemoved(position);

                    buttonLearn.setEnabled(!currentVocabList.isEmpty());
                    buttonLearn.setAlpha(currentVocabList.isEmpty() ? 0.5f : 1.0f);
                } else {
                    Log.e(TAG, "Lỗi khi xóa từ vựng: " + response.code());
                    Toast.makeText(FlashcardDetailActivity.this, "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi xóa", t);
                Toast.makeText(FlashcardDetailActivity.this, "Lỗi mạng khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
