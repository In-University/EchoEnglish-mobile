package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.CategoryResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.model.Category;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// *** Implement đúng interface với đúng kiểu dữ liệu ***
public class PublicCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener<CategoryResponse> {

    private static final String TAG = "PublicCategories";
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter<CategoryResponse> categoryAdapter; // *** Đặt kiểu dữ liệu cho Adapter ***
    private ProgressBar progressBar;
    private TextView textViewNoCategories;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_categories);

        Toolbar toolbar = findViewById(R.id.toolbarPublicCategories);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh mục công khai"); // Đặt tiêu đề
        }

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        progressBar = findViewById(R.id.progressBarCategories);
        textViewNoCategories = findViewById(R.id.textViewNoCategories);
        apiService = ApiClient.getApiService(); // Lấy instance ApiService

        setupRecyclerView(); // Cài đặt RecyclerView và Adapter
        loadPublicCategories(); // Bắt đầu tải dữ liệu
    }

    // Xử lý sự kiện nhấn nút back trên Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Hành động mặc định là quay lại màn hình trước
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Cần override onSupportNavigateUp để nút back hoạt động nhất quán
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        // *** Khởi tạo Adapter với kiểu dữ liệu đúng và listener là 'this' ***
        categoryAdapter = new CategoryAdapter<>(this, new ArrayList<>(), this);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void loadPublicCategories() {
        showLoading(true); // Hiển thị ProgressBar
        textViewNoCategories.setVisibility(View.GONE); // Ẩn thông báo không có dữ liệu

        // Gọi API để lấy danh sách category công khai
        apiService.getPublicCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                showLoading(false); // Ẩn ProgressBar
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật dữ liệu cho Adapter
                    categoryAdapter.updateData(response.body());
                    // Hiển thị thông báo nếu danh sách rỗng
                    textViewNoCategories.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                    Log.d(TAG, "Loaded " + response.body().size() + " public categories.");
                } else {
                    // Xử lý lỗi từ server
                    Log.e(TAG, "Lỗi tải category: " + response.code() + " - " + response.message());
                    Toast.makeText(PublicCategoriesActivity.this, "Lỗi tải danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                    textViewNoCategories.setVisibility(View.VISIBLE); // Hiển thị thông báo lỗi
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                showLoading(false); // Ẩn ProgressBar
                // Xử lý lỗi mạng
                Log.e(TAG, "Lỗi mạng khi tải category", t);
                Toast.makeText(PublicCategoriesActivity.this, "Lỗi mạng khi tải danh mục", Toast.LENGTH_SHORT).show();
                textViewNoCategories.setVisibility(View.VISIBLE); // Hiển thị thông báo lỗi
            }
        });
    }

    // Hàm tiện ích để hiển thị/ẩn ProgressBar và RecyclerView
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewCategories.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE); // Ẩn/hiện list
    }

    // *** IMPLEMENT PHƯƠNG THỨC TỪ INTERFACE LISTENER ***
    @Override
    public void onCategoryClick(CategoryResponse category) { // *** Đảm bảo đúng tên và kiểu tham số ***
        if (category == null || category.getId() == null) {
            Log.e(TAG, "Category hoặc Category ID là null khi click.");
            Toast.makeText(this, "Không thể mở danh mục này.", Toast.LENGTH_SHORT).show();
            return; // Thoát nếu dữ liệu không hợp lệ
        }

        // Tạo Intent để mở màn hình danh sách flashcard công khai
        Log.d(TAG, "Category clicked: ID=" + category.getId() + ", Name=" + category.getName());
        Intent intent = new Intent(this, PublicFlashcardsActivity.class);
        // Truyền ID và Tên của category được chọn sang Activity tiếp theo
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_ID_EXTRA, category.getId());
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_NAME_EXTRA, category.getName());
        startActivity(intent); // Mở màn hình mới
    }
}