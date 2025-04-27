package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;


public class MainQuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_quiz); // Đảm bảo dùng layout đúng

        Button btnPart1 = findViewById(R.id.btn_part1);
        Button btnPart5 = findViewById(R.id.btn_part5);

        // Sửa lại để mở TestListActivity
        btnPart1.setOnClickListener(v -> startTestListActivity(1));
        btnPart5.setOnClickListener(v -> startTestListActivity(5));
    }

    // Hàm mở TestListActivity
    private void startTestListActivity(int partNum) {
        Intent intent = new Intent(MainQuizActivity.this, TestListActivity.class);
        // Gửi partNumber để TestListActivity biết cần hiển thị gì hoặc lọc (nếu cần)
        // và để TestListAdapter biết cần gửi partNumber nào sang TestActivity
        intent.putExtra(Constants.EXTRA_PART_NUMBER, partNum);
        startActivity(intent);
    }
}
