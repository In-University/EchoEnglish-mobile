package com.example.echoenglish_mobile.view.activity.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.DashboardAdapter;
import com.example.echoenglish_mobile.model.ListDomain;
import com.example.echoenglish_mobile.view.activity.webview.WebGameActivity;
import com.example.echoenglish_mobile.view.activity.chatbot.ChatActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.MainFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.quiz.MainQuizzAppActivity;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerViewList = findViewById(R.id.view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        ArrayList<ListDomain> articles = new ArrayList<>();
        articles.add(new ListDomain("Caitlyn Jenner in \"Hell no\"", "image_woman"));
        articles.add(new ListDomain("Gov. Brian kemp", "image_man"));
        articles.add(new ListDomain("US-China War", "image_woman"));
        articles.add(new ListDomain("Caitlyn Jenner in \"Hell no\"", "image_man"));

        adapter = new DashboardAdapter(articles);
        recyclerViewList.setAdapter(adapter);

        LinearLayout btnFlashcard = findViewById(R.id.btnFlashcard);
        btnFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainFlashcardActivity.class);
            startActivity(intent);
        });

        LinearLayout btnQuizz = findViewById(R.id.btnQuizz);
        btnQuizz.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainQuizzAppActivity.class);
            startActivity(intent);
        });

        LinearLayout btnWebGame = findViewById(R.id.btnWebGame);
        btnWebGame.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, WebGameActivity.class);
            startActivity(intent);
        });

        LinearLayout btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChatActivity.class);
            startActivity(intent);
        });

    }

}
