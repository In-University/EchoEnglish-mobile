package com.example.echoenglish_mobile.ui.pronunciation_assessment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.PhonemeComparisonDTO;
import com.example.echoenglish_mobile.data.remote.ApiClient;
import com.example.echoenglish_mobile.data.remote.ApiService;
import com.example.echoenglish_mobile.ui.activity.WebViewActivity;
import com.example.echoenglish_mobile.utils.AudioHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class PronunciationAssessmentActivity extends AppCompatActivity {
    private AudioHandler audioHandler;
    private FloatingActionButton fabMicrophone;
    private boolean isRecording = false;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 200;
    private String outputFile;
    ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pronunciation);

        fabMicrophone = findViewById(R.id.fabMicrophone);

        // Thiết lập đường dẫn lưu file ghi âm
        outputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";
        audioHandler = new AudioHandler(this, outputFile);
        apiService = ApiClient.getApiService();

        // Thiết lập sự kiện click cho nút microphone
        fabMicrophone.setOnClickListener(view -> {
            if (checkPermission()) {
                if (!isRecording) {
                    try {
                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error when recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopRecording();
                }
            } else {
                requestPermission();
            }
        });

        Button examplesButton = findViewById(R.id.examplesButton);
        examplesButton.setOnClickListener(view -> {
            Intent intent = new Intent(PronunciationAssessmentActivity.this, WebViewActivity.class);
            intent.putExtra("URL", "https://content-media.elsanow.co/_static_/youglish.html?communication");
            startActivity(intent);
        });


    }

    private void startRecording() throws IOException {
        // Bắt đầu ghi âm trong 15 giây
        audioHandler.startRecording(15000, this::stopRecording);
        updateUIRecordingStarted();
        isRecording = true;
    }

    private void stopRecording() {
        audioHandler.stopRecording();
        updateUIRecordingStopped();
        isRecording = false;
        Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
//        PhoneticFeedbackFragment dialogFragment = PhoneticFeedbackFragment.newInstance(new ArrayList<>(), "communication");
//        dialogFragment.show(getSupportFragmentManager(), "phonetic_feedback");
        analyzeSpeech();
    }

    public void playRecording(View view) {
        File file = new File(outputFile);
        if (file.exists()) {
            audioHandler.playRecording(mp -> {
                // Thực hiện sau khi phát xong (nếu cần)
            });
        } else {
            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIRecordingStarted() {
        fabMicrophone.setImageResource(R.drawable.audio_wave);
        Drawable drawable = fabMicrophone.getDrawable();
        ((AnimatedImageDrawable) drawable).start();
        fabMicrophone.setScaleX(3.0f);
        fabMicrophone.setScaleY(2.0f);
        fabMicrophone.setBackground(ContextCompat.getDrawable(this, R.color.white));
        fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        fabMicrophone.setRippleColor(ColorStateList.valueOf(Color.WHITE));
    }

    private void updateUIRecordingStopped() {
        fabMicrophone.clearAnimation();
        fabMicrophone.setScaleX(1.0f);
        fabMicrophone.setScaleY(1.0f);
        fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3399FF")));
        fabMicrophone.setImageResource(R.drawable.ic_voice2);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_AUDIO_PERMISSION_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio recording permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio recording permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        audioHandler.release();
    }

    private void analyzeSpeech() {
        Call<List<PhonemeComparisonDTO>> call = apiService.uploadAudio();
        call.enqueue(new retrofit2.Callback<List<PhonemeComparisonDTO>>() {
            @Override
            public void onResponse(Call<List<PhonemeComparisonDTO>> call, retrofit2.Response<List<PhonemeComparisonDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PhonemeComparisonDTO> resultList = response.body();
                    Toast.makeText(PronunciationAssessmentActivity.this, "Analysis complete", Toast.LENGTH_SHORT).show();
                    // Truyền kết quả sang dialog feedback
                    PhoneticFeedbackFragment dialogFragment = PhoneticFeedbackFragment.newInstance(resultList, "communication");
                    dialogFragment.show(getSupportFragmentManager(), "phonetic_feedback");
                } else {
                    Toast.makeText(PronunciationAssessmentActivity.this, "Analysis failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PhonemeComparisonDTO>> call, Throwable t) {
                Log.d("ERROR:::::", t.getMessage());
                Toast.makeText(PronunciationAssessmentActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}