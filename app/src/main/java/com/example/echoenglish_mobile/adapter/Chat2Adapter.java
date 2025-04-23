package com.example.echoenglish_mobile.adapter;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.ChatMessage;
import com.example.echoenglish_mobile.view.activity.chatbot.ConversationInfoDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Chat2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "Chat2Adapter";
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private Context context;
    private List<ChatMessage> chatMessages;
    private MediaPlayer mediaPlayer;
    private ViewHolder currentlyPlayingHolder = null;
    private Handler durationHandler = new Handler(Looper.getMainLooper());
    private Runnable updateDurationRunnable;

    private ConversationInfoDialog.AudioSettingsListener settingsListener;
    private String currentAdapterVoiceId;
    private float currentAdapterSpeed;


    public Chat2Adapter(Context context, List<ChatMessage> chatMessages, ConversationInfoDialog.AudioSettingsListener settingsListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.settingsListener = settingsListener;
        if (settingsListener != null) {
            this.currentAdapterVoiceId = settingsListener.getCurrentVoiceId();
            this.currentAdapterSpeed = settingsListener.getCurrentSpeed();
        } else {
            this.currentAdapterVoiceId = "1";
            this.currentAdapterSpeed = 1.0f;
            Log.w(TAG, "AudioSettingsListener is null, using default audio settings.");
        }
    }

    public void updateAudioSettings(String voiceId, float speed) {
        this.currentAdapterVoiceId = voiceId;
        this.currentAdapterSpeed = speed;
        Log.d(TAG, "Adapter settings updated - Voice ID: " + voiceId + ", Speed: " + speed);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return (message.getSenderType() == ChatMessage.SenderType.ME) ? VIEW_TYPE_ME : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_stream_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        ViewHolder messageHolder = (ViewHolder) holder;
        messageHolder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSenderInfo;
        TextView textViewMessageContent;
        ImageView playButton;
        TextView audioDuration;
        ConstraintLayout contentContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSenderInfo = itemView.findViewById(R.id.textViewSenderInfo);
            textViewMessageContent = itemView.findViewById(R.id.textViewMessageContent);
            playButton = itemView.findViewById(R.id.playButton);
            audioDuration = itemView.findViewById(R.id.audioDuration);
            contentContainer = itemView.findViewById(R.id.messageContentContainer);
        }

        void bind(ChatMessage message) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String time = sdf.format(new Date(message.getTimestamp()));
            String sender = (message.getSenderType() == ChatMessage.SenderType.ME) ? "Me" : "AI Assistant";
            textViewSenderInfo.setText(String.format("%s - %s", sender, time));

            textViewMessageContent.setText(message.getMessage());

            if (this == currentlyPlayingHolder && mediaPlayer != null && mediaPlayer.isPlaying()) {
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                updateDuration(this);
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play);
                audioDuration.setText("0:00");
            }

            playButton.setOnClickListener(v -> {
                if (this == currentlyPlayingHolder && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    stopPlayingAudio();
                } else {
                    playAudio(message.getMessage(), this);
                }
            });


            if (message.getSenderType() == ChatMessage.SenderType.ME) {
                textViewSenderInfo.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                contentContainer.setBackgroundResource(R.drawable.bg_message_text_me);
            } else {
                textViewSenderInfo.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                contentContainer.setBackgroundResource(R.drawable.bg_message_text);
            }
        }
    }

    public void playAudio(String text, ViewHolder holder) {
        stopPlayingAudio();

        String audioUrl = "https://classmate-vuive.vn/tts?text=" + text + "&voice=" + currentAdapterVoiceId + "&chunk=0";
        Log.d(TAG, "Playing audio from URL: " + audioUrl + " with Speed: " + currentAdapterSpeed + " Voice: " + currentAdapterVoiceId);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared, starting playback.");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        PlaybackParams params = new PlaybackParams();
                        params.setSpeed(currentAdapterSpeed);
                        mediaPlayer.setPlaybackParams(params);
                        Log.d(TAG, "Playback speed set to: " + currentAdapterSpeed);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error setting playback speed " + currentAdapterSpeed, e);
                        Toast.makeText(context, "Unsupported speed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG,"Playback speed control not available on this API level.");
                }

                mp.start();
                currentlyPlayingHolder = holder;
                holder.playButton.setImageResource(android.R.drawable.ic_media_pause);
                updateDuration(holder);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "MediaPlayer playback completed.");
                stopPlayingAudio();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
                stopPlayingAudio();
                return true;
            });
            mediaPlayer.prepareAsync();
            holder.audioDuration.setText("...");

        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "MediaPlayer setup failed", e);
            Toast.makeText(context, "Cannot play audio", Toast.LENGTH_SHORT).show();
            stopPlayingAudio();
        }
    }


    public void stopPlayingAudio() {
        durationHandler.removeCallbacks(updateDurationRunnable);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer IllegalStateException during stop/release", e);
            } finally {
                mediaPlayer = null;
            }
        }

        if (currentlyPlayingHolder != null) {
            currentlyPlayingHolder.audioDuration.setText("0:00");
            currentlyPlayingHolder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }
        currentlyPlayingHolder = null;
    }

    private void updateDuration(final ViewHolder holder) {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && holder == currentlyPlayingHolder) {
            try {
                long currentPosition = mediaPlayer.getCurrentPosition();
                String currentPositionStr = formatDuration(currentPosition);
                holder.audioDuration.setText(currentPositionStr);
                updateDurationRunnable = () -> updateDuration(holder);
                durationHandler.postDelayed(updateDurationRunnable, 500);
            } catch (IllegalStateException e) {
                Log.e(TAG, "MediaPlayer not ready for getCurrentPosition", e);
                stopPlayingAudio();
            }
        } else {
            durationHandler.removeCallbacks(updateDurationRunnable);
            if(holder != null && holder != currentlyPlayingHolder) {
                holder.audioDuration.setText(String.valueOf(mediaPlayer.getDuration()));
                holder.playButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    private String formatDuration(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public void releasePlayer() {
        Log.d(TAG, "Releasing Adapter MediaPlayer");
        stopPlayingAudio();
        settingsListener = null;
    }
}