package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView; // Use ImageView instead of ImageButton for the back button
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
import com.example.echoenglish_mobile.view.activity.dashboard.DashboardActivity;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.PronunciationAssessmentActivity;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment; // Assuming you have this class

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

    // CHANGE: Declare as ImageView, as the crash indicates it's being found as such
    private ImageView btnBackHeader; // Reference for the back button

    private NestedScrollView contentScrollView; // Reference for the scroll view
    // ProgressBar loadingProgressBar; // Not needed if using DialogFragment for loading


    private static final String TAG = "WordDetailActivity";
    private static final String LOADING_DIALOG_TAG = "LoadingDialog"; // Tag for the loading dialog

    // Method to show/hide loading using DialogFragment
    private int loadingApiCount = 0; // Đếm số lượng API đang chạy

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) { // Only show dialog on the first active call
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0; // Just in case it goes negative somehow
            showLoading(false);
        }
    }

    // Adapted showLoading method for this activity's views
    private void showLoading(boolean isLoading) {
        // Ensure views are initialized before accessing them
        if (contentScrollView == null || btnBackHeader == null || btnWordPronunAnalyze == null || btnPlayUkAudio == null || btnPlayUsAudio == null) {
            // Views not yet initialized, do nothing or log a warning
            Log.w(TAG, "showLoading called before views are fully initialized.");
            return;
        }


        if (isLoading) {
            // Show the loading dialog
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading word details..."); // Pass optional message
            // Hide content
            contentScrollView.setVisibility(View.INVISIBLE);
            // Disable interactive elements
            btnBackHeader.setEnabled(false);
            btnWordPronunAnalyze.setEnabled(false);
            btnPlayUkAudio.setEnabled(false);
            btnPlayUsAudio.setEnabled(false);

        } else {
            // Hide the loading dialog
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            // Show content
            contentScrollView.setVisibility(View.VISIBLE);
            // Re-enable interactive elements
            btnBackHeader.setEnabled(true);
            btnWordPronunAnalyze.setEnabled(true); // Re-enable unconditionally
            btnPlayUkAudio.setEnabled(true); // Re-enable, audio availability handled by listener
            btnPlayUsAudio.setEnabled(true); // Re-enable, audio availability handled by listener
        }
    }


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

        // Initialize Header and Scroll Views
        // CHANGE: Assign to ImageView variable
        btnBackHeader = findViewById(R.id.btn_back_header);
        contentScrollView = findViewById(R.id.contentScrollView);
        // loadingProgressBar = findViewById(R.id.loadingProgressBar); // Not needed

        // --- Back Button Listener ---
        // setOnClickListener works on ImageView
        btnBackHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DictionaryWordDetailActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack cũ
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
        Log.d(TAG, "Searching for synonym: " + keyword);
        // Removed Toast here to rely on the loading dialog
        // Toast.makeText(this, "Searching for: " + keyword, Toast.LENGTH_SHORT).show(); // Thông báo đang tìm

        ApiService apiService = ApiClient.getApiService(); // Lấy instance ApiService
        Call<Word> call = apiService.getWordDetails(keyword); // Gọi API

        // Context để sử dụng trong Callback
        Context context = this;

        // --- Start Loading ---
        startApiCall();

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                // --- Finish Loading ---
                finishApiCall();

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
                // --- Finish Loading ---
                finishApiCall();

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
                player.stop(); // Stop previous playback
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