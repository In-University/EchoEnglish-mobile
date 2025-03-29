package com.example.echoenglish_mobile.ui.pronunciation_assessment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.ui.activity.WebViewActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class PronunciationAssessmentActivity extends AppCompatActivity {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String outputFile;
    private FloatingActionButton fabMicrophone;
    private AnimationDrawable recordingAnimation;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_pronunciation);

        fabMicrophone = findViewById(R.id.fabMicrophone);

        // Thiết lập đường dẫn lưu file ghi âm
        outputFile = getExternalCacheDir().getAbsolutePath() + "/audiorecord.3gp";

        // Thiết lập sự kiện click cho nút microphone
        fabMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    if (!isRecording) {
                        startRecording();
                    } else {
                        stopRecording();
                    }
                } else {
                    requestPermission();
                }
            }
        });

        Button examplesButton = findViewById(R.id.examplesButton);
        examplesButton.setOnClickListener(view -> {
            Intent intent = new Intent(PronunciationAssessmentActivity.this, WebViewActivity.class);
            intent.putExtra("URL", "https://content-media.elsanow.co/_static_/youglish.html?communication");
            startActivity(intent);
        });

        PhoneticFeedbackFragment dialogFragment = PhoneticFeedbackFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager(), "phonetic_feedback");

    }


    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // MP3 cần MP4 container
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // AAC cho chất lượng MP3 tốt hơn
            mediaRecorder.setAudioChannels(1); // Mono
            mediaRecorder.setAudioSamplingRate(16000); // 16KHz
            mediaRecorder.setAudioEncodingBitRate(64000); // Bitrate cho âm thanh rõ

            mediaRecorder.setOutputFile(outputFile);
            mediaRecorder.prepare();
            mediaRecorder.start();
            new Handler(Looper.getMainLooper()).postDelayed(this::stopRecording, 10000);

            fabMicrophone.setImageResource(R.drawable.audio_wave);
            ((AnimatedImageDrawable) fabMicrophone.getDrawable()).start();
            fabMicrophone.setScaleX(3.0f);
            fabMicrophone.setScaleY(2.0f);
            fabMicrophone.setBackground(ContextCompat.getDrawable(this, R.color.white));
            fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            fabMicrophone.setRippleColor(ColorStateList.valueOf(Color.WHITE));
            isRecording = true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error when recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.d("error", e.getMessage());
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }

        // Trở lại giao diện ban đầu
        fabMicrophone.clearAnimation();
        fabMicrophone.setScaleX(1.0f);
        fabMicrophone.setScaleY(1.0f);
        fabMicrophone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3399FF")));
        fabMicrophone.setImageResource(R.drawable.ic_voice2);

        isRecording = false;
        Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
    }

    // play the recorded audio
    public void playRecording(View view) {
        File file = new File(outputFile);
        if (file.exists()) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(outputFile);
                mediaPlayer.prepare();
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mediaPlayer = null;
                    }
                });
            } catch (IOException e) {
                Toast.makeText(PronunciationAssessmentActivity.this, "Playback error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show();
        }
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
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}