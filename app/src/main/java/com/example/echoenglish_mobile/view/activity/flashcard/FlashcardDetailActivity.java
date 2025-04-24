package com.example.echoenglish_mobile.view.activity.flashcard; // Đảm bảo package đúng

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
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable; // Import Serializable nếu chưa có
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// *** IMPLEMENT LISTENER ĐÃ GỘP TỪ ADAPTER ***
public class FlashcardDetailActivity extends AppCompatActivity implements VocabularyAdapter.OnVocabularyActionsListener {

    private static final String TAG = "FlashcardDetail";
    public static final String FLASHCARD_ID = "FLASHCARD_ID"; // Key để nhận ID từ Intent

    // Views
    private ImageView imageViewHeader;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private RecyclerView recyclerViewVocabularies;
    private VocabularyAdapter vocabularyAdapter;
    private ProgressBar progressBar;
    private Button buttonLearn, buttonGame1, buttonGame2;
    private FloatingActionButton fabAddVocabulary;
    private SearchView searchViewVocabulary;

    // Logic
    private ApiService apiService;
    private Long flashcardId;

    // Launcher để nhận kết quả từ Add/Edit Vocabulary
    private final ActivityResultLauncher<Intent> vocabularyActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(TAG, "Returned from Add/Edit Vocabulary with RESULT_OK. Reloading details...");
                    loadFlashcardDetails(); // Load lại khi Thêm hoặc Sửa thành công
                } else {
                    Log.d(TAG, "Returned from Add/Edit Vocabulary without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);

        findViews(); // Gọi hàm tìm views
        apiService = ApiClient.getApiService(); // Khởi tạo ApiService

        // Lấy flashcardId từ Intent
        flashcardId = getIntent().getLongExtra(FLASHCARD_ID, -1);
        if (flashcardId == -1) {
            handleInvalidFlashcardId(); // Xử lý lỗi ID không hợp lệ
            return;
        }

        setupToolbar(); // Cài đặt Toolbar
        setupRecyclerView(); // Cài đặt RecyclerView
        setupSearchView(); // Cài đặt SearchView
        setupButtonClickListeners(); // Cài đặt Listeners cho các nút

        loadFlashcardDetails(); // Tải dữ liệu chi tiết
    }

    // Tìm tất cả các view
    private void findViews() {
        imageViewHeader = findViewById(R.id.imageViewDetailHeader);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbarDetail);
        recyclerViewVocabularies = findViewById(R.id.recyclerViewVocabularies);
        progressBar = findViewById(R.id.progressBarDetail);
        buttonLearn = findViewById(R.id.buttonLearn);
        buttonGame1 = findViewById(R.id.buttonGame1);
        buttonGame2 = findViewById(R.id.buttonGame2);
        fabAddVocabulary = findViewById(R.id.fabAddVocabularyDetail);
        searchViewVocabulary = findViewById(R.id.searchViewVocabulary);
    }

    // Xử lý khi flashcardId không hợp lệ
    private void handleInvalidFlashcardId() {
        Log.e(TAG, "Invalid Flashcard ID received.");
        Toast.makeText(this, "ID bộ thẻ không hợp lệ.", Toast.LENGTH_LONG).show();
        finish();
    }

    // Cài đặt Toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện nút back
            getSupportActionBar().setTitle(""); // Xóa tiêu đề mặc định, CollapsingToolbar sẽ xử lý
        }
    }

    // Cài đặt RecyclerView
    private void setupRecyclerView() {
        recyclerViewVocabularies.setLayoutManager(new LinearLayoutManager(this));
        // Khởi tạo adapter với listener là Activity này
        vocabularyAdapter = new VocabularyAdapter(this, new ArrayList<>(), this);
        recyclerViewVocabularies.setAdapter(vocabularyAdapter);
    }

    // Cài đặt SearchView
    private void setupSearchView() {
        searchViewVocabulary.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (vocabularyAdapter != null) vocabularyAdapter.getFilter().filter(query);
                searchViewVocabulary.clearFocus(); // Ẩn bàn phím
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (vocabularyAdapter != null) vocabularyAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    // Cài đặt các OnClickListener cho Button
    private void setupButtonClickListeners() {
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

    // Tải dữ liệu chi tiết flashcard từ API
    private void loadFlashcardDetails() {
        showLoading(true);
        searchViewVocabulary.setQuery("", false); // Xóa text tìm kiếm
        searchViewVocabulary.clearFocus(); // Bỏ focus khỏi search view

        apiService.getFlashcardDetails(flashcardId).enqueue(new Callback<FlashcardDetailResponse>() {
            @Override
            public void onResponse(Call<FlashcardDetailResponse> call, Response<FlashcardDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body()); // Cập nhật giao diện
                } else {
                    handleLoadError("Lỗi tải dữ liệu: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<FlashcardDetailResponse> call, Throwable t) {
                showLoading(false);
                handleLoadError("Lỗi mạng khi tải chi tiết");
                Log.e(TAG, "Network error loading details", t);
            }
        });
    }

    // Cập nhật giao diện sau khi load dữ liệu
    private void updateUI(FlashcardDetailResponse details) {
        collapsingToolbar.setTitle(details.getName()); // Đặt tiêu đề cho CollapsingToolbar

        // Load ảnh header bằng Glide
        Glide.with(this)
                .load(details.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(imageViewHeader);

        // Cập nhật dữ liệu cho Vocabulary Adapter
        List<VocabularyResponse> vocabs = details.getVocabularies() != null ? details.getVocabularies() : new ArrayList<>();
        vocabularyAdapter.updateData(vocabs); // Adapter quản lý list gốc và list lọc

        // Cập nhật trạng thái các nút bấm
        boolean hasVocabs = !vocabs.isEmpty(); // Kiểm tra list gốc có dữ liệu không
        setButtonEnabled(buttonLearn, hasVocabs);
        setButtonEnabled(buttonGame1, hasVocabs);
        setButtonEnabled(buttonGame2, hasVocabs);

        // Hiển thị/Ẩn FAB dựa trên categoryId
        boolean isUserDefined = details.getCategoryId() != null && details.getCategoryId() == 1L;
        fabAddVocabulary.setVisibility(isUserDefined ? View.VISIBLE : View.GONE);
    }

    // Hàm tiện ích để bật/tắt và đổi alpha cho Button
    private void setButtonEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }

    // Xử lý lỗi khi tải dữ liệu
    private void handleLoadError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Có thể hiển thị thông báo lỗi trên UI thay vì chỉ Toast
    }

    // Hiển thị/ẩn ProgressBar
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewVocabularies.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    // Mở màn hình Game
    private void openGameActivity(Class<?> gameActivityClass) {
        List<VocabularyResponse> fullVocabularyList = null;
        if (vocabularyAdapter != null) {
            fullVocabularyList = vocabularyAdapter.getFullList(); // Lấy list gốc từ adapter
        }

        if (fullVocabularyList == null || fullVocabularyList.isEmpty()) {
            Toast.makeText(this, "Không có từ vựng để bắt đầu game.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, gameActivityClass);
        // Truyền danh sách vocabulary (cần VocabularyResponse là Serializable)
        intent.putExtra("VOCABULARY_LIST", new ArrayList<>(fullVocabularyList));
        startActivity(intent);
    }

    // Mở màn hình Thêm Vocabulary (chế độ Add)
    private void openAddVocabularyActivity() {
        Intent intent = new Intent(this, AddVocabularyActivity.class);
        // Truyền parentFlashcardId để AddVocabularyActivity biết ngữ cảnh (không bắt buộc)
        intent.putExtra(AddVocabularyActivity.EXTRA_PARENT_FLASHCARD_ID, flashcardId);
        // Mở bằng launcher để nhận kết quả
        vocabularyActivityResultLauncher.launch(intent);
    }


    // --- IMPLEMENT METHODS TỪ VocabularyAdapter.OnVocabularyActionsListener ---

    // Xử lý sự kiện xóa từ vựng
    @Override
    public void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition) {
        Long vocabularyId = vocabulary.getId();
        if (vocabularyId == null) {
            Log.e(TAG, "Vocabulary ID is null, cannot delete.");
            return;
        }
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa từ vựng '" + vocabulary.getWord() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteVocabularyApiCall(vocabularyId, originalPosition); // Gọi API xóa
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_delete) // Icon thùng rác
                .show();
    }

    // Xử lý sự kiện click vào item từ vựng để EDIT
    @Override
    public void onVocabularyItemClick(VocabularyResponse vocabulary) {
        if (vocabulary == null || vocabulary.getId() == null) {
            Log.e(TAG, "Cannot start edit: Vocabulary or its ID is null!");
            Toast.makeText(this, "Lỗi: Không thể sửa từ vựng này.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Starting edit for Vocab ID: " + vocabulary.getId() + ", Word: " + vocabulary.getWord());
        Intent intent = new Intent(this, AddVocabularyActivity.class);
        intent.putExtra(AddVocabularyActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(AddVocabularyActivity.EXTRA_VOCABULARY_TO_EDIT, vocabulary); // Đảm bảo vocabulary không null
        intent.putExtra(AddVocabularyActivity.EXTRA_PARENT_FLASHCARD_ID, flashcardId);
        vocabularyActivityResultLauncher.launch(intent);
    }

    // Gọi API để xóa từ vựng
    private void deleteVocabularyApiCall(Long vocabularyId, int originalPosition) {
        showLoading(true); // Hiển thị loading
        apiService.deleteVocabulary(vocabularyId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(FlashcardDetailActivity.this, "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();
                    // Xóa item khỏi adapter bằng vị trí gốc
                    if (vocabularyAdapter != null) {
                        vocabularyAdapter.removeItem(originalPosition);
                        // Cập nhật trạng thái nút bấm
                        boolean hasVocabs = vocabularyAdapter.getItemCount() > 0;
                        setButtonEnabled(buttonLearn, hasVocabs);
                        setButtonEnabled(buttonGame1, hasVocabs);
                        setButtonEnabled(buttonGame2, hasVocabs);
                    }
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

    // Xử lý sự kiện nút back vật lý
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Hoạt động giống nút back của hệ thống
        return true;
    }
}