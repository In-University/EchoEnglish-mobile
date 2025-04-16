package com.example.echoenglish_mobile.view.activity.dashboard;

import android.content.Intent;
import android.content.SharedPreferences; // Thêm import
import android.os.Bundle;
import android.preference.PreferenceManager; // Thêm import
import android.widget.ImageView; // Thêm import
import android.widget.LinearLayout;
import android.widget.Toast; // Thêm import

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.DashboardAdapter;
import com.example.echoenglish_mobile.model.ListDomain;
// Đảm bảo import đúng LoginActivity của bạn
import com.example.echoenglish_mobile.view.activity.auth.LoginActivity;
import com.example.echoenglish_mobile.view.activity.webview.WebGameActivity;
import com.example.echoenglish_mobile.view.activity.chatbot.ChatActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.MainFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizzAppActivity;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerViewList;
    private SharedPreferences sharedPreferences; // Thêm SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        recyclerViewList = findViewById(R.id.view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        ArrayList<ListDomain> articles = new ArrayList<>();
        articles.add(new ListDomain("Article 1", "image_woman"));
        articles.add(new ListDomain("Article 2", "image_man"));
        articles.add(new ListDomain("Article 3", "image_woman"));
        articles.add(new ListDomain("Article 4", "image_man"));

        adapter = new DashboardAdapter(articles);
        recyclerViewList.setAdapter(adapter);

        // --- Sự kiện click cho các nút chức năng ---
        LinearLayout btnFlashcard = findViewById(R.id.btnFlashcard);
        btnFlashcard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MainFlashcardActivity.class)));

        LinearLayout btnQuizz = findViewById(R.id.btnQuizz);
        btnQuizz.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MainQuizzAppActivity.class)));

        LinearLayout btnWebGame = findViewById(R.id.btnWebGame);
        btnWebGame.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, WebGameActivity.class)));

        LinearLayout btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ChatActivity.class)));

        // --- Sự kiện click cho nút Logout ---
        ImageView btnLogout = findViewById(R.id.btnLogout); // Lấy tham chiếu nút Logout bằng ID
        btnLogout.setOnClickListener(v -> {
            performLogout(); // Gọi hàm xử lý logout
        });
    }

    // Hàm xử lý logout
    private void performLogout() {
        // Xóa token đã lưu
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("AUTH_TOKEN"); // Đảm bảo key "AUTH_TOKEN" giống với key khi lưu ở LoginActivity
        editor.apply(); // Áp dụng thay đổi

        // Hiển thị thông báo (tùy chọn)
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình Login và xóa các Activity trên stack
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class); // Đảm bảo LoginActivity được import đúng
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack cũ
        startActivity(intent);
        finish(); // Đóng DashboardActivity hiện tại
    }
}