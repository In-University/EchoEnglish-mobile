package com.example.echoenglish_mobile.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.analyze_result.AnalyzeResultActivity;
import com.example.echoenglish_mobile.view.activity.chatbot.ConversationCategoriesActivity;
import com.example.echoenglish_mobile.view.activity.dictionary.DictionaryActivity;
import com.example.echoenglish_mobile.view.activity.document_hub.MainDocumentHubActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.MainFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.grammar.GrammarActivity;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.UploadSpeechActivity;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizActivity;
import com.example.echoenglish_mobile.view.activity.quiz.TestActivity;
import com.example.echoenglish_mobile.view.activity.writing_feedback.UploadNewWritingActivity;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText searchEditText;
    private CardView dictionaryCard, flashcardsCard, grammarCard, listeningCard;
    private CardView speechAnalyzeCard, aiConversationCard, documentHubCard, writingCard, reportCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setClickListeners();
        setupSearchListener();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        dictionaryCard = findViewById(R.id.dictionaryCard);
        flashcardsCard = findViewById(R.id.flashcardsCard);
        grammarCard = findViewById(R.id.grammarCard);
        listeningCard = findViewById(R.id.listeningCard);
        speechAnalyzeCard = findViewById(R.id.speechAnalyzeCard);
        aiConversationCard = findViewById(R.id.aiConversationCard);
        documentHubCard = findViewById(R.id.documentHubCard);
        writingCard = findViewById(R.id.writingCard);
        reportCard = findViewById(R.id.reportCard);
    }

    private void setClickListeners() {
        if (dictionaryCard != null) dictionaryCard.setOnClickListener(this);
        if (flashcardsCard != null) flashcardsCard.setOnClickListener(this);
        if (grammarCard != null) grammarCard.setOnClickListener(this);
        if (listeningCard != null) listeningCard.setOnClickListener(this);
        if (speechAnalyzeCard != null) speechAnalyzeCard.setOnClickListener(this);
        if (aiConversationCard != null) aiConversationCard.setOnClickListener(this);
        if (documentHubCard != null) documentHubCard.setOnClickListener(this);
        if (writingCard != null) writingCard.setOnClickListener(this);
        if (reportCard != null) reportCard.setOnClickListener(this);
    }

    private void setupSearchListener() {
        if (searchEditText == null) return;
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch() {
        if (searchEditText == null) return;
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            hideKeyboard();
            Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
            // intent.putExtra("SEARCH_QUERY", query);
            // startActivity(intent);
        } else {
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;

        if (id == R.id.dictionaryCard) {
            intent = new Intent(HomeActivity.this, DictionaryActivity.class);
        } else if (id == R.id.flashcardsCard) {
            intent = new Intent(HomeActivity.this, MainFlashcardActivity.class);
        } else if (id == R.id.grammarCard) {
            intent = new Intent(HomeActivity.this, GrammarActivity.class);
        } else if (id == R.id.listeningCard) {
            intent = new Intent(HomeActivity.this, MainQuizActivity.class);
        } else if (id == R.id.speechAnalyzeCard) {
            intent = new Intent(HomeActivity.this, UploadSpeechActivity.class);
        } else if (id == R.id.aiConversationCard) {
            intent = new Intent(HomeActivity.this, ConversationCategoriesActivity.class);
        } else if (id == R.id.documentHubCard) {
            intent = new Intent(HomeActivity.this, MainDocumentHubActivity.class);
        } else if (id == R.id.writingCard) {
            intent = new Intent(HomeActivity.this, UploadNewWritingActivity.class);
        } else if (id == R.id.reportCard) {
            intent = new Intent(HomeActivity.this, AnalyzeResultActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}