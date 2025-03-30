package com.example.echoenglish_mobile.ui.pronun_summary_result;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.echoenglish_mobile.R;
import com.google.android.material.chip.Chip;
public class SummaryResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores_results);

        Chip chipPronunciation = findViewById(R.id.tabPronunciation);
        chipPronunciation.setText("Pronunciation");

        Chip chipFluency = findViewById(R.id.tabFluency);
        chipFluency.setText("Fluency");
        Chip chipIntonation = findViewById(R.id.tabIntonation);
        chipIntonation.setText("Intonation");
        Chip chipGrammar = findViewById(R.id.tabGrammar);
        chipGrammar.setText("Grammar");
        Chip chipVocabulary = findViewById(R.id.tabVocabulary);
        chipVocabulary.setText("Vocabulary");
        chipFluency.setOnClickListener(v -> replaceFragment(new FluencyFragment()));

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}
