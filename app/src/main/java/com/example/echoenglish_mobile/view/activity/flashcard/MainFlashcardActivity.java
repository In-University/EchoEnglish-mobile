package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;

public class MainFlashcardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flashcard);

        Button viewFlashcardBtn = findViewById(R.id.viewFlashcardBtn);
        viewFlashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainFlashcardActivity.this, FolderListActivity.class);
                startActivity(intent);
            }
        });
    }
}
