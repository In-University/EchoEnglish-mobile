package com.example.echoenglish_mobile.view.activity.video_youtube;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.response.TranscriptItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoYoutubeActivity extends AppCompatActivity implements TranscriptAdapter.OnTranscriptItemClickListener{
    private WebView youtubeWebView;
    private RecyclerView transcriptRecyclerView;
    private TranscriptAdapter transcriptAdapter;
    private List<TranscriptItem> transcriptItems;
    private ImageButton btnPause;
    private boolean isPlaying = true;

    private static String VIDEO_ID = "MY5SatbZMAo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_youtube);

        String intentVideoId = getIntent().getStringExtra("VideoId");
        if (intentVideoId != null && !intentVideoId.isEmpty()) {
            VIDEO_ID = intentVideoId;
        }

        youtubeWebView = findViewById(R.id.youtube_web_view);
        transcriptRecyclerView = findViewById(R.id.transcript_recycler_view);
        btnPause = findViewById(R.id.btn_pause);

        setupYoutubeWebView();

        // Set up RecyclerView
        transcriptItems = new ArrayList<>();
        transcriptAdapter = new TranscriptAdapter(this, transcriptItems, this);
        transcriptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transcriptRecyclerView.setAdapter(transcriptAdapter);

        // Set up play/pause toggle
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = !isPlaying;
                btnPause.setImageResource(isPlaying ?
                        android.R.drawable.ic_media_pause :
                        android.R.drawable.ic_media_play);

                // Execute JavaScript to control YouTube player
                String jsCommand = isPlaying ? "player.playVideo()" : "player.pauseVideo()";
                youtubeWebView.evaluateJavascript(jsCommand, null);
            }
        });

        // Parse mock API response
        parseMockApiResponse();
    }

    private void setupYoutubeWebView() {
        // Enable JavaScript
        WebSettings webSettings = youtubeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);

        youtubeWebView.setWebChromeClient(new WebChromeClient());
        youtubeWebView.setWebViewClient(new WebViewClient());

        // Load YouTube Embedded Player
        String embedUrl = "<html><style>body, html { margin: 0; padding: 0; }</style><body>" +
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
                "      'controls': 1," +
                "      'rel': 0" +
                "    }," +
                "    events: {" +
                "      'onReady': onPlayerReady" +
                "    }" +
                "  });" +
                "}" +
                "function onPlayerReady(event) {" +
                "  event.target.playVideo();" +
                "}" +
                "</script></body></html>";

        youtubeWebView.loadData(embedUrl, "text/html", "utf-8");
    }

    @Override
    public void onTranscriptItemClick(TranscriptItem item) {
        double timestamp = item.getStartTime();
        String jsSeekCommand = "player.seekTo(" + timestamp + ", true)";
        youtubeWebView.evaluateJavascript(jsSeekCommand, null);
    }

    private void parseMockApiResponse() {
        String mockApiResponse = "{\"content\": [{\"text\": \"Translator: Riaki Poništ\\nReviewer: Peter van de Ven\",\"start\": 0.0,\"dur\": 7.0}," +
                "{\"text\": \"Thank you so much.\",\"start\": 9.07,\"dur\": 1.74}," +
                "{\"text\": \"I am a journalist.\",\"start\": 12.39,\"dur\": 1.809}," +
                "{\"text\": \"My job is to talk to people\\nfrom all walks of life,\",\"start\": 14.539,\"dur\": 3.49}," +
                "{\"text\": \"all over the world.\",\"start\": 18.239,\"dur\": 1.68}," +
                "{\"text\": \"Today, I want to tell you\",\"start\": 19.999,\"dur\": 1.44}," +
                "{\"text\": \"why I decided to do this with my life\\nand what I've learned.\",\"start\": 21.439,\"dur\": 4.301}," +
                "{\"text\": \"My story begins in Caracas, Venezuela,\",\"start\": 26.58,\"dur\": 2.849}," +
                "{\"text\": \"in South America, where I grew up;\",\"start\": 29.479,\"dur\": 2.42}," +
                "{\"text\": \"a place that to me was,\\nand always will be,\",\"start\": 32.119,\"dur\": 2.881}," +
                "{\"text\": \"filled with magic and wonder.\",\"start\": 35.0,\"dur\": 1.95}," +
                "{\"text\": \"Frоm a very young age,\",\"start\": 37.74,\"dur\": 1.33}," +
                "{\"text\": \"my parents wanted me\\nto have a wider view of the world.\",\"start\": 39.07,\"dur\": 3.53}]}";

        try {
            JSONObject jsonObject = new JSONObject(mockApiResponse);
            JSONArray contentArray = jsonObject.getJSONArray("content");

            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String text = item.getString("text");
                double startTime = item.getDouble("start");
                double duration = item.getDouble("dur");

                TranscriptItem transcriptItem = new TranscriptItem(text, startTime, duration);
                transcriptItems.add(transcriptItem);
            }

            transcriptAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
