package com.example.echoenglish_mobile.view.activity.flashcard;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Import SearchView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FAB

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
    private Button buttonLearn, buttonGame1, buttonGame2; // Thêm nút game
    private FloatingActionButton fabAddVocabulary; // Thêm FAB
    private SearchView searchViewVocabulary; // Thêm SearchView

    private ApiService apiService;
    private Long flashcardId;
    // Không cần lưu currentVocabList ở đây nữa, adapter sẽ quản lý list đã lọc và list gốc

    private final ActivityResultLauncher<Intent> addVocabularyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { // Dùng lambda cho gọn
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(TAG, "Returned from AddVocabularyActivity with RESULT_OK. Reloading details...");
                    loadFlashcardDetails(); // Load lại chi tiết flashcard
                } else {
                    Log.d(TAG, "Returned from AddVocabularyActivity without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);

        // --- Find Views ---
        imageViewHeader = findViewById(R.id.imageViewDetailHeader);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbarDetail);
        recyclerViewVocabularies = findViewById(R.id.recyclerViewVocabularies);
        progressBar = findViewById(R.id.progressBarDetail);
        buttonLearn = findViewById(R.id.buttonLearn);
        buttonGame1 = findViewById(R.id.buttonGame1); // Tìm nút Game 1
        buttonGame2 = findViewById(R.id.buttonGame2); // Tìm nút Game 2
        fabAddVocabulary = findViewById(R.id.fabAddVocabularyDetail); // Tìm FAB
        searchViewVocabulary = findViewById(R.id.searchViewVocabulary); // Tìm SearchView

        // --- Setup ---
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
        setupSearchView(); // Gọi hàm setup SearchView
        loadFlashcardDetails(); // Load dữ liệu ban đầu

        // --- Listeners ---
        buttonLearn.setOnClickListener(v -> {
            if (vocabularyAdapter != null && vocabularyAdapter.getItemCount() > 0) {
                Intent intent = new Intent(this, LearnActivity.class);
                intent.putExtra(LearnActivity.FLASHCARD_ID_EXTRA, flashcardId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có từ vựng để học.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonGame1.setOnClickListener(v -> openGameActivity(Game1Activity.class));
        buttonGame2.setOnClickListener(v -> openGameActivity(Game2Activity.class));
        fabAddVocabulary.setOnClickListener(v -> openAddVocabularyActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        recyclerViewVocabularies.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter với list rỗng ban đầu và listener xóa
        vocabularyAdapter = new VocabularyAdapter(this, new ArrayList<>(), this);
        recyclerViewVocabularies.setAdapter(vocabularyAdapter);
    }

    // --- Cài đặt SearchView ---
    private void setupSearchView() {
        searchViewVocabulary.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Lọc khi submit (hoặc không làm gì nếu lọc theo thời gian thực)
                if (vocabularyAdapter != null) {
                    vocabularyAdapter.getFilter().filter(query);
                }
                searchViewVocabulary.clearFocus(); // Ẩn bàn phím
                return true; // Đã xử lý
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Lọc danh sách mỗi khi text thay đổi
                if (vocabularyAdapter != null) {
                    vocabularyAdapter.getFilter().filter(newText);
                }
                return true; // Đã xử lý
            }
        });
    }
    // --- Kết thúc cài đặt SearchView ---

    private void loadFlashcardDetails() {
        showLoading(true);
        // Đặt lại bộ lọc tìm kiếm khi tải lại
        searchViewVocabulary.setQuery("", false);
        searchViewVocabulary.clearFocus();

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

        // Cập nhật dữ liệu cho Adapter (Adapter sẽ quản lý cả list gốc và list đã lọc)
        List<VocabularyResponse> vocabs = details.getVocabularies() != null ? details.getVocabularies() : new ArrayList<>();
        vocabularyAdapter.updateData(vocabs);

        // Cập nhật trạng thái các nút dựa trên danh sách TỪ VỰNG (lấy từ adapter sau khi update)
        boolean hasVocabs = vocabularyAdapter.getItemCount() > 0; // Kiểm tra list đã lọc (hoặc list gốc nếu muốn)
        buttonLearn.setEnabled(hasVocabs);
        buttonLearn.setAlpha(hasVocabs ? 1.0f : 0.5f);
        buttonGame1.setEnabled(hasVocabs);
        buttonGame1.setAlpha(hasVocabs ? 1.0f : 0.5f);
        buttonGame2.setEnabled(hasVocabs);
        buttonGame2.setAlpha(hasVocabs ? 1.0f : 0.5f);

        // Hiển thị/Ẩn FAB thêm từ dựa trên categoryId
        boolean isUserDefined = details.getCategoryId() != null && details.getCategoryId() == 1L;
        fabAddVocabulary.setVisibility(isUserDefined ? View.VISIBLE : View.GONE);
        // Ẩn nút button cũ (nếu có)
        // Button oldAddButton = findViewById(R.id.buttonAddVocabulary); // Tìm nút cũ nếu có
        // if(oldAddButton != null) oldAddButton.setVisibility(View.GONE);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewVocabularies.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE); // Ẩn/hiện RecyclerView
    }


    // --- Xử lý click nút Game ---
    private void openGameActivity(Class<?> gameActivityClass) {
        // Lấy danh sách gốc đầy đủ từ adapter để truyền cho game
        List<VocabularyResponse> fullVocabularyList = null;
        if (vocabularyAdapter != null) {
            fullVocabularyList = vocabularyAdapter.getFullList();
        }

        if (fullVocabularyList == null || fullVocabularyList.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng để bắt đầu game.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, gameActivityClass);
        // Truyền danh sách vocabulary sang Game Activity (dùng Serializable)
        intent.putExtra("VOCABULARY_LIST", new ArrayList<>(fullVocabularyList)); // Truyền bản sao ArrayList
        // intent.putExtra("FLASHCARD_ID", flashcardId); // Vẫn có thể truyền ID nếu cần
        startActivity(intent);
    }
    // --- Kết thúc xử lý click nút Game ---

    // --- Xử lý click nút Thêm Vocab (FAB) ---
    private void openAddVocabularyActivity() {
        Intent intent = new Intent(this, AddVocabularyActivity.class);
        intent.putExtra(AddVocabularyActivity.FLASHCARD_ID_EXTRA, flashcardId);
        addVocabularyLauncher.launch(intent);
    }
    // --- Kết thúc xử lý nút Thêm Vocab ---


    // --- Xử lý xóa Vocabulary ---
    @Override
    public void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition) {
        // Lấy ID từ đối tượng vocabulary
        Long vocabularyId = vocabulary.getId();
        if (vocabularyId == null) {
            Log.e(TAG, "Vocabulary ID is null, cannot delete.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa từ vựng '" + vocabulary.getWord() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Truyền ID và originalPosition (vị trí trong list gốc)
                    deleteVocabularyApiCall(vocabularyId, originalPosition);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteVocabularyApiCall(Long vocabularyId, int originalPosition) {
        showLoading(true); // Hiển thị loading khi xóa
        apiService.deleteVocabulary(vocabularyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(FlashcardDetailActivity.this, "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();
                    // Gọi hàm removeItem của adapter với vị trí trong list GỐC
                    vocabularyAdapter.removeItem(originalPosition);

                    // Cập nhật trạng thái các nút nếu cần
                    boolean hasVocabs = vocabularyAdapter.getItemCount() > 0; // Kiểm tra list đã lọc còn item không
                    buttonLearn.setEnabled(hasVocabs);
                    buttonGame1.setEnabled(hasVocabs);
                    buttonGame2.setEnabled(hasVocabs);
                    buttonLearn.setAlpha(hasVocabs ? 1.0f : 0.5f);
                    buttonGame1.setAlpha(hasVocabs ? 1.0f : 0.5f);
                    buttonGame2.setAlpha(hasVocabs ? 1.0f : 0.5f);
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
    // --- Kết thúc xử lý xóa Vocabulary ---
}