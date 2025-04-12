package com.example.echoenglish_mobile.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

public class AudioHandler {
    private static final String TAG = "AudioHandler";

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String outputFile;
    private Context context;

    public AudioHandler(Context context, String outputFile) {
        this.context = context;
        this.outputFile = outputFile;
    }

    /**
     * Bắt đầu ghi âm với thời gian tối đa (durationMillis). Sau thời gian này,
     * ghi âm sẽ tự dừng và gọi callback onRecordingStopped nếu không null.
     */
    public void startRecording(long durationMillis, Runnable onRecordingStopped) throws IOException {
        initMediaRecorder();
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        // Dừng ghi âm sau durationMillis (ví dụ: 15000ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                stopRecording();
                if (onRecordingStopped != null) {
                    onRecordingStopped.run();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recording", e);
            }
        }, durationMillis);
    }

    private void initMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Dùng MP4 container
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);   // Dùng AAC cho chất lượng tốt
        mediaRecorder.setAudioChannels(1); // Mono
        mediaRecorder.setAudioSamplingRate(16000); // 16KHz
        mediaRecorder.setAudioEncodingBitRate(64000); // Bitrate cho âm thanh rõ
        mediaRecorder.setOutputFile(outputFile);
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "stopRecording exception: " + e.getMessage(), e);
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    /**
     * Phát âm thanh đã ghi. Callback onCompletionListener sẽ được gọi khi phát xong.
     */
    public void playRecording(MediaPlayer.OnCompletionListener onCompletionListener) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(outputFile);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion(mp);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "playRecording error: " + e.getMessage(), e);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Giải phóng tài nguyên, gọi khi Activity/Fragment dừng.
     */
    public void release() {
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
