package com.example.echoenglish_mobile.view.activity.document_hub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.PageResponse;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.browser.BrowserActivity;
import com.example.echoenglish_mobile.view.activity.document_hub.dto.NewsItem;
import com.example.echoenglish_mobile.view.activity.document_hub.dto.VideoItem;
import com.example.echoenglish_mobile.view.activity.video_youtube.VideoYoutubeActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainDocumentHubActivity extends AppCompatActivity
        implements NewsAdapter.OnItemClickListener, VideoAdapter.OnVideoItemClickListener {

    private static final String TAG = "MainDocumentHubActivity";
    private static final int PAGE_SIZE = 5;

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private LinearLayoutManager newsLayoutManager;
    private ProgressBar progressBar;
    private ApiService apiService;
    ImageView btnBack;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;

    private RecyclerView videosRecyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoItem> videoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_hub);

        setupViews();
        setupNewsRecyclerView();
        setupVideosRecyclerView();
        setupApi();

        loadNewsData(currentPage);
        loadVideoData();
    }

    private void setupViews() {
        LinearLayout browseWebButton = findViewById(R.id.browseWebButton);
        LinearLayout youtubeImportButton = findViewById(R.id.youtubeImportButton);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        browseWebButton.setOnClickListener(v -> navigateToBrowserActivity());
        youtubeImportButton.setOnClickListener(v -> showYoutubeImportPopup());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupNewsRecyclerView() {
        newsAdapter = new NewsAdapter(this, this);
        newsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newsRecyclerView.setLayoutManager(newsLayoutManager);
        newsRecyclerView.setAdapter(newsAdapter);

        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = newsLayoutManager.getChildCount();
                int totalItemCount = newsLayoutManager.getItemCount();
                int firstVisibleItemPosition = newsLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadNewsData(currentPage + 1);
                    }
                }
            }
        });
    }

    private void setupVideosRecyclerView() {
        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList, this);
        LinearLayoutManager videoLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(videoLayoutManager);
        videosRecyclerView.setAdapter(videoAdapter);
        videosRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupApi() {
        apiService = ApiClient.getApiService();
    }

    private void loadNewsData(int page) {
        if (isLoading || isLastPage) {
            return;
        }

        Log.d(TAG, "Loading news page: " + page);
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        apiService.getNews(page, PAGE_SIZE).enqueue(new Callback<PageResponse<NewsItem>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<NewsItem>> call, @NonNull Response<PageResponse<NewsItem>> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<NewsItem> pageResponse = response.body();
                    if (pageResponse.getContent() != null && !pageResponse.getContent().isEmpty()) {
                        newsAdapter.addNewsItems(pageResponse.getContent());
                        currentPage = pageResponse.getNumber();
                        isLastPage = pageResponse.isLast();
                        Log.d(TAG, "Loaded news page: " + currentPage + ", Is last: " + isLastPage);
                    } else {
                        isLastPage = true;
                        Log.d(TAG, "No more news items found.");
                    }
                } else {
                    Log.e(TAG, "News API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(MainDocumentHubActivity.this, "Failed to load news: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<NewsItem>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e(TAG, "News API Failure: ", t);
                Toast.makeText(MainDocumentHubActivity.this, "Network Error loading news: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVideoData() {
        videoList.clear();
        videoList.add(new VideoItem("The power of vulnerability | Brené Brown", "iCvmsMzlF7o", "ADVANCED", "20:12", "TED Talk 1", "C1 Level"));
        videoList.add(new VideoItem("How to speak so that people want to listen | Julian Treasure", "eIho2S0ZahI", "INTERMEDIATE", "09:58", "TED Talk 2", "B2 Level"));
        videoList.add(new VideoItem("How great leaders inspire action | Simon Sinek", "qp0HIF3SfI4", "INTERMEDIATE", "18:04", "TED Talk 3", "B2 Level"));
        videoList.add(new VideoItem("Grit: the power of passion and perseverance | Angela Lee Duckworth", "H14bBuluwB8", "UPPER-INT", "06:13", "Lesson 10", "B2/C1 Level"));
        videoList.add(new VideoItem("The puzzle of motivation | Dan Pink", "rrkrvAUbU9Y", "ADVANCED", "18:36", "Business English", "C1 Level"));
        videoList.add(new VideoItem("Learn English with Friends | Rachel's Monologue", "q7SAt9h4sd0", "BEGINNER", "05:30", "Episode 1", "A2 Level"));


        videoAdapter.notifyDataSetChanged();
        if (videoList.isEmpty()) {
            findViewById(R.id.videoSectionTitle).setVisibility(View.GONE);
        } else {
            findViewById(R.id.videoSectionTitle).setVisibility(View.VISIBLE);
        }
    }


    private void navigateToBrowserActivity() {
        Intent intent = new Intent(this, BrowserActivity.class);
        startActivity(intent);
    }

    private void showYoutubeImportPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import YouTube Link");
        builder.setMessage("Enter the YouTube video URL:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(48, 16, 48, 16);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Import", (dialog, which) -> {
            String url = input.getText().toString().trim();
            if (!url.isEmpty()) {
                String videoId = getIDFromYoutubeLink(url);
                if (videoId != null && !videoId.isEmpty()){
                    Intent intent = new Intent(this, VideoYoutubeActivity.class);
                    intent.putExtra("VideoId", videoId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private String getIDFromYoutubeLink(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)([\\w-]{11})";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public void onItemClick(NewsItem newsItem) {
        Toast.makeText(this, "Clicked News: " + newsItem.getTitle(), Toast.LENGTH_SHORT).show();

        if (newsItem.getUrl() != null && !newsItem.getUrl().isEmpty()) {
            Intent intent = new Intent(this, BrowserActivity.class);
            intent.putExtra(BrowserActivity.EXTRA_URL, newsItem.getUrl());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No URL available for this news item.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVideoClick(VideoItem videoItem) {
        Toast.makeText(this, "Clicked Video: " + videoItem.getTitle(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, VideoYoutubeActivity.class);
        intent.putExtra("VideoId", videoItem.getVideoId());
        startActivity(intent);
    }
}