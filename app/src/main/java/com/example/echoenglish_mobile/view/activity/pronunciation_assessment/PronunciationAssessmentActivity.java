package com.example.echoenglish_mobile.view.activity.pronunciation_assessment;

import android.Manifest;
// Bỏ import android.media.MediaPlayer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri; // Thêm import Uri
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Thêm import Nullable
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Thêm các import của ExoPlayer
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.dialog.AddWordToFlashcardDialog;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;


import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Meaning;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.AudioHandler;
import com.example.echoenglish_mobile.view.activity.webview.WebViewActivity;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;
import com.example.echoenglish_mobile.view.fragment.PhoneticFeedbackFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PronunciationAssessmentActivity extends AppCompatActivity implements AddWordToFlashcardDialog.OnFlashcardSelectedListener {

    // Constants
    public static final String EXTRA_WORD = "TARGET_WORD_OBJECT";
    private static final int RECORD_AUDIO_PERMISSION_CODE = 200;
    private static final String LOADING_DIALOG_TAG = "PronunciationLoading";
    private static final String LOG_TAG = "PronunciationActivity";

    // UI Elements
    private FloatingActionButton fabMicrophone;
    private FloatingActionButton fabPlayRecording;
    private TextView tvWordTitle;
    private TextView tvPhonetic;
    private TextView tvDefinition;
    private Button btnUsAudio;
    private Button btnUkAudio;
    private Button btnExamples;
    private ImageButton btnBookmark;
    private ImageButton btnBack;

    // State & Data
    private AudioHandler audioHandler;
    private ExoPlayer exoPlayer;
    private boolean isRecording = false;
    private String localOutputFile;
    private Word currentWord;
    private ApiService apiService;

    private Player.Listener playerListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pronunciation);

        apiService = ApiClient.getApiService();
        localOutputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
        audioHandler = new AudioHandler(this, localOutputFile);

        // Khởi tạo ExoPlayer listener
        initializePlayerListener();

        if (getIntent().hasExtra(EXTRA_WORD)) {
            currentWord = (Word) getIntent().getSerializableExtra(EXTRA_WORD);
        }

        // Chỉ check null cơ bản cho currentWord
        if (currentWord == null) {
            Log.e(LOG_TAG, "Word object not received. Finishing Activity.");
            Toast.makeText(this, "Error: Word data missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViews();
        updateUIWithWordData();
        setupListeners();
    }

    // Khởi tạo ExoPlayer khi Activity bắt đầu nhìn thấy
    @Override
    protected void onStart() {
        super.onStart();
        initializeExoPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseExoPlayer();
        audioHandler.release();
        if (isRecording) {
            audioHandler.stopRecording();
            updateUIRecordingStopped();
            isRecording = false;
        }
    }

    private void initializeExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            // Gắn listener vào player
            exoPlayer.addListener(playerListener);
        }
    }

    // Khởi tạo listener cho ExoPlayer
    private void initializePlayerListener() {
        playerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    setAudioButtonsEnabled(true);
                } else if (playbackState == Player.STATE_READY) {
                    setAudioButtonsEnabled(false);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Toast.makeText(PronunciationAssessmentActivity.this, "Buffering audio...", Toast.LENGTH_SHORT).show();
                    setAudioButtonsEnabled(false);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                // Xử lý lỗi khi phát
                Log.e(LOG_TAG, "ExoPlayer Error: " + error.getMessage(), error);
                Toast.makeText(PronunciationAssessmentActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                setAudioButtonsEnabled(true); // Re-enable button khi có lỗi
            }
        };
    }

    // Giải phóng ExoPlayer
    private void releaseExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener); // Tháo listener
            exoPlayer.release(); // Giải phóng tài nguyên
            exoPlayer = null;
        }
    }


    private void findViews() {
        fabMicrophone = findViewById(R.id.fabMicrophone);
        fabPlayRecording = findViewById(R.id.fabPlayRecording);
        tvWordTitle = findViewById(R.id.wordTitle);
        tvPhonetic = findViewById(R.id.phonetic);
        tvDefinition = findViewById(R.id.definitionText);
        btnUsAudio = findViewById(R.id.usAudioButton);
        btnUkAudio = findViewById(R.id.ukAudioButton);
        btnExamples = findViewById(R.id.examplesButton);
        btnBookmark = findViewById(R.id.bookmarkButton);
        btnBack = findViewById(R.id.backButton);
    }

    private void updateUIWithWordData() {
        tvWordTitle.setText(currentWord.getWord());
        String phoneticToDisplay = !TextUtils.isEmpty(currentWord.getUsPronunciation())
                ? "/" + currentWord.getUsPronunciation() + "/" : "/" + currentWord.getUkPronunciation() + "/";
        tvPhonetic.setText(phoneticToDisplay != null ? phoneticToDisplay : "//");

        String definitionText = "No definition available.";
        if (currentWord.getMeanings() != null && !currentWord.getMeanings().isEmpty()) {
            Meaning firstMeaning = currentWord.getMeanings().get(0);
            definitionText = firstMeaning.getDefinition();
        }
        tvDefinition.setText(definitionText);


        btnUsAudio.setEnabled(!TextUtils.isEmpty(currentWord.getUsAudio()));
        btnUkAudio.setEnabled(!TextUtils.isEmpty(currentWord.getUkAudio()));

        fabPlayRecording.setVisibility(View.GONE);
        File localFile = new File(localOutputFile);
        if (localFile.exists() && localFile.length() > 0) {
            fabPlayRecording.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        fabMicrophone.setOnClickListener(view -> handleMicButtonClick());
        fabPlayRecording.setOnClickListener(this::playLocalRecording);

        btnUsAudio.setOnClickListener(v -> playRemoteAudioWithExoPlayer(currentWord.getUsAudio()));
        btnUkAudio.setOnClickListener(v -> playRemoteAudioWithExoPlayer(currentWord.getUkAudio()));

        btnExamples.setOnClickListener(view -> {
            Intent intent = new Intent(PronunciationAssessmentActivity.this, WebViewActivity.class);
            String url = "https://content-media.elsanow.co/_static_/youglish.html?" + currentWord.getWord();
            intent.putExtra("URL", url);
            startActivity(intent);
        });

        btnBookmark.setOnClickListener(v -> {
            AddWordToFlashcardDialog dialog = AddWordToFlashcardDialog.newInstance(currentWord.getWord());
            dialog.show(getSupportFragmentManager(), "AddWordToFlashcardDialogTag");
            Toast.makeText(this, "Bookmark clicked (logic not implemented)", Toast.LENGTH_SHORT).show();
        });
    }


    private void handleMicButtonClick() {
        if (checkPermission()) {
            if (!isRecording) {
                try {
                    startRecording();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error starting recording", e);
                    Toast.makeText(this, "Error starting recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                stopRecordingAndAnalyze();
            }
        } else {
            requestPermission();
        }
    }

    private void startRecording() throws IOException {
        // Dừng ExoPlayer nếu đang phát khi bắt đầu ghi âm
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.stop();
        }
        setAudioButtonsEnabled(true); // Đảm bảo nút audio được enable lại

        audioHandler.startRecording(15000, this::stopRecordingAndAnalyze);
        updateUIRecordingStarted();
        isRecording = true;
        fabPlayRecording.setVisibility(View.GONE);
    }

    private void stopRecordingAndAnalyze() {
        if (!isRecording) return;

        audioHandler.stopRecording();
        updateUIRecordingStopped();
        isRecording = false;
        File recordedFile = new File(localOutputFile);
        // Chỉ cần check file tồn tại và có dữ liệu
        if (recordedFile.exists() && recordedFile.length() > 0) {
            fabPlayRecording.setVisibility(View.VISIBLE);
            analyzeSpeech();
        } else {
            fabPlayRecording.setVisibility(View.GONE);
            Toast.makeText(this, "Recording failed or empty.", Toast.LENGTH_SHORT).show();
        }
    }


    public void playLocalRecording(View view) {
        File file = new File(localOutputFile);
        if (file.exists() && file.length() > 0) {
            // Dừng ExoPlayer nếu đang phát
            if (exoPlayer != null && exoPlayer.isPlaying()) {
                exoPlayer.stop();
                setAudioButtonsEnabled(true); // Enable lại nút US/UK
            }
            // Tạm disable nút play local
            fabPlayRecording.setEnabled(false);
            audioHandler.playRecording(mp -> {
                // Re-enable nút khi phát xong
                if (fabPlayRecording != null) {
                    fabPlayRecording.setEnabled(true);
                }
            });
        } else {
            Toast.makeText(this, "No recording found or recording is empty", Toast.LENGTH_SHORT).show();
        }
    }


    // --- Sử dụng ExoPlayer để phát âm thanh từ URL ---
    private void playRemoteAudioWithExoPlayer(String url) {
        // Check cơ bản URL trống
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Audio URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đảm bảo ExoPlayer đã được khởi tạo
        if (exoPlayer == null) {
            initializeExoPlayer();
            // Check lại sau khi khởi tạo (trường hợp hiếm)
            if (exoPlayer == null) {
                Toast.makeText(this, "Audio player error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Dừng phát lại trước đó (nếu có)
        exoPlayer.stop();
        // Dừng phát local recording nếu đang chạy
        fabPlayRecording.setEnabled(true); // Enable lại nút play local

        // Tạo MediaItem từ URL (HTTPS được hỗ trợ mặc định)
        try {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            // Đặt media item và chuẩn bị phát
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.play(); // Bắt đầu phát
            // Listener sẽ xử lý trạng thái (buffering, playing, ended, error)
        } catch (Exception e) { // Bắt các lỗi phân tích URL hoặc lỗi khác
            Log.e(LOG_TAG, "Error preparing ExoPlayer for URL: " + url, e);
            Toast.makeText(this, "Error preparing audio", Toast.LENGTH_SHORT).show();
            setAudioButtonsEnabled(true); // Enable lại nút nếu có lỗi chuẩn bị
        }
    }

    // Helper method để enable/disable nút US/UK audio
    private void setAudioButtonsEnabled(boolean enabled) {
        // Chỉ enable nếu URL tương ứng không trống
        btnUsAudio.setEnabled(enabled && !TextUtils.isEmpty(currentWord.getUsAudio()));
        btnUkAudio.setEnabled(enabled && !TextUtils.isEmpty(currentWord.getUkAudio()));
    }


    // --- Các phương thức còn lại giữ nguyên ---

    private void updateUIRecordingStarted() {
        fabMicrophone.setImageResource(R.drawable.gif_audio_wave);
        Drawable drawable = fabMicrophone.getDrawable();
        ((AnimatedImageDrawable) drawable).start();
        fabMicrophone.setScaleX(3.0f);
        fabMicrophone.setScaleY(2.0f);
        fabMicrophone.setBackground(ContextCompat.getDrawable(this, R.color.white));
        fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        fabMicrophone.setRippleColor(ColorStateList.valueOf(Color.WHITE));
    }

    private void updateUIRecordingStopped() {
        Drawable drawable = fabMicrophone.getDrawable();
        if (drawable instanceof AnimatedImageDrawable) {
            ((AnimatedImageDrawable) drawable).stop();
        }
        fabMicrophone.clearAnimation();
        fabMicrophone.setScaleX(1.0f);
        fabMicrophone.setScaleY(1.0f);
        fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3399FF")));
        fabMicrophone.setImageResource(R.drawable.ic_image_voice2);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio recording permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio recording permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void analyzeSpeech() {
        File audioFile = new File(localOutputFile);
        // Check cơ bản file tồn tại và có dữ liệu
        if (!audioFile.exists() || audioFile.length() == 0) {
            Toast.makeText(this, "Recording not found or empty.", Toast.LENGTH_SHORT).show();
            fabPlayRecording.setVisibility(View.GONE);
            return;
        }
        // Giả định currentWord và word không null/trống ở đây

        RequestBody audioRequestBody = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), audioRequestBody);
        RequestBody targetWordBody = RequestBody.create(MediaType.parse("text/plain"), currentWord.getWord());

        LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Analyzing pronunciation...");
        Call<List<PhonemeComparison>> call = apiService.analyzeSpeech(audioPart, targetWordBody);

        call.enqueue(new Callback<List<PhonemeComparison>>() {
            @Override
            public void onResponse(@NonNull Call<List<PhonemeComparison>> call, @NonNull Response<List<PhonemeComparison>> response) {
                LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
                // Chỉ check response thành công và body không null
                if (response.isSuccessful() && response.body() != null) {
                    List<PhonemeComparison> resultList = response.body();
                    PhoneticFeedbackFragment dialogFragment = PhoneticFeedbackFragment.newInstance(resultList, currentWord.getWord());
                    try {
                        dialogFragment.show(getSupportFragmentManager(), "phonetic_feedback");
                    } catch (IllegalStateException e) {
                        Log.e(LOG_TAG, "Error showing feedback dialog", e);
                        Toast.makeText(PronunciationAssessmentActivity.this, "Analysis complete, cannot show feedback now.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(LOG_TAG, "Analysis API failed: Code=" + response.code() + ", Message=" + response.message());
                    Toast.makeText(PronunciationAssessmentActivity.this, "Analysis failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PhonemeComparison>> call, @NonNull Throwable t) {
                LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
                Log.e(LOG_TAG, "API call failed: ", t);
                Toast.makeText(PronunciationAssessmentActivity.this, "API error: Check connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFlashcardSelectedForWord(String word, FlashcardBasicResponse selectedFlashcard) {
        Toast.makeText(this, "Đang thêm '" + word + "' vào bộ '" + selectedFlashcard.getName() + "'...", Toast.LENGTH_LONG).show();
    }
}