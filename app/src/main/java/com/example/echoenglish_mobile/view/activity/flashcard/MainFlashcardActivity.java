package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.echoenglish_mobile.R;

public class MainFlashcardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_flashcard);

        Button buttonMyDecks = findViewById(R.id.buttonGoToMyDecks);
        Button buttonPublicDecks = findViewById(R.id.buttonGoToPublicDecks);

        buttonMyDecks.setOnClickListener(v -> {
            Intent intent = new Intent(MainFlashcardActivity.this, MyFlashcardsActivity.class);
            startActivity(intent);
        });

        buttonPublicDecks.setOnClickListener(v -> {
            Intent intent = new Intent(MainFlashcardActivity.this, PublicCategoriesActivity.class);
            startActivity(intent);
        });
    }
}