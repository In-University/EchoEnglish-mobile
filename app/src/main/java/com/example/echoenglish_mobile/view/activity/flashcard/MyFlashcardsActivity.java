package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFlashcardsActivity extends AppCompatActivity implements
        FlashcardAdapter.OnFlashcardClickListener,
        FlashcardAdapter.OnFlashcardDeleteClickListener,
        FlashcardAdapter.OnFlashcardEditClickListener { // Thêm listener sửa

    private static final String TAG = "MyFlashcardsActivity";
    private RecyclerView recyclerViewMyDecks;
    private FlashcardAdapter myDecksAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreate;
    private TextView textViewNoDecks; // Thêm TextView báo rỗng
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);

        Toolbar toolbar = findViewById(R.id.toolbarMyFlashcards);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bộ thẻ của bạn"); // Hoặc lấy từ R.string
        }

        recyclerViewMyDecks = findViewById(R.id.recyclerViewMyDecks);
        progressBar = findViewById(R.id.progressBarMyFlashcards);
        fabCreate = findViewById(R.id.fabCreateFlashcard);
        textViewNoDecks = findViewById(R.id.textViewNoDecks);

        apiService = ApiClient.getApiService();

        setupRecyclerView();

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
            // Không cần truyền dữ liệu khi tạo mới
            startActivity(intent);
        });

        loadMyFlashcards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại khi quay về để cập nhật sau khi thêm/sửa
        loadMyFlashcards();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Xử lý nút back trên toolbar
        return true;
    }


    private void setupRecyclerView() {
        recyclerViewMyDecks.setLayoutManager(new LinearLayoutManager(this));
        // Truyền cả 3 listener vào Adapter
        myDecksAdapter = new FlashcardAdapter(this, new ArrayList<>(), this, this, this);
        recyclerViewMyDecks.setAdapter(myDecksAdapter);
    }

    private void loadMyFlashcards() {
        showLoading(true);
        textViewNoDecks.setVisibility(View.GONE); // Ẩn thông báo rỗng khi load
        apiService.getUserDefinedFlashcards().enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    myDecksAdapter.updateData(response.body());
                    // Hiển thị thông báo nếu danh sách rỗng
                    textViewNoDecks.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Lỗi tải bộ thẻ: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Lỗi tải bộ thẻ của bạn", Toast.LENGTH_SHORT).show();
                    textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE); // Hiển thị nếu vẫn rỗng
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi tải", t);
                Toast.makeText(MyFlashcardsActivity.this, "Lỗi mạng khi tải bộ thẻ của bạn", Toast.LENGTH_SHORT).show();
                textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE); // Hiển thị nếu vẫn rỗng
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerViewMyDecks.setVisibility(View.GONE); // Ẩn list khi loading
        } else {
            recyclerViewMyDecks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFlashcardClick(FlashcardBasicResponse flashcard) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId());
        startActivity(intent);
    }

    @Override
    public void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ thẻ '" + flashcard.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteFlashcardApiCall(flashcard.getId(), position))
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    // *** THÊM HÀM XỬ LÝ SỬA ***
    @Override
    public void onFlashcardEditClick(FlashcardBasicResponse flashcard, int position) {
        Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
        // Truyền dữ liệu cần sửa sang CreateFlashcardActivity
        intent.putExtra(CreateFlashcardActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_ID, flashcard.getId());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_NAME, flashcard.getName());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_IMAGE_URL, flashcard.getImageUrl());
        startActivity(intent);
    }

    private void deleteFlashcardApiCall(Long flashcardId, int position) {
        showLoading(true);
        apiService.deleteFlashcard(flashcardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false); // Ẩn loading ngay cả khi lỗi
                if (response.isSuccessful()) {
                    Toast.makeText(MyFlashcardsActivity.this, "Đã xóa bộ thẻ", Toast.LENGTH_SHORT).show();
                    myDecksAdapter.removeItem(position);
                    textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Lỗi xóa flashcard: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi mạng khi xóa", t);
                Toast.makeText(MyFlashcardsActivity.this, "Lỗi mạng khi xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
