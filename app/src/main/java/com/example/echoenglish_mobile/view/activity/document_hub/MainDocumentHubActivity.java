package com.example.echoenglish_mobile.view.activity.document_hub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.PageResponse;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.browser.BrowserActivity;
import com.example.echoenglish_mobile.view.activity.document_hub.dto.NewsItem;
import com.example.echoenglish_mobile.view.activity.video_youtube.VideoYoutubeActivity;
import com.example.echoenglish_mobile.view.fragment.BrowserFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainDocumentHubActivity extends AppCompatActivity implements NewsAdapter.OnItemClickListener {

    private static final String TAG = "MainDocumentHubActivity";
    private static final int PAGE_SIZE = 5;

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private ApiService apiService;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_hub);

        setupViews();
        setupRecyclerView();
        setupApi();

        loadNewsData(currentPage);
    }

    private void setupViews() {
        LinearLayout browseWebButton = findViewById(R.id.browseWebButton);
        LinearLayout youtubeImportButton = findViewById(R.id.youtubeImportButton);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        browseWebButton.setOnClickListener(v -> navigateToBrowserActivity());
        youtubeImportButton.setOnClickListener(v -> showYoutubeImportPopup());
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(this, this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newsRecyclerView.setLayoutManager(layoutManager);
        newsRecyclerView.setAdapter(newsAdapter);

        // Add scroll listener for lazy loading
        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) { // Ensure enough items loaded initially
                        loadNewsData(currentPage + 1);
                    }
                }
            }
        });
    }

    private void setupApi() {
        apiService = ApiClient.getApiService();
    }

    private void loadNewsData(int page) {
        if (isLoading || isLastPage) {
            return; // Don't load if already loading or is the last page
        }

        Log.d(TAG, "Loading page: " + page);
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
                        currentPage = pageResponse.getNumber(); // Update current page from response
                        isLastPage = pageResponse.isLast(); // Check if this is the last page
                        Log.d(TAG, "Loaded page: " + currentPage + ", Is last: " + isLastPage);
                    } else {
                        // No more items or empty response
                        isLastPage = true;
                        Log.d(TAG, "No more items found.");
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(MainDocumentHubActivity.this, "Failed to load news: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<NewsItem>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e(TAG, "API Failure: ", t);
                Toast.makeText(MainDocumentHubActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void navigateToBrowserActivity() {
        Intent intent = new Intent(this, BrowserActivity.class);
        startActivity(intent);
    }

    private void showYoutubeImportPopup() {
        // (Code for youtube popup remains the same)
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
                Toast.makeText(this, "Importing: " + url, Toast.LENGTH_SHORT).show();
                String videoId = getIDFromYoutubeLink(url);
                if (!videoId.isEmpty()){
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
            return "";
        }
        String pattern = "(?<=v=|youtu\\.be/|/videos/|embed\\/|/v/|\\?v=)([\\w-]+)";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    @Override
    public void onItemClick(NewsItem newsItem) {
        Toast.makeText(this, "Clicked: " + newsItem.getTitle(), Toast.LENGTH_SHORT).show();

        if (newsItem.getUrl() != null && !newsItem.getUrl().isEmpty()) {
            Intent intent = new Intent(this, BrowserActivity.class);
            intent.putExtra(BrowserActivity.EXTRA_URL, newsItem.getUrl());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No URL available for this item.", Toast.LENGTH_SHORT).show();
        }
    }
}