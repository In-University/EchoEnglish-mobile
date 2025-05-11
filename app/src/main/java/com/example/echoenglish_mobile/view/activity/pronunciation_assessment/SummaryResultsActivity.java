package com.example.echoenglish_mobile.view.activity.pronunciation_assessment;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.fragment.FluencyResultFragment;
import com.example.echoenglish_mobile.view.fragment.GrammarResultFragment;
import com.example.echoenglish_mobile.view.fragment.IntonationResultFragment;
import com.example.echoenglish_mobile.view.fragment.PronunciationFragment;
import com.example.echoenglish_mobile.view.fragment.VocabularyResultFragment;
import com.google.android.material.chip.Chip;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class SummaryResultsActivity extends AppCompatActivity {
    private ApiService apiService;
    private String targetWord;
    private String outputFile;
    private SentenceAnalysisResult sentenceAnalysisResult;
    public static final String ANALYSIS_RESULT = "ANALYSIS_RESULT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(ANALYSIS_RESULT)) {
            sentenceAnalysisResult = (SentenceAnalysisResult) getIntent().getSerializableExtra(ANALYSIS_RESULT);
        } else {
            Toast.makeText(this, "Analysis result not provided.", Toast.LENGTH_LONG).show();
            return;
        }
        setContentView(R.layout.activity_scores_results);
        replaceFragment(PronunciationFragment.newInstance(sentenceAnalysisResult));

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
        chipFluency.setOnClickListener(v -> replaceFragment(new FluencyResultFragment()));
        targetWord = getIntent().getStringExtra("targetWord");
        if(targetWord == null || targetWord.isEmpty()){
            targetWord = "test audio";
        }
        outputFile = getIntent().getStringExtra("outputFile");
        if(outputFile == null || outputFile.isEmpty()){
            outputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
        }

        chipFluency.setOnClickListener(v -> {
            if (sentenceAnalysisResult != null) {
                replaceFragment(FluencyResultFragment.newInstance(sentenceAnalysisResult));
            } else {
                Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
            }
        });

        chipPronunciation.setOnClickListener(v -> {
            if (sentenceAnalysisResult != null) {
                replaceFragment(PronunciationFragment.newInstance(sentenceAnalysisResult));
            } else {
                Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
            }
        });

        chipVocabulary.setOnClickListener(v -> {
            if (sentenceAnalysisResult != null) {
                replaceFragment(VocabularyResultFragment.newInstance(sentenceAnalysisResult));
            } else {
                Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
            }
        });

        chipIntonation.setOnClickListener(v -> {
            if (sentenceAnalysisResult != null) {
                replaceFragment(IntonationResultFragment.newInstance(sentenceAnalysisResult));
            } else {
                Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
            }
        });

        chipGrammar.setOnClickListener(v -> {
            if (sentenceAnalysisResult != null) {
                replaceFragment(GrammarResultFragment.newInstance(sentenceAnalysisResult.getFeedback()));
            } else {
                Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}
