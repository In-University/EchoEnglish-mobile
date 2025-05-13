package com.example.echoenglish_mobile.view.activity.flashcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implement the listener interface
public class FlashcardDetailActivity extends AppCompatActivity implements VocabularyAdapter.OnVocabularyActionsListener {

    private static final String ACTIVITY_TAG = "FlashcardDetail";
    public static final String FLASHCARD_ID = "FLASHCARD_ID";
    // <-- Thêm extra key để truyền flag ngữ cảnh -->
    public static final String EXTRA_IS_PUBLIC = "IS_PUBLIC_SET";
    // --> Kết thúc thêm extra key <--
    private static final String LOADING_DIALOG_TAG = "FlashcardDetailLoadingDialog";
    private VocabularyAdapter vocabularyAdapter;


    // Views
    private ImageView backButton;
    private TextView textScreenTitle;
    private ImageView imageViewDetailHeader;
    private RecyclerView recyclerViewVocabularies;
    private TextView textViewVocabulariesLabel;
    private TextView textViewNoVocabularies;
    private Button buttonLearn, buttonGame1, buttonGame2;
    private FloatingActionButton fabAddVocabulary;
    private SearchView searchViewVocabulary;
    private NestedScrollView contentScrollView;
    private View layoutDetailButtons;

    // Logic
    private ApiService apiService;
    private Long flashcardId;
    // <-- Field lưu trạng thái ngữ cảnh -->
    private boolean isPublicSet = false;
    // --> Kết thúc field trạng thái <--
    private int loadingApiCount = 0;


    // Launcher to get results from Add/Edit Vocabulary
    private final ActivityResultLauncher<Intent> vocabularyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(ACTIVITY_TAG, "Returned from Add/Edit Vocabulary with RESULT_OK. Reloading details...");
                    loadFlashcardDetails(); // Reload after Add or Edit successful
                } else {
                    Log.d(ACTIVITY_TAG, "Returned from Add/Edit Vocabulary without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);

        findViews();
        apiService = ApiClient.getApiService();

        // Lấy flashcardId và flag ngữ cảnh từ Intent
        flashcardId = getIntent().getLongExtra(FLASHCARD_ID, -1);
        // <-- Lấy flag ngữ cảnh -->
        isPublicSet = getIntent().getBooleanExtra(EXTRA_IS_PUBLIC, false); // Default là false (bộ thẻ của tôi)
        // --> Kết thúc lấy flag ngữ cảnh <--

        if (flashcardId == -1) {
            handleInvalidFlashcardId();
            return;
        }

        setupRecyclerView();
        setupSearchView();
        setupButtonClickListeners(); // Buttons Learn/Game

        loadFlashcardDetails(); // Tải dữ liệu chi tiết

        // FAB Add Vocabulary chỉ hiển thị cho bộ thẻ của người dùng
        // Visibility của FAB được set trong updateUI dựa trên categoryId,
        // nhưng cũng cần ẩn nếu là bộ thẻ công khai
        if (isPublicSet) {
            fabAddVocabulary.setVisibility(View.GONE);
        }
    }

    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);
        imageViewDetailHeader = findViewById(R.id.imageViewDetailHeader);
        recyclerViewVocabularies = findViewById(R.id.recyclerViewVocabularies);
        textViewVocabulariesLabel = findViewById(R.id.textViewVocabulariesLabel);
        textViewNoVocabularies = findViewById(R.id.textViewNoVocabularies);
        searchViewVocabulary = findViewById(R.id.searchViewVocabulary);
        layoutDetailButtons = findViewById(R.id.layoutDetailButtons);
        buttonLearn = findViewById(R.id.buttonLearn);
        buttonGame1 = findViewById(R.id.buttonGame1);
        buttonGame2 = findViewById(R.id.buttonGame2);
        fabAddVocabulary = findViewById(R.id.fabAddVocabularyDetail);

        // Set listener for custom back button
        backButton.setOnClickListener(v -> finish());
    }

    private void handleInvalidFlashcardId() {
        Log.e(ACTIVITY_TAG, "Invalid Flashcard ID received.");
        Toast.makeText(this, "Invalid flashcard set ID.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setupRecyclerView() {
        recyclerViewVocabularies.setLayoutManager(new LinearLayoutManager(this));
        // <-- Khởi tạo adapter và truyền flag ngữ cảnh -->
        vocabularyAdapter = new VocabularyAdapter(this, new ArrayList<>(), this, isPublicSet);
        // --> Kết thúc truyền flag ngữ cảnh <--
        recyclerViewVocabularies.setAdapter(vocabularyAdapter);
    }

    private void setupSearchView() {
        searchViewVocabulary.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (vocabularyAdapter != null) vocabularyAdapter.getFilter().filter(query);
                searchViewVocabulary.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (vocabularyAdapter != null) vocabularyAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupButtonClickListeners() {
        buttonLearn.setOnClickListener(v -> {
            if (vocabularyAdapter != null && vocabularyAdapter.getFullList() != null && !vocabularyAdapter.getFullList().isEmpty()) {
                Intent intent = new Intent(this, LearnActivity.class);
                intent.putExtra("VOCABULARY_LIST", new ArrayList<>(vocabularyAdapter.getFullList()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No vocabulary available for learning.", Toast.LENGTH_SHORT).show();
            }
        });
        buttonGame1.setOnClickListener(v -> openGameActivity(Game1Activity.class));
        buttonGame2.setOnClickListener(v -> openGameActivity(Game2Activity.class));
        // FAB listener được set trong onCreate
    }

    private void loadFlashcardDetails() {
        startApiCall();

        searchViewVocabulary.setQuery("", false);
        searchViewVocabulary.clearFocus();

        apiService.getFlashcardDetails(flashcardId).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    handleLoadError("Failed to load details: " + response.code());
                    updateUI(null);
                }
            }
            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                finishApiCall();
                handleLoadError("Network error while loading details.");
                Log.e(ACTIVITY_TAG, "Network error loading details", t);
                updateUI(null);
            }
        });
    }

    private void updateUI(FlashcardDetailResponse details) {
        String title = "Flashcard Set";
        List<VocabularyResponse> vocabs = new ArrayList<>();
        String imageUrl = null;
        boolean isUserDefinedSet = false; // Flag kiểm tra bộ thẻ do người dùng tạo

        if (details != null) {
            title = details.getName() != null ? details.getName() : title;
            vocabs = details.getVocabularies() != null ? details.getVocabularies() : new ArrayList<>();
            imageUrl = details.getImageUrl();
            // Giả định categoryId 1L là của user-defined flashcard
            isUserDefinedSet = details.getCategoryId() != null && details.getCategoryId().equals(1L);
        }

        textScreenTitle.setText(title);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(imageViewDetailHeader);

        vocabularyAdapter.updateData(vocabs);

        // Show/hide "no data" message and RecyclerView based on adapter's full list
        boolean hasVocabs = !vocabularyAdapter.getFullList().isEmpty();
        textViewNoVocabularies.setVisibility(hasVocabs ? View.GONE : View.VISIBLE);
        recyclerViewVocabularies.setVisibility(hasVocabs ? View.VISIBLE : View.GONE);

        // Update button states (Learn/Game) based on adapter's full list
        setButtonEnabled(buttonLearn, hasVocabs);
        setButtonEnabled(buttonGame1, hasVocabs);
        setButtonEnabled(buttonGame2, hasVocabs);

        // Visibility của FAB: Chỉ hiển thị nếu là bộ thẻ do user tự định nghĩa VÀ KHÔNG ở chế độ công khai
        // (Nếu là bộ thẻ công khai, FAB đã bị ẩn ngay từ onCreate)
        if (!isPublicSet) {
            fabAddVocabulary.setVisibility(isUserDefinedSet ? View.VISIBLE : View.GONE);
        } else {
            fabAddVocabulary.setVisibility(View.GONE); // Luôn ẩn FAB nếu ở chế độ công khai
        }
    }

    private void setButtonEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void handleLoadError(String message) {
        Log.e(ACTIVITY_TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading flashcard details...");

            // Hide content elements that might show old data or interfere
            recyclerViewVocabularies.setVisibility(View.GONE);
            textViewNoVocabularies.setVisibility(View.GONE);
            layoutDetailButtons.setVisibility(View.INVISIBLE);
            fabAddVocabulary.setVisibility(View.INVISIBLE); // Tạm ẩn FAB khi load

            // Disable interaction on fixed elements
            backButton.setEnabled(false);
            searchViewVocabulary.setEnabled(false);

        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);

            // Show content and re-enable interaction
            // Visibility của recyclerViewVocabularies và textViewNoVocabularies được updateUI xử lý sau khi load
            layoutDetailButtons.setVisibility(View.VISIBLE);
            // Visibility của FAB được updateUI xử lý

            backButton.setEnabled(true);
            searchViewVocabulary.setEnabled(true);

            // Button Learn/Game enabled state is handled by updateUI
        }
    }

    private void openGameActivity(Class<?> gameActivityClass) {
        List<VocabularyResponse> fullVocabularyList = null;
        if (vocabularyAdapter != null) {
            fullVocabularyList = vocabularyAdapter.getFullList();
        }

        if (fullVocabularyList == null || fullVocabularyList.isEmpty()) {
            Toast.makeText(this, "No vocabulary available to start the game.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, gameActivityClass);
        intent.putExtra("VOCABULARY_LIST", new ArrayList<>(fullVocabularyList));
        startActivity(intent);
    }

    private void openAddVocabularyActivity() {
        // Chỉ cho phép mở màn hình thêm nếu KHÔNG phải bộ thẻ công khai
        if (isPublicSet) {
            Log.w(ACTIVITY_TAG, "Attempted to open Add Vocabulary for a public set.");
            Toast.makeText(this, "Cannot add vocabulary to public sets.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AddVocabularyActivity.class);
        intent.putExtra(AddVocabularyActivity.EXTRA_PARENT_FLASHCARD_ID, flashcardId);
        // Không cần truyền EXTRA_IS_PUBLIC sang AddVocabulary vì màn hình Add chỉ liên quan đến bộ thẻ của mình
        vocabularyActivityResultLauncher.launch(intent);
    }


    // --- IMPLEMENT METHODS TỪ VocabularyAdapter.OnVocabularyActionsListener ---

    // Xử lý sự kiện xóa từ vựng
    @Override
    public void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition) {
        // Chỉ cho phép xóa nếu KHÔNG phải bộ thẻ công khai
        if (isPublicSet) {
            Log.w(ACTIVITY_TAG, "Attempted to delete vocabulary from a public set.");
            Toast.makeText(this, "Cannot delete vocabulary from public sets.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long vocabularyId = vocabulary.getId();
        if (vocabularyId == null) {
            Log.e(ACTIVITY_TAG, "Vocabulary ID is null, cannot delete.");
            return;
        }
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Delete vocabulary word '" + vocabulary.getWord() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteVocabularyApiCall(vocabularyId, originalPosition);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    // Xử lý sự kiện click vào item từ vựng (để EDIT hoặc xem chi tiết ở chế độ công khai)
    @Override
    public void onVocabularyItemClick(VocabularyResponse vocabulary) {
        if (vocabulary == null || vocabulary.getId() == null) {
            Log.e(ACTIVITY_TAG, "Cannot open vocabulary: item or its ID is null!");
            Toast.makeText(this, "Error: Cannot open this vocabulary.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu là bộ thẻ công khai, chỉ xem chi tiết (nếu có màn hình xem chi tiết từ vựng riêng)
        // Hiện tại, hành động này sẽ dẫn đến màn hình AddVocabulary ở chế độ Edit (nơi các trường bị disable)
        // Nếu muốn màn hình xem chi tiết riêng, cần tạo 1 VocabularyDetailActivity mới
        if (isPublicSet) {
            Log.d(ACTIVITY_TAG, "Viewing vocabulary details (Read-Only) for Vocab ID: " + vocabulary.getId() + ", Word: " + vocabulary.getWord());
            // Option 1: Show a Toast or a simple Dialog with details
            Toast.makeText(this, "Viewing '" + vocabulary.getWord() + "' (Read-Only).", Toast.LENGTH_SHORT).show();
            // Option 2: Navigate to a dedicated view-only Vocabulary Detail screen (Recommended if details are complex)
            // Intent viewIntent = new Intent(this, VocabularyDetailViewOnlyActivity.class);
            // viewIntent.putExtra(VocabularyDetailViewOnlyActivity.EXTRA_VOCABULARY, (Serializable) vocabulary);
            // startActivity(viewIntent);

        } else {
            // Nếu là bộ thẻ của mình, mở màn hình Sửa
            Log.d(ACTIVITY_TAG, "Starting edit for Vocab ID: " + vocabulary.getId() + ", Word: " + vocabulary.getWord());
            Intent intent = new Intent(this, AddVocabularyActivity.class);
            intent.putExtra(AddVocabularyActivity.EXTRA_EDIT_MODE, true);
            intent.putExtra(AddVocabularyActivity.EXTRA_VOCABULARY_TO_EDIT, (Serializable) vocabulary);
            intent.putExtra(AddVocabularyActivity.EXTRA_PARENT_FLASHCARD_ID, flashcardId); // Vẫn cần parent ID
            vocabularyActivityResultLauncher.launch(intent);
        }
    }


    // Gọi API để xóa từ vựng
    private void deleteVocabularyApiCall(Long vocabularyId, int originalPosition) {
        // Kiểm tra lại ngữ cảnh một lần nữa (an toàn)
        if (isPublicSet) {
            Log.w(ACTIVITY_TAG, "deleteVocabularyApiCall called for public set, but should be prevented by listener.");
            return;
        }

        startApiCall();

        apiService.deleteVocabulary(vocabularyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishApiCall();

                if (response.isSuccessful()) {
                    Toast.makeText(FlashcardDetailActivity.this, "Vocabulary deleted successfully.", Toast.LENGTH_SHORT).show();
                    // Reload the whole list after delete is usually safer
                    loadFlashcardDetails();
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to delete vocabulary: " + response.code());
                    Toast.makeText(FlashcardDetailActivity.this, "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show();
                    loadFlashcardDetails(); // Reload on failure
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error deleting vocabulary", t);
                Toast.makeText(FlashcardDetailActivity.this, "Network error while deleting.", Toast.LENGTH_SHORT).show();
                loadFlashcardDetails(); // Reload on network error
            }
        });
    }
}