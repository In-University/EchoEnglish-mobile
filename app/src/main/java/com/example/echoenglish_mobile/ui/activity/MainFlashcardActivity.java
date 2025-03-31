package com.example.echoenglish_mobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.flashcard.about.AboutActivity;
import com.example.echoenglish_mobile.ui.flashcard.folders.FolderListActivity;

public class MainFlashcardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flashcard);

        Button viewFlashcardBtn = findViewById(R.id.viewFlashcardBtn);
        Button aboutBtn = findViewById(R.id.aboutBtn);
        viewFlashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFlashcardActivity.this, FolderListActivity.class);
                startActivity(intent);
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFlashcardActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}
