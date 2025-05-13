package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.MeaningAdapter;
import com.example.echoenglish_mobile.model.Meaning;
import com.example.echoenglish_mobile.model.Synonym;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.home.HomeActivity;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.PronunciationAssessmentActivity;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.flexbox.FlexboxLayout;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DictionaryWordDetailActivity extends AppCompatActivity {
    private TextView tvWord, tvUkPronunciation, tvUsPronunciation;
    private ImageView ivImage;
    private RecyclerView meaningsRecyclerView;
    private MeaningAdapter meaningAdapter;
    private FlexboxLayout synonymsContainer;
    private ExoPlayer player;
    private ImageView btnPlayUkAudio, btnPlayUsAudio;
    private Button btnWordPronunAnalyze;

    private ImageView btnBackHeader;

    private NestedScrollView contentScrollView;


    private static final String TAG = "WordDetailActivity";
    private static final String LOADING_DIALOG_TAG = "LoadingDialog";

    private int loadingApiCount = 0;

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (contentScrollView == null || btnBackHeader == null || btnWordPronunAnalyze == null || btnPlayUkAudio == null || btnPlayUsAudio == null) {
            Log.w(TAG, "showLoading called before views are fully initialized.");
            return;
        }


        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading word details...");
            contentScrollView.setVisibility(View.INVISIBLE);
            btnBackHeader.setEnabled(false);
            btnWordPronunAnalyze.setEnabled(false);
            btnPlayUkAudio.setEnabled(false);
            btnPlayUsAudio.setEnabled(false);

        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            contentScrollView.setVisibility(View.VISIBLE);
            btnBackHeader.setEnabled(true);
            btnWordPronunAnalyze.setEnabled(true);
            btnPlayUkAudio.setEnabled(true);
            btnPlayUsAudio.setEnabled(true);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_word_detail);

        tvWord = findViewById(R.id.tvWord);
        ivImage = findViewById(R.id.ivImage);
        tvUkPronunciation = findViewById(R.id.tvUkPronunciation);
        tvUsPronunciation = findViewById(R.id.tvUsPronunciation);
        meaningsRecyclerView = findViewById(R.id.meaningsRecyclerView);
        synonymsContainer = findViewById(R.id.synonymsContainer);
        btnPlayUkAudio = findViewById(R.id.btnPlayUkAudio);
        btnPlayUsAudio = findViewById(R.id.btnPlayUsAudio);
        btnWordPronunAnalyze = findViewById(R.id.btnWordPronunAnalyze);

        btnBackHeader = findViewById(R.id.btn_back_header);
        contentScrollView = findViewById(R.id.contentScrollView);

        btnBackHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryWordDetailActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        Word word = (Word) getIntent().getSerializableExtra("word_data");

        if (word != null) {
            displayWordDetails(word);
        } else {
            String keywordToSearch = getIntent().getStringExtra("keyword_to_search");
            if (keywordToSearch != null && !keywordToSearch.isEmpty()) {
                searchAndDisplayWord(keywordToSearch);
            } else {
                Log.e(TAG, "Word data and keyword_to_search are both null.");
                Toast.makeText(this, "Error: Could not load word data.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void displayWordDetails(Word word) {
        btnWordPronunAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryWordDetailActivity.this, PronunciationAssessmentActivity.class);
                intent.putExtra("TARGET_WORD_OBJECT", word);
                startActivity(intent);
            }
        });
        tvWord.setText(word.getWord());
        tvUkPronunciation.setText("/"+word.getUkPronunciation()+"/");
        tvUsPronunciation.setText("/"+word.getUsPronunciation()+"/");

        Glide.with(this)
                .load(word.getImageUrl())
                .placeholder(R.drawable.ic_xml_launcher_background)
                .error(R.drawable.ic_xml_launcher_background)
                .into(ivImage);

        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
        }
        btnPlayUkAudio.setOnClickListener(v -> playAudioFromUrl(player, word.getUkAudio()));
        btnPlayUsAudio.setOnClickListener(v -> playAudioFromUrl(player, word.getUsAudio()));

        setupMeaningsRecyclerView(word.getMeanings());

        displaySynonyms(word.getSynonyms());
    }

    private void searchAndDisplayWord(String keyword) {
        Log.d(TAG, "Searching for synonym: " + keyword);

        ApiService apiService = ApiClient.getApiService();
        Call<Word> call = apiService.getWordDetails(keyword);

        Context context = this;

        startApiCall();

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    Word foundWord = response.body();
                    Log.d(TAG, "Synonym found: " + foundWord.getWord());

                    Intent intent = new Intent(context, DictionaryWordDetailActivity.class);
                    intent.putExtra("word_data", foundWord);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent);


                } else {
                    Log.w(TAG, "Synonym not found or error: " + response.code());
                    Toast.makeText(context, "Không tìm thấy từ '" + keyword + "'", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                finishApiCall();

                Log.e(TAG, "API call failed for synonym: " + keyword, t);
                Toast.makeText(context, "Lỗi kết nối khi tìm '" + keyword + "'", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupMeaningsRecyclerView(List<Meaning> meanings) {
        if (meanings == null || meanings.isEmpty()) {
            Log.w(TAG, "No meanings found for this word.");
            findViewById(R.id.labelMeanings).setVisibility(View.GONE);
            meaningsRecyclerView.setVisibility(View.GONE);
            return;
        }

        meaningsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (meaningAdapter == null) {
            meaningAdapter = new MeaningAdapter(meanings);
            meaningsRecyclerView.setAdapter(meaningAdapter);
        } else {
            meaningAdapter.updateData(meanings);
        }
        findViewById(R.id.labelMeanings).setVisibility(View.VISIBLE);
        meaningsRecyclerView.setVisibility(View.VISIBLE);
    }


    private void displaySynonyms(List<Synonym> synonyms) {
        synonymsContainer.removeAllViews();
        if (synonyms == null || synonyms.isEmpty()) {
            Log.w(TAG, "No synonyms found for this word.");
            findViewById(R.id.labelSynonyms).setVisibility(View.GONE);
            synonymsContainer.setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.labelSynonyms).setVisibility(View.VISIBLE);
        synonymsContainer.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Synonym synonym : synonyms) {
            TextView synonymView = (TextView) inflater.inflate(R.layout.item_dictionary_synonym, synonymsContainer, false);
            final String synonymText = synonym.getSynonym();
            synonymView.setText(synonymText);

            synonymView.setOnClickListener(v -> {
                searchAndDisplayWord(synonymText);
            });

            synonymsContainer.addView(synonymView);
        }
    }


    private void playAudioFromUrl(ExoPlayer player, String url) {
        if (player == null) {
            Log.e(TAG, "ExoPlayer is not initialized.");
            Toast.makeText(this, "Audio player error.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
            try {
                player.stop();
                player.setMediaItem(MediaItem.fromUri(url));
                player.prepare();
                player.play();
                Log.d(TAG, "Playing audio from URL: " + url);
            } catch (Exception e) {
                Log.e(TAG, "Error playing audio from URL: " + url, e);
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Invalid or empty audio URL: " + url);
            Toast.makeText(this, "Audio not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
            Log.d(TAG, "ExoPlayer released.");
        }
    }
}