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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView; // Thêm import này
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
import com.google.gson.Gson;

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
    public static final String EXTRA_CONTEXT = "EXTRA_CONTEXT";
    public static final String EXTRA_START_CONVERSATION_JSON = "EXTRA_START_CONVERSATION_JSON";
    private RecyclerView recyclerViewStream;
    private Chat2Adapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonShowInfo;
    private ImageButton buttonMic;
    private String currentVoiceId = "1";
    private float currentSpeed = 1.0f;
    private MediaPlayer dialogMediaPlayer;
    private ConversationInfoDialog activeDialog = null;
    private ConstraintLayout inputContainer;
    private FrameLayout congratulationsContainer;
    private LottieAnimationView lottieAnimationView;

    private ApiService apiService;
    private String conversationContext = "";
    private List<ChecklistItemResponse> currentChecklist = new ArrayList<>();
    private List<MessageHistoryItem> conversationHistory = new ArrayList<>();
    private boolean isConversationStarted = false;
    private boolean isWaitingForResponse = false;
    private boolean isConversationCompleted = false;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        String startResponseJson = getIntent().getStringExtra(EXTRA_START_CONVERSATION_JSON);
        String contextFromExtra = getIntent().getStringExtra(EXTRA_CONTEXT);
        ConversationResponse initialResponseData = null;

        // 1. Determine Conversation Context (MUST have context)
        if (contextFromExtra != null && !contextFromExtra.trim().isEmpty()) {
            conversationContext = contextFromExtra;
        } else {
            Toast.makeText(this, "Error: Conversation context not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (startResponseJson != null && !startResponseJson.trim().isEmpty()) {
            Log.d(TAG, "Attempting to initialize from JSON Response: " + startResponseJson);
            try {
                Gson gson = new Gson();
                initialResponseData = gson.fromJson(startResponseJson, ConversationResponse.class);

                if (initialResponseData != null && initialResponseData.getAiResponse() != null) {
                    isConversationStarted = true;
                    currentChecklist = initialResponseData.getUpdatedChecklist() != null ? initialResponseData.getUpdatedChecklist() : new ArrayList<>();
                    isConversationCompleted = initialResponseData.isAllCompleted();
                    Log.d(TAG, "Successfully initialized state from JSON response.");
                } else {
                    Log.w(TAG, "Parsed initial response data or AI response was null. Falling back.");
                    initialResponseData = null; // Ensure fallback
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse start conversation JSON response: " + startResponseJson, e);
                Toast.makeText(this, "Error: Invalid start conversation data format. Using context only.", Toast.LENGTH_LONG).show();
                initialResponseData = null;
            }
        }

        // --- Find Views
        recyclerViewStream = findViewById(R.id.recyclerViewStream);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonShowInfo = findViewById(R.id.buttonShowInfo);
        buttonMic = findViewById(R.id.buttonMic);
        inputContainer = findViewById(R.id.inputContainer);
        congratulationsContainer = findViewById(R.id.congratulationsContainer);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        // --- Initialize API Service and Chat List/Adapter
        apiService = ApiClient.getApiService();
        chatMessages = new ArrayList<>();
        chatAdapter = new Chat2Adapter(this, chatMessages, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewStream.setLayoutManager(layoutManager);
        recyclerViewStream.setAdapter(chatAdapter);
        recyclerViewStream.setItemAnimator(null);

        if (initialResponseData != null) {
            String aiText = initialResponseData.getAiResponse();
            if (aiText != null && !aiText.trim().isEmpty()) {
                ChatMessage aiChatMessage = new ChatMessage(aiText, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
                chatMessages.add(aiChatMessage);
                conversationHistory.add(new MessageHistoryItem("assistant", aiText));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                mainThreadHandler.post(() -> recyclerViewStream.smoothScrollToPosition(chatMessages.size() - 1));
                Log.d(TAG, "Initial AI Response from JSON added to UI.");
            } else {
                Log.w(TAG,"Initial JSON response had empty AI message, none added.");
            }

            if (isConversationCompleted) {
                Log.d(TAG, "Conversation loaded as already completed from JSON.");
                mainThreadHandler.post(() -> {
                    showCongratulationsScreen();
                    hideInputControls();
                    mainThreadHandler.postDelayed(this::hideCongratulationsScreen, 5000);
                    mainThreadHandler.postDelayed(this::requestConversationReview, 150);
                });
            }

        } else {
            Log.d(TAG, "Initializing normally: Sending initial user message to start conversation.");
            String initialUserGreeting = "Hi, let's talk about " + conversationContext;

            sendMessage(initialUserGreeting);
        }


        // --- Set Listeners and Initial View State
        buttonShowInfo.setOnClickListener(v -> showConversationInfo());
        buttonSend.setOnClickListener(v -> sendMessage(""));
        buttonMic.setOnClickListener(v -> startSpeechToText());

        if (congratulationsContainer != null) {
            congratulationsContainer.setVisibility(View.GONE);
        }
    }
    private void startSpeechToText() {
        if (isConversationCompleted) return;

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
            if (isConversationStarted) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (isWaitingForResponse) {
            Toast.makeText(this, "Please wait for the current response", Toast.LENGTH_SHORT).show();
            return;
        }

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
                Response<ConversationResponse> response = apiService.continueChat(request).execute();
                mainThreadHandler.post(() -> handleApiResponse(response));
            } catch (Exception e) {
                Log.e(TAG, "API Call Failed (continueChat): ", e);
                mainThreadHandler.post(() -> handleApiError("Network error during conversation."));
            }
        });
    }


    private void handleApiResponse(Response<ConversationResponse> response) {
        setLoadingState(false);

        if (response.isSuccessful() && response.body() != null) {
            ConversationResponse conversationResponse = response.body();
            isConversationStarted = true;
            currentChecklist = conversationResponse.getUpdatedChecklist() != null ? conversationResponse.getUpdatedChecklist() : new ArrayList<>();

            String aiText = conversationResponse.getAiResponse();

            if (aiText != null && !aiText.trim().isEmpty()) {
                ChatMessage aiChatMessage = new ChatMessage(aiText, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
                addNewMessageToUi(aiChatMessage);
                conversationHistory.add(new MessageHistoryItem("assistant", aiText));
                Log.d(TAG, "AI Response added: " + aiText.substring(0, Math.min(aiText.length(), 50)) + "...");
            } else {
                Log.w(TAG, "Received null or empty AI response text.");
            }


            if (conversationResponse.isAllCompleted() && !isConversationCompleted) {
                isConversationCompleted = true;
                showCongratulationsScreen();
                hideInputControls();
                mainThreadHandler.postDelayed(this::hideCongratulationsScreen, 5000);
                mainThreadHandler.postDelayed(this::requestConversationReview, 150);
            }

        } else {
            String errorMessage = "Error: " + response.code() + " - " + response.message();
            handleApiError(errorMessage);
            if (response.errorBody() != null) {
                try {
                    Log.e(TAG, "API Error Body: " + response.errorBody().string());
                } catch (Exception e) { Log.e(TAG, "Error reading error body", e); }
            }
        }
    }

    private void requestConversationReview() {
        List<MessageHistoryItem> historyCopy = new ArrayList<>(conversationHistory);
        List<ChecklistItemResponse> checklistCopy = new ArrayList<>(currentChecklist);

        ConverseRequest reviewRequest = new ConverseRequest(conversationContext, checklistCopy, "", historyCopy);

        Call<ConversationResponse> call = apiService.reviewConversation(reviewRequest);

        call.enqueue(new Callback<ConversationResponse>() {
            @Override
            public void onResponse(@NonNull Call<ConversationResponse> call, @NonNull Response<ConversationResponse> response) {
                setLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    String reviewText = response.body().getAiResponse();
                    if (reviewText != null && !reviewText.trim().isEmpty()) {
                        Log.d(TAG, "Review received via API: " + reviewText.substring(0, Math.min(reviewText.length(), 100)) + "...");
                        ChatMessage reviewMessage = new ChatMessage(reviewText, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
                        addNewMessageToUi(reviewMessage);
                    } else {
                        Log.w(TAG, "Review API response successful but review text was empty.");
                    }
                } else {
                    String errorMsg = "Failed to get review via API: " + response.code() + " " + response.message();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConversationResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Network error requesting review: " + t.getMessage(), t);
            }
        });
    }

    private void handleApiError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        ChatMessage errorMsg = new ChatMessage("Error: " + errorMessage, ChatMessage.SenderType.OTHER, System.currentTimeMillis());
        addNewMessageToUi(errorMsg);
    }


    private void setLoadingState(boolean isLoading) {
        isWaitingForResponse = isLoading;
        if (isLoading) { // Chỉ hiển thị loading cho user
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), "ConversationLoading", "...");
            buttonSend.setEnabled(false);
            editTextMessage.setEnabled(false);
            buttonMic.setEnabled(false); // Disable mic button khi loading
        } else if (!isLoading && !isConversationCompleted) { // Chỉ enable lại nếu chưa hoàn thành
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
            buttonSend.setEnabled(true);
            editTextMessage.setEnabled(true);
            buttonMic.setEnabled(true); // Enable mic button lại
        } else if (!isLoading && isConversationCompleted) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
            hideInputControls();
        }
    }

    private void showCongratulationsScreen() {
        if (congratulationsContainer != null) {
            congratulationsContainer.setVisibility(View.VISIBLE);
            if (lottieAnimationView != null) {
                lottieAnimationView.playAnimation();
            }
        }
        if (buttonShowInfo != null) { // Ẩn cả nút info
            buttonShowInfo.setVisibility(View.GONE);
        }
    }
    private void hideCongratulationsScreen() {
        if (congratulationsContainer != null && congratulationsContainer.getVisibility() == View.VISIBLE) {
            congratulationsContainer.setVisibility(View.GONE);
            if (lottieAnimationView != null) { // Dừng animation khi ẩn
                lottieAnimationView.cancelAnimation();
            }
            Log.d(TAG,"Congratulations screen hidden after delay.");
            scrollToBottom();
        }
    }

    private void hideInputControls() {
        if (inputContainer != null) {
            inputContainer.setVisibility(View.GONE);
        }
        if (editTextMessage != null) {
            editTextMessage.setVisibility(View.GONE);
            editTextMessage.setEnabled(false);
        }
        if (buttonSend != null) {
            buttonSend.setVisibility(View.GONE);
            buttonSend.setEnabled(false);
        }
        if (buttonMic != null) {
            buttonMic.setVisibility(View.GONE);
            buttonMic.setEnabled(false);
        }
    }


    private void showConversationInfo() {
        if (isConversationCompleted) return; // Không hiển thị nếu đã hoàn thành
        activeDialog = ConversationInfoDialog.newInstance(conversationContext, new ArrayList<>(currentChecklist));
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
        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation();
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