package com.example.echoenglish_mobile.view.activity.chatbot;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.Chat2Adapter;
import com.example.echoenglish_mobile.model.ChatMessage;
import com.example.echoenglish_mobile.model.request.ConverseRequest;
import com.example.echoenglish_mobile.model.request.StartConversationRequest;
import com.example.echoenglish_mobile.model.response.ChecklistItemResponse;
import com.example.echoenglish_mobile.model.response.ConversationResponse;
import com.example.echoenglish_mobile.model.response.MessageHistoryItem;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConversationActivity extends AppCompatActivity implements ConversationInfoDialog.AudioSettingsListener {

    private static final String TAG = "ConversationActivity";
    public static final String EXTRA_CONTEXT = "EXTRA_CONTEXT"; // Key for intent extra

    private RecyclerView recyclerViewStream;
    private Chat2Adapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText editTextMessage; // Add EditText for typing
    private Button buttonSend; // Rename mic button or add a separate send button
    private ImageButton buttonShowInfo;
    private ImageButton buttonMic;
    private String currentVoiceId = "1";
    private float currentSpeed = 1.0f;
    private MediaPlayer dialogMediaPlayer;
    private ConversationInfoDialog activeDialog = null;

    // --- State for API Interaction ---
    private ApiService apiService;
    private String conversationContext = ""; // Store the context
    private List<ChecklistItemResponse> currentChecklist = new ArrayList<>();
    private List<MessageHistoryItem> conversationHistory = new ArrayList<>();
    private boolean isConversationStarted = false;
    private boolean isWaitingForResponse = false; // Prevent multiple simultaneous requests
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    // --- Threading ---
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // --- Get Context from Intent ---
        conversationContext = getIntent().getStringExtra(EXTRA_CONTEXT);
        if (conversationContext == null || conversationContext.trim().isEmpty()) {
            Log.e(TAG, "Conversation context is missing!");
            Toast.makeText(this, "Error: Conversation context not provided.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if context is missing
            return;
        }
        Log.d(TAG, "Conversation Context: " + conversationContext);

        // --- Initialize Views ---
        recyclerViewStream = findViewById(R.id.recyclerViewStream);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonShowInfo = findViewById(R.id.buttonShowInfo);
        buttonMic = findViewById(R.id.buttonMic);

        // --- Initialize API Service ---
        apiService = ApiClient.getApiService();

        // --- Setup RecyclerView ---
        chatMessages = new ArrayList<>();
        chatAdapter = new Chat2Adapter(this, chatMessages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewStream.setLayoutManager(layoutManager);
        recyclerViewStream.setAdapter(chatAdapter);
        recyclerViewStream.setItemAnimator(null);

         String initialUserGreeting = "Hi, let's talk about " + conversationContext;
         sendMessage(initialUserGreeting);

        // --- Setup Listeners ---
        buttonShowInfo.setOnClickListener(v -> showConversationInfo());
        buttonSend.setOnClickListener(v -> sendMessage(""));
        buttonMic.setOnClickListener(v -> startSpeechToText());
    }
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói gì đó...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "This device does not support speech recognition.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                sendMessage(result.get(0));
            }
        }
    }
    private void sendMessage(String userInput) {
        if(userInput.equals("")) {
            userInput = editTextMessage.getText().toString().trim();
        }
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isWaitingForResponse) {
            Toast.makeText(this, "Please wait for the current response", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Add User Message to UI and History ---
        ChatMessage userChatMessage = new ChatMessage(userInput, ChatMessage.SenderType.ME, System.currentTimeMillis());
        addNewMessageToUi(userChatMessage);
        conversationHistory.add(new MessageHistoryItem("user", userInput));
        editTextMessage.setText("");

        setLoadingState(true);

        if (!isConversationStarted) {
            callStartConversationApi(userInput);
        } else {
            callContinueConversationApi(userInput);
        }
    }

    private void callStartConversationApi(String initialInput) {
        StartConversationRequest request = new StartConversationRequest(conversationContext, initialInput);

        executorService.execute(() -> {
            try {
                Response<ConversationResponse> response = apiService.startChat(request).execute();
                mainThreadHandler.post(() -> handleApiResponse(response));
            } catch (Exception e) {
                Log.e(TAG, "API Call Failed (startChat): ", e);
                mainThreadHandler.post(() -> handleApiError("Network error. Could not start conversation."));
            }
        });
    }

    private void callContinueConversationApi(String currentUserInput) {
        List<MessageHistoryItem> historyCopy = new ArrayList<>(conversationHistory);
        List<ChecklistItemResponse> checklistCopy = new ArrayList<>(currentChecklist);

        ConverseRequest request = new ConverseRequest(conversationContext, checklistCopy, currentUserInput, historyCopy);

        executorService.execute(() -> {
            try {
                Response<ConversationResponse> response = apiService.continueChat(request).execute(); // Synchronous call
                mainThreadHandler.post(() -> handleApiResponse(response)); // Process response on main thread
            } catch (Exception e) {
                Log.e(TAG, "API Call Failed (continueChat): ", e);
                mainThreadHandler.post(() -> handleApiError("Network error during conversation."));
            }
        });
    }


    private void handleApiResponse(Response<ConversationResponse> response) {
        setLoadingState(false); // Hide progress bar

        if (response.isSuccessful() && response.body() != null) {
            ConversationResponse conversationResponse = response.body();
            Log.d(TAG, "API Success: AI Response: " + conversationResponse.getAiResponse());
            Log.d(TAG, "API Success: Checklist: " + conversationResponse.getUpdatedChecklist());
            Log.d(TAG, "API Success: All Completed: " + conversationResponse.isAllCompleted());


            // --- Update State ---
            isConversationStarted = true; // Mark as started after first successful call
            currentChecklist = conversationResponse.getUpdatedChecklist() != null ? conversationResponse.getUpdatedChecklist() : new ArrayList<>();

            // --- Add AI Response to UI and History ---
            String aiText = conversationResponse.getAiResponse();
            if (aiText != null && !aiText.trim().isEmpty()) {
                ChatMessage aiChatMessage = new ChatMessage(aiText, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
                addNewMessageToUi(aiChatMessage);
                conversationHistory.add(new MessageHistoryItem("assistant", aiText));
            } else {
                Log.w(TAG, "Received null or empty AI response text.");
            }

            if (conversationResponse.isAllCompleted()) {
                Toast.makeText(this, "Conversation goals achieved!", Toast.LENGTH_LONG).show();
                editTextMessage.setEnabled(false);
                buttonSend.setEnabled(false);
            }

        } else {
            handleApiError("Error: " + response.code() + " - " + response.message());
            Log.e(TAG, "API Error Body: " + response.errorBody());
        }
    }

    private void handleApiError(String errorMessage) {
        setLoadingState(false);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        ChatMessage errorMsg = new ChatMessage("Error: " + errorMessage, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
        addNewMessageToUi(errorMsg);
    }


    private void setLoadingState(boolean isLoading) {
        isWaitingForResponse = isLoading;
        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), "ConversationLoading", "...");
            buttonSend.setEnabled(false);
            editTextMessage.setEnabled(false);
        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
            buttonSend.setEnabled(true);
            editTextMessage.setEnabled(true);
        }
    }


    private void showConversationInfo() {
        activeDialog = ConversationInfoDialog.newInstance(conversationContext, new ArrayList<>(currentChecklist)); // Pass copies
        activeDialog.show(getSupportFragmentManager(), ConversationInfoDialog.TAG);
    }

    private void addNewMessageToUi(ChatMessage message) {
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
        executorService.shutdown();
    }

    // --- AudioSettingsListener Implementation ---
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