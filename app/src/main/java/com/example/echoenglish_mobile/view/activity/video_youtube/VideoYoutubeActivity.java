package com.example.echoenglish_mobile.view.activity.video_youtube;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.response.TranscriptItem;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.video_youtube.dto.TranscriptContent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoYoutubeActivity extends AppCompatActivity implements TranscriptAdapter.OnTranscriptItemClickListener {
    private WebView youtubeWebView;
    private RecyclerView transcriptRecyclerView;
    private TranscriptAdapter transcriptAdapter;
    private List<TranscriptItem> transcriptItems;
    private ImageButton btnPause;
    private boolean isPlaying = true;

    private ApiService apiService;
    private String VIDEO_ID = "MY5SatbZMAo";
    private static final String TAG = "VideoYoutubeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_youtube);

        String intentVideoId = getIntent().getStringExtra("VideoId");
        if (intentVideoId != null && !intentVideoId.isEmpty()) {
            VIDEO_ID = intentVideoId;
        } else {
            Log.w(TAG, "VideoId not passed via Intent, using default: " + VIDEO_ID);
        }

        youtubeWebView = findViewById(R.id.youtube_web_view);
        transcriptRecyclerView = findViewById(R.id.transcript_recycler_view);
        btnPause = findViewById(R.id.btn_pause);

        apiService = ApiClient.getApiService();

        setupYoutubeWebView();

        transcriptItems = new ArrayList<>();
        transcriptAdapter = new TranscriptAdapter(this, transcriptItems, this);
        transcriptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transcriptRecyclerView.setAdapter(transcriptAdapter);

        btnPause.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            btnPause.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            String jsCommand = isPlaying ? "player.playVideo();" : "player.pauseVideo();";
            youtubeWebView.evaluateJavascript(jsCommand, null);
        });

        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (VIDEO_ID != null && !VIDEO_ID.isEmpty()) {
            fetchTranscriptFromApi(VIDEO_ID);
        } else {
            Toast.makeText(this, "Video ID is missing.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupYoutubeWebView() {
        WebSettings webSettings = youtubeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        youtubeWebView.setWebChromeClient(new WebChromeClient());
        youtubeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "YouTube player page finished loading.");
            }
        });

        String embedUrl = "<html><head><style>body,html{margin:0;padding:0;height:100%;overflow:hidden;}#player{height:100%;}</style></head><body>" +
                "<div id='player'></div>" +
                "<script>" +
                "var tag = document.createElement('script');" +
                "tag.src = 'https://www.youtube.com/iframe_api';" +
                "var firstScriptTag = document.getElementsByTagName('script')[0];" +
                "firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +
                "var player;" +
                "function onYouTubeIframeAPIReady() {" +
                "  player = new YT.Player('player', {" +
                "    height: '100%'," +
                "    width: '100%'," +
                "    videoId: '" + VIDEO_ID + "'," +
                "    playerVars: {" +
                "      'playsinline': 1," +
                "      'autoplay': 1," +
                "      'controls': 1," +
                "      'rel': 0," +
                "      'fs': 1" +
                "    }," +
                "    events: {" +
                "      'onReady': onPlayerReady," +
                "      'onStateChange': onPlayerStateChange" +
                "    }" +
                "  });" +
                "}" +
                "function onPlayerReady(event) {" +
                "   Android.onPlayerReady();" +
                "}" +
                "function onPlayerStateChange(event) {" +
                "   Android.onPlayerStateChange(event.data);" +
                "}" +
                "</script></body></html>";
        youtubeWebView.addJavascriptInterface(new YouTubePlayerInterface(this), "Android");
        youtubeWebView.loadDataWithBaseURL("https://www.youtube.com", embedUrl, "text/html", "utf-8", null);
    }

    public class YouTubePlayerInterface {
        Context mContext;
        YouTubePlayerInterface(Context c) { mContext = c; }

        @android.webkit.JavascriptInterface
        public void onPlayerReady() {
            Log.d(TAG, "Player is ready (called from JS)");
            runOnUiThread(() -> {
                isPlaying = true;
                btnPause.setImageResource(android.R.drawable.ic_media_pause);
            });
        }

        @android.webkit.JavascriptInterface
        public void onPlayerStateChange(int playerState) {
            Log.d(TAG, "Player state changed (called from JS): " + playerState);
            runOnUiThread(() -> {
                if (playerState == 1) { // PLAYING
                    isPlaying = true;
                    btnPause.setImageResource(android.R.drawable.ic_media_pause);
                } else if (playerState == 2 || playerState == 0 || playerState == 5) { // PAUSED, ENDED, CUED
                    isPlaying = false;
                    btnPause.setImageResource(android.R.drawable.ic_media_play);
                }
            });
        }
    }

    @Override
    public void onTranscriptItemClick(TranscriptItem item) {
        if (youtubeWebView != null && item != null) {
            double timestamp = item.getStartTime();
            String jsSeekCommand = "if(player && typeof player.seekTo === 'function'){ player.seekTo(" + timestamp + ", true); }";
            youtubeWebView.evaluateJavascript(jsSeekCommand, null);
            Log.d(TAG, "Seeking to: " + timestamp);
            if (!isPlaying) {
                isPlaying = true;
                btnPause.setImageResource(android.R.drawable.ic_media_pause);
                youtubeWebView.evaluateJavascript("if(player && typeof player.playVideo === 'function'){ player.playVideo(); }", null);
            }
        }
    }

    private void fetchTranscriptFromApi(String videoId) {
        Log.d(TAG, "Fetching transcript for video ID: " + videoId);
        Toast.makeText(this, "Loading transcript...", Toast.LENGTH_SHORT).show();

        apiService.getYoutubeTranscript(videoId).enqueue(new Callback<TranscriptContent>() {
            @Override
            public void onResponse(Call<TranscriptContent> call, Response<TranscriptContent> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TranscriptContent transcriptContent = response.body();
                    List<TranscriptItem> items = transcriptContent.getContent();
                    if (items != null && !items.isEmpty()) {
                        Log.d(TAG, "Transcript fetched successfully: " + items.size() + " items.");
                        transcriptItems.clear();
                        transcriptItems.addAll(items);
                        transcriptAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Transcript content is null or empty.");
                        Toast.makeText(VideoYoutubeActivity.this, "No transcript available.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    try {
                        Log.e(TAG, "Error Body: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) { Log.e(TAG, "Error reading error body", e); }
                    Toast.makeText(VideoYoutubeActivity.this, "Failed to load transcript: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TranscriptContent> call, Throwable t) {
                Log.e(TAG, "API Failure: ", t);
                Toast.makeText(VideoYoutubeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (youtubeWebView != null) {
            youtubeWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (youtubeWebView != null) {
            youtubeWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (youtubeWebView != null) {
            youtubeWebView.removeJavascriptInterface("Android");
            youtubeWebView.stopLoading();
            ViewGroup parent = (ViewGroup) youtubeWebView.getParent();
            if (parent != null) {
                parent.removeView(youtubeWebView);
            }
            youtubeWebView.destroy();
            youtubeWebView = null;
        }
        super.onDestroy();
    }
}