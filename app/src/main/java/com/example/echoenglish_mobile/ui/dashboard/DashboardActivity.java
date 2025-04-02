package com.example.echoenglish_mobile.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.activity.MainActivity;
import com.example.echoenglish_mobile.ui.activity.MainFlashcardActivity;
import com.example.echoenglish_mobile.ui.activity.WebGameActivity;
import com.example.echoenglish_mobile.ui.chatbot.ChatActivity;
import com.example.echoenglish_mobile.ui.quizz_app.MainQuizzAppActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        articles.add(new ListDomain("Caitlyn Jenner in \"Hell no\"", "pic1"));
        articles.add(new ListDomain("Gov. Brian kemp", "pic2"));
        articles.add(new ListDomain("US-China War", "pic3"));
        articles.add(new ListDomain("Caitlyn Jenner in \"Hell no\"", "pic1"));

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
