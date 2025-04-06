package com.example.echoenglish_mobile.ui.pronun_summary_result;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.data.remote.ApiClient;
import com.example.echoenglish_mobile.data.remote.ApiService;
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
        chipFluency.setOnClickListener(v -> replaceFragment(new FluencyResultFragment()));
        targetWord = getIntent().getStringExtra("targetWord");
        if(targetWord == null || targetWord.isEmpty()){
            targetWord = "test audio";
        }
        outputFile = getIntent().getStringExtra("outputFile");
        if(outputFile == null || outputFile.isEmpty()){
            outputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
        }

        apiService = ApiClient.getApiService();

        getSentenceAnalysisResult();

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
    }

    private void getSentenceAnalysisResult() {
        File audioFile = new File(outputFile);
        RequestBody audioRequestBody = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), audioRequestBody);
        RequestBody targetWordBody = RequestBody.create(MediaType.parse("text/plain"), targetWord);

        Call<SentenceAnalysisResult> call = apiService.analyzeSentences(audioPart, targetWordBody);
        call.enqueue(new retrofit2.Callback<SentenceAnalysisResult>() {
            @Override
            public void onResponse(Call<SentenceAnalysisResult> call, retrofit2.Response<SentenceAnalysisResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sentenceAnalysisResult = response.body();
                    Toast.makeText(SummaryResultsActivity.this, "Sentences Analysis complete", Toast.LENGTH_SHORT).show();
//                    replaceFragment(FluencyFragment.newInstance(sentenceAnalysisResult));
                } else {
                    Toast.makeText(SummaryResultsActivity.this, "Sentences Analysis failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SentenceAnalysisResult> call, Throwable t) {
                Toast.makeText(SummaryResultsActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
