package com.example.echoenglish_mobile.view.activity.chatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.Chat2Adapter;
import com.example.echoenglish_mobile.model.ChatMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConversationActivity extends AppCompatActivity implements ConversationInfoDialog.AudioSettingsListener {

    private static final String TAG = "ConversationActivity";

    private RecyclerView recyclerViewStream;
    private Chat2Adapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ImageButton buttonMicCustom;
    private ImageButton buttonShowInfo;

    private String currentVoiceId = "1";
    private float currentSpeed = 1.0f;
    private MediaPlayer dialogMediaPlayer;
    private ConversationInfoDialog activeDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        recyclerViewStream = findViewById(R.id.recyclerViewStream);
        buttonMicCustom = findViewById(R.id.buttonMicCustom);
        buttonShowInfo = findViewById(R.id.buttonShowInfo);

        chatMessages = new ArrayList<>();
        chatAdapter = new Chat2Adapter(this, chatMessages, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewStream.setLayoutManager(layoutManager);
        recyclerViewStream.setAdapter(chatAdapter);
        recyclerViewStream.setItemAnimator(null);

        generateFakeMessages();
        scrollToBottom();

        buttonShowInfo.setOnClickListener(v -> showConversationInfo());
        buttonMicCustom.setOnClickListener(v -> {
            Log.d(TAG, "Microphone button clicked!");
            Toast.makeText(ConversationActivity.this, "Mic Clicked (Start Recording?)", Toast.LENGTH_SHORT).show();
            long now = System.currentTimeMillis();
            ChatMessage newMessage = new ChatMessage("This is a new message from me!", ChatMessage.SenderType.ME, now);
            addNewMessage(newMessage);
        });
    }

    private void generateFakeMessages() {
        long now = System.currentTimeMillis();
        chatMessages.add(new ChatMessage("Hey! What do you usually do in your free time?", ChatMessage.SenderType.OTHER, now - TimeUnit.MINUTES.toMillis(5)));
        chatMessages.add(new ChatMessage("Hi there! I love reading books, mostly fantasy novels. It's a great escape.", ChatMessage.SenderType.ME, now - TimeUnit.MINUTES.toMillis(4)));
        chatMessages.add(new ChatMessage("Oh, cool! Any favorite authors or series?", ChatMessage.SenderType.OTHER, now - TimeUnit.MINUTES.toMillis(3)));
        chatMessages.add(new ChatMessage("Definitely Brandon Sanderson. The Stormlight Archive is amazing! What about you?", ChatMessage.SenderType.ME, now - TimeUnit.MINUTES.toMillis(2)));
        chatMessages.add(new ChatMessage("I'm more into outdoor activities. Hiking, cycling... getting some fresh air.", ChatMessage.SenderType.OTHER, now - TimeUnit.MINUTES.toMillis(1)));
        chatMessages.add(new ChatMessage("That sounds refreshing too! I should try hiking sometime.", ChatMessage.SenderType.ME, now));
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }

    private void showConversationInfo() {
        activeDialog = ConversationInfoDialog.newInstance();
        activeDialog.show(getSupportFragmentManager(), ConversationInfoDialog.TAG);
    }

    private void addNewMessage(ChatMessage message) {
        chatMessages.add(message);
        if (chatAdapter != null) {
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (chatAdapter != null && chatAdapter.getItemCount() > 0) {
            recyclerViewStream.post(() -> recyclerViewStream.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDialogTTSPlayback();
        if (chatAdapter != null) {
            chatAdapter.releasePlayer();
        }
    }

    @Override
    public void onAudioSettingsChanged(String voiceId, float speed) {
        this.currentVoiceId = voiceId;
        this.currentSpeed = speed;
        if (chatAdapter != null) {
            chatAdapter.updateAudioSettings(voiceId, speed);
        }
    }

    @Override
    public String getCurrentVoiceId() {
        return this.currentVoiceId;
    }

    @Override
    public float getCurrentSpeed() {
        return this.currentSpeed;
    }
    private void stopDialogTTSPlayback() {
        if (dialogMediaPlayer != null) {
            try {
                if (dialogMediaPlayer.isPlaying()) {
                    dialogMediaPlayer.stop();
                }
                dialogMediaPlayer.reset();
                dialogMediaPlayer.release();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Dialog MediaPlayer IllegalStateException during stop/release", e);
            } finally {
                dialogMediaPlayer = null;
                Log.d(TAG, "Dialog MediaPlayer stopped and released.");
            }
        }
    }

    public void onDialogDismissed() {
        activeDialog = null;
        stopDialogTTSPlayback();
    }
}