package com.example.echoenglish_mobile.view.base;

import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.echoenglish_mobile.R;

public class BaseActivity extends AppCompatActivity { // Dùng để tái sử dụng toolbar (thanh tiêu đề ở cạnh trên màn hình ) trong cái flashcard,
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }
}
