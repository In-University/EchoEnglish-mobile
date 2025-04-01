package com.example.echoenglish_mobile.ui.quizz_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.echoenglish_mobile.databinding.ActivityMainBinding;
import com.example.echoenglish_mobile.databinding.ActivityMainQuizzAppBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class MainQuizzAppActivity extends AppCompatActivity {

    private ActivityMainQuizzAppBinding binding;
    private List<QuizModel> quizModelList;
    private QuizListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainQuizzAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        quizModelList = new ArrayList<>();
        getDataFromFirebase();
    }

    private void setupRecyclerView() {
        binding.progressBar.setVisibility(View.GONE);
        adapter = new QuizListAdapter(quizModelList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void getDataFromFirebase() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance("https://quizz-app-c826f-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference()
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        QuizModel quizModel = snapshot.getValue(QuizModel.class);
                        if (quizModel != null) {
                            quizModelList.add(quizModel);
                        }
                    }
                    Log.d("FirebaseData", "Data fetched: " + quizModelList);
                    setupRecyclerView();
                });
    }
}
