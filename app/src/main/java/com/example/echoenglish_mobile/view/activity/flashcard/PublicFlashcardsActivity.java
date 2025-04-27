package com.example.echoenglish_mobile.view.activity.flashcard;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Import
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.PurchaseManager; // Import
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicFlashcardsActivity extends AppCompatActivity implements PublicFlashcardAdapter.OnPublicFlashcardClickListener {

    private static final String TAG = "PublicFlashcards";
    public static final String CATEGORY_ID_EXTRA = "CATEGORY_ID";
    public static final String CATEGORY_NAME_EXTRA = "CATEGORY_NAME";

    private RecyclerView recyclerViewPublicFlashcards;
    private PublicFlashcardAdapter adapter;
    private ProgressBar progressBar;
    private TextView textViewNoFlashcards;
    private ApiService apiService;
    private PurchaseManager purchaseManager;
    private Long categoryId;
    private String categoryName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_flashcards);

        Toolbar toolbar = findViewById(R.id.toolbarPublicFlashcards);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewPublicFlashcards = findViewById(R.id.recyclerViewPublicFlashcards);
        progressBar = findViewById(R.id.progressBarPublicFlashcards);
        textViewNoFlashcards = findViewById(R.id.textViewNoPublicFlashcards);
        apiService = ApiClient.getApiService();
        purchaseManager = new PurchaseManager(this); // Khởi tạo PurchaseManager

        categoryId = getIntent().getLongExtra(CATEGORY_ID_EXTRA, -1L);
        categoryName = getIntent().getStringExtra(CATEGORY_NAME_EXTRA);

        if (categoryId == -1L) {
            Log.e(TAG, "Invalid Category ID received.");
            Toast.makeText(this, "Danh mục không hợp lệ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null && categoryName != null) {
            getSupportActionBar().setTitle(categoryName); // Set tiêu đề Toolbar
        }

        setupRecyclerView();
        loadPublicFlashcards();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ** GỌI TẢI DỮ LIỆU KHI ACTIVITY QUAY TRỞ LẠI FOREGROUND **
        Log.d(TAG, "onResume: Loading public flashcards...");
        loadPublicFlashcards();
    }


    private void setupRecyclerView() {
        recyclerViewPublicFlashcards.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicFlashcardAdapter(this, new ArrayList<>(), this);
        recyclerViewPublicFlashcards.setAdapter(adapter);
    }

    private void loadPublicFlashcards() {
        showLoading(true);
        textViewNoFlashcards.setVisibility(View.GONE);

        apiService.getPublicFlashcardsByCategory(categoryId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                    textViewNoFlashcards.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Lỗi tải flashcards: " + response.code());
                    Toast.makeText(PublicFlashcardsActivity.this, "Lỗi tải bộ thẻ", Toast.LENGTH_SHORT).show();
                    textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi tải flashcards", t);
                Toast.makeText(PublicFlashcardsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewPublicFlashcards.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }


    // Xử lý click vào item flashcard công khai
    @Override
    public void onPublicFlashcardClick(FlashcardBasicResponse flashcard, boolean isPurchased) {
        if (isPurchased) {
            // Đã mua -> Mở màn hình chi tiết
            openFlashcardDetail(flashcard.getId());
        } else {
            // Chưa mua -> Hiển thị dialog xác nhận "Purchase"
            showPurchaseDialog(flashcard);
        }
    }

    private void showPurchaseDialog(FlashcardBasicResponse flashcard) {
        new AlertDialog.Builder(this)
                .setTitle("Mở khóa bộ thẻ")
                .setMessage("Bạn có muốn mở khóa bộ thẻ '" + flashcard.getName() + "' để xem và học không?")
                .setPositiveButton("Mở khóa", (dialog, which) -> {
                    // Giả lập purchase thành công
                    purchaseManager.setPurchased(flashcard.getId(), true);
                    // Cập nhật lại giao diện (adapter sẽ tự cập nhật khi scroll hoặc dùng notifyItemChanged)
                    // Tìm vị trí item và cập nhật lại nó
                    int position = findFlashcardPosition(flashcard.getId());
                    if (position != -1) {
                        adapter.notifyItemChanged(position); // Cập nhật item để đổi icon khóa
                    }
                    Toast.makeText(this, "Đã mở khóa!", Toast.LENGTH_SHORT).show();
                    // Tùy chọn: Mở luôn màn hình chi tiết sau khi mở khóa
                    // openFlashcardDetail(flashcard.getId());
                })
                .setNegativeButton("Để sau", null)
                .setIcon(R.drawable.ic_xml_lock_open_24px) // Icon mở khóa
                .show();
    }

    // Hàm helper để tìm vị trí item trong adapter (cần tối ưu nếu list lớn)
    private int findFlashcardPosition(Long flashcardId) {
        if (flashcardId == null || adapter == null) return -1;
        List<FlashcardBasicResponse> currentList = adapter.getCurrentList(); // Cần thêm hàm này vào adapter
        if (currentList == null) return -1;
        for (int i = 0; i < currentList.size(); i++) {
            if (flashcardId.equals(currentList.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    // Hàm helper để mở màn hình chi tiết
    private void openFlashcardDetail(Long flashcardId) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcardId);
        startActivity(intent);
    }

}
