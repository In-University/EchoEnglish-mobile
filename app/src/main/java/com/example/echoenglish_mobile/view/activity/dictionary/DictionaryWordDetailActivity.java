package com.example.echoenglish_mobile.view.activity.dictionary;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Meaning;
import com.example.echoenglish_mobile.model.Synonym;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.PronunciationAssessmentActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.flexbox.FlexboxLayout;
import com.example.echoenglish_mobile.adapter.MeaningAdapter;

import java.util.List;
import android.content.Context;
import android.content.Intent;
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

    private static final String TAG = "WordDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_word_detail);

        // Initialize Views
        tvWord = findViewById(R.id.tvWord);
        ivImage = findViewById(R.id.ivImage);
        tvUkPronunciation = findViewById(R.id.tvUkPronunciation);
        tvUsPronunciation = findViewById(R.id.tvUsPronunciation);
        meaningsRecyclerView = findViewById(R.id.meaningsRecyclerView);
        synonymsContainer = findViewById(R.id.synonymsContainer);
        btnPlayUkAudio = findViewById(R.id.btnPlayUkAudio);
        btnPlayUsAudio = findViewById(R.id.btnPlayUsAudio);
        btnWordPronunAnalyze = findViewById(R.id.btnWordPronunAnalyze);
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

        // Load Image using Glide
        Glide.with(this)
                .load(word.getImageUrl())
                .placeholder(R.drawable.ic_xml_launcher_background) // Replace with your placeholder
                .error(R.drawable.ic_xml_launcher_background) // Replace with your error image
                .into(ivImage);

        // Initialize or Re-initialize ExoPlayer
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
        }
        btnPlayUkAudio.setOnClickListener(v -> playAudioFromUrl(player, word.getUkAudio()));
        btnPlayUsAudio.setOnClickListener(v -> playAudioFromUrl(player, word.getUsAudio()));

        // Setup RecyclerView for Meanings
        setupMeaningsRecyclerView(word.getMeanings());

        // Display Synonyms (using FlexboxLayout)
        displaySynonyms(word.getSynonyms());
    }

    // Hàm tìm kiếm và hiển thị từ (Tương tự như trong DictionaryActivity)
    private void searchAndDisplayWord(String keyword) {
        // Có thể hiển thị một ProgressBar ở đây
        Log.d(TAG, "Searching for synonym: " + keyword);
        Toast.makeText(this, "Searching for: " + keyword, Toast.LENGTH_SHORT).show(); // Thông báo đang tìm

        ApiService apiService = ApiClient.getApiService(); // Lấy instance ApiService
        Call<Word> call = apiService.getWordDetails(keyword); // Gọi API

        // Context để sử dụng trong Callback
        Context context = this;

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                // Ẩn ProgressBar ở đây
                if (response.isSuccessful() && response.body() != null) {
                    Word foundWord = response.body();
                    Log.d(TAG, "Synonym found: " + foundWord.getWord());

                    // **Quan trọng:** Tạo Intent MỚI để mở lại chính Activity này
                    Intent intent = new Intent(context, DictionaryWordDetailActivity.class);
                    intent.putExtra("word_data", foundWord); // Truyền dữ liệu từ TÌM ĐƯỢC

                    // Cờ để quản lý back stack (tùy chọn)
                    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // FLAG_ACTIVITY_NEW_TASK thường đủ dùng để mở instance mới
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent);

                    // Nếu bạn *không* muốn activity hiện tại (của từ gốc) đóng lại
                    // thì không cần gọi finish(). Nếu muốn đóng thì gọi finish().
                    // finish(); // Đóng activity hiện tại sau khi mở activity mới (tùy chọn)

                } else {
                    Log.w(TAG, "Synonym not found or error: " + response.code());
                    Toast.makeText(context, "Không tìm thấy từ '" + keyword + "'", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                // Ẩn ProgressBar ở đây
                Log.e(TAG, "API call failed for synonym: " + keyword, t);
                Toast.makeText(context, "Lỗi kết nối khi tìm '" + keyword + "'", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupMeaningsRecyclerView(List<Meaning> meanings) {
        // ... (Giữ nguyên code của bạn)
        if (meanings == null || meanings.isEmpty()) {
            Log.w(TAG, "No meanings found for this word.");
            findViewById(R.id.labelMeanings).setVisibility(View.GONE);
            meaningsRecyclerView.setVisibility(View.GONE);
            return;
        }

        meaningsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Kiểm tra adapter đã tồn tại chưa để tránh tạo mới không cần thiết
        if (meaningAdapter == null) {
            meaningAdapter = new MeaningAdapter(meanings);
            meaningsRecyclerView.setAdapter(meaningAdapter);
        } else {
            meaningAdapter.updateData(meanings); // Thêm hàm update nếu cần refresh data
        }
        findViewById(R.id.labelMeanings).setVisibility(View.VISIBLE);
        meaningsRecyclerView.setVisibility(View.VISIBLE);
    }


    private void displaySynonyms(List<Synonym> synonyms) {
        synonymsContainer.removeAllViews(); // Clear previous views
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
            // Inflate the chip layout
            TextView synonymView = (TextView) inflater.inflate(R.layout.item_dictionary_synonym, synonymsContainer, false);
            final String synonymText = synonym.getSynonym(); // Lấy text của synonym
            synonymView.setText(synonymText);

            // *** GÁN OnClickListener ***
            synonymView.setOnClickListener(v -> {
                // Gọi hàm tìm kiếm khi synonym được click
                searchAndDisplayWord(synonymText);
            });

            synonymsContainer.addView(synonymView);
        }
    }


    private void playAudioFromUrl(ExoPlayer player, String url) {
        // ... (Giữ nguyên code của bạn)
        if (player == null) {
            Log.e(TAG, "ExoPlayer is not initialized.");
            Toast.makeText(this, "Audio player error.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Basic URL check
        if (url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
            try {
                MediaItem mediaItem = MediaItem.fromUri(url);
                player.stop(); // Stop previous playback
                player.setMediaItem(mediaItem);
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
