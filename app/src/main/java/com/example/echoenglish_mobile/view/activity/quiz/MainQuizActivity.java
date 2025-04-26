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
        setContentView(R.layout.activity_main_quiz);

        Button btnPart1 = findViewById(R.id.btn_part1);
        Button btnPart5 = findViewById(R.id.btn_part5);
        Button btnHistory = findViewById(R.id.btn_view_history);

        // SỬA LẠI INTENT: Chỉ gửi partNumber đến TestActivity
        btnPart1.setOnClickListener(v -> startTestActivity(1));
        btnPart5.setOnClickListener(v -> startTestActivity(5));

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainQuizActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    // Hàm mới để khởi động TestActivity
    private void startTestActivity(int partNum) {
        Intent intent = new Intent(MainQuizActivity.this, TestActivity.class);
        // Chỉ cần gửi partNumber (hoặc có thể đặt tên là partType tùy ý)
        intent.putExtra(Constants.EXTRA_PART_NUMBER, partNum);
        startActivity(intent);
    }
}
