package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.DialogInterface; // Import DialogInterface
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment; // Import LoadingDialogFragment
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton; // Import MaterialButton

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Game2Activity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game2Activity";

    private static final long CURRENT_USER_ID = 27L; // Example user ID
    private static final String LOADING_DIALOG_TAG = "Game2LoadingDialog"; // Tag for loading dialog

    // --- View References ---
    // loadingContainer and gameContent views from XML are no longer used directly for visibility
    // private LinearLayout loadingContainer;
    private ConstraintLayout gameContent; // Still reference the container layout

    private TextView textGame2Progress;
    private ImageButton buttonGame2PlaySound; // Corrected name
    private TextView textViewListenInstruction; // Corrected name
    private GridLayout gridGame2Answers; // Corrected name
    private MaterialButton buttonGame2Skip; // Corrected name, added button


    // --- Game Data ---
    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private VocabularyResponse currentCorrectVocab;
    private List<VocabularyResponse> currentOptions = new ArrayList<>();

    // --- Audio ---
    private TextToSpeech tts;
    private boolean ttsInitialized = false;
    private SoundPool soundPool; // SoundPool for sound effects
    private int soundCorrect; // ID for correct sound
    private int soundIncorrect; // ID for incorrect sound

    // --- API ---
    private ApiService apiService;
    private int loadingApiCount = 0; // Counter for API calls

    // Handler for delays
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
        setTitle("Game: Listen & Match Image"); // Translated title

        // Ánh xạ View (từ gameContent nếu cần)
        gameContent = findViewById(R.id.gameContent); // Still get the container
        textGame2Progress = findViewById(R.id.textGame2Progress);
        buttonGame2PlaySound = findViewById(R.id.buttonGame2PlaySound); // Corrected name
        textViewListenInstruction = findViewById(R.id.textViewListenInstruction); // Corrected name
        gridGame2Answers = findViewById(R.id.gridGame2Answers); // Corrected name
        buttonGame2Skip = findViewById(R.id.buttonGame2Skip); // Added skip button reference


        // Khởi tạo ApiService
        apiService = ApiClient.getApiService();

        // Nhận dữ liệu
        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        // Check vocab list size early, but don't load game yet
        if (vocabularyList == null || vocabularyList.size() < 4) {
            Log.e(TAG, "Not enough vocabularies received for game (need at least 4).");
            Toast.makeText(this, "Not enough vocabularies for this game.", Toast.LENGTH_LONG).show(); // Translated
            finish();
            return; // Exit onCreate
        }

        Collections.shuffle(vocabularyList); // Xáo trộn danh sách từ vựng

        // Hiển thị loading dialog ngay lập tức để chờ TTS và SoundPool
        startApiCall("Preparing audio and game..."); // Translated loading message

        // Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, this);
        buttonGame2PlaySound.setEnabled(false); // Disable sound button initially

        // Khởi tạo SoundPool và tải âm thanh
        loadSounds();

        // Vô hiệu hóa các controls game ban đầu (có thể gọi enable sau khi game sẵn sàng)
        setGameControlsEnabled(false);


        // Set up listeners
        buttonGame2PlaySound.setOnClickListener(v -> speakWord());
        buttonGame2Skip.setOnClickListener(v -> { // Add listener for Skip button
            setGameControlsEnabled(false); // Disable controls immediately
            if (currentQuestionIndex < vocabularyList.size()) {
                long vocabId = vocabularyList.get(currentQuestionIndex).getId();
                // Record as forgotten when skipping
                recordLearningProgress(vocabId, false);
            }
            goToNextQuestion(); // Move to next question
        });


        // loadQuestion() will be called in onInit() after TTS is ready
    }


    // Khởi tạo SoundPool và tải âm thanh
    private void loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1) // Allow only one sound effect at a time
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        // Tải âm thanh
        soundCorrect = soundPool.load(this, R.raw.correct_answer, 1); // Replace with your sound file IDs
        soundIncorrect = soundPool.load(this, R.raw.wrong_answer, 1);

        Log.d(TAG, "SoundPool initialized and loading sounds. Correct ID: " + soundCorrect + ", Incorrect ID: " + soundIncorrect);
        // No need to call finishApiCall here, it's called after TTS init
    }


    // Xử lý sau khi TTS được khởi tạo
    @Override
    public void onInit(int status) {
        runOnUiThread(() -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS Language not supported");
                    Toast.makeText(this, "TTS language (US English) not supported.", Toast.LENGTH_LONG).show(); // Translated
                    handleTTSInitializationFailure("Language not supported.");
                } else {
                    ttsInitialized = true;
                    buttonGame2PlaySound.setEnabled(true); // Enable sound button
                    Log.d(TAG, "TTS Initialized successfully.");

                    // TTS ready, hide loading and show game content (by enabling controls)
                    finishApiCall();
                    loadQuestion(); // Load first question and trigger auto-speak
                    setGameControlsEnabled(true); // Enable game controls

                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status: " + status);
                Toast.makeText(this, "TTS initialization failed.", Toast.LENGTH_LONG).show(); // Translated
                handleTTSInitializationFailure("Initialization failed.");
                finishApiCall(); // Hide loading dialog even on failure
            }
        });
    }

    // Handle what happens when TTS initialization fails
    private void handleTTSInitializationFailure(String message) {
        // Still show the game content but disable sound features
        buttonGame2PlaySound.setEnabled(false); // Ensure disabled
        // Optionally hide the button
        // buttonGame2PlaySound.setVisibility(View.GONE);
        Log.e(TAG, "TTS functionality disabled due to failure: " + message);
        Toast.makeText(this, "Vocabulary audio function unavailable.", Toast.LENGTH_LONG).show(); // Translated
        // Game is still playable without sound
    }


    // ... (Rest of loadQuestion, speakWord, checkAnswer, highlightCorrectAnswer, etc. methods) ...

    // Hiển thị câu hỏi và các lựa chọn
    private void loadQuestion() {
        // Re-enable controls at the start of a new question
        setGameControlsEnabled(true);

        if (currentQuestionIndex >= vocabularyList.size()) {
            endGame();
            return;
        }

        currentCorrectVocab = vocabularyList.get(currentQuestionIndex);
        currentOptions.clear();
        gridGame2Answers.removeAllViews(); // Corrected name

        // Update progress text
        textGame2Progress.setText(String.format(Locale.getDefault(), "Question %d / %d", // Translated
                currentQuestionIndex + 1, vocabularyList.size()));

        // Prepare 4 options (1 correct, 3 wrong)
        currentOptions.add(currentCorrectVocab); // Add the correct answer

        // Get 3 random wrong options from the rest of the list
        List<VocabularyResponse> wrongOptionsPool = new ArrayList<>(vocabularyList);

        // Remove the current correct vocabulary safely.
        boolean removed = false;
        for(int i = 0; i < wrongOptionsPool.size(); i++) {
            if (wrongOptionsPool.get(i).getId().equals(currentCorrectVocab.getId())) {
                wrongOptionsPool.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed && vocabularyList.contains(currentCorrectVocab)) {
            Log.w(TAG, "Could not remove currentCorrectVocab from wrongOptionsPool by ID: " + currentCorrectVocab.getId());
        }

        Collections.shuffle(wrongOptionsPool);

        int neededWrong = 3;
        int addedWrong = 0;
        for (int i = 0; i < wrongOptionsPool.size() && addedWrong < neededWrong; i++) {
            VocabularyResponse wrongOption = wrongOptionsPool.get(i);
            boolean alreadyAdded = false;
            for (VocabularyResponse option : currentOptions) {
                if (option.getId().equals(wrongOption.getId())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                currentOptions.add(wrongOption);
                addedWrong++;
            }
        }

        if (currentOptions.size() < 4) {
            Log.e(TAG, "Could not generate 4 unique options! Needed " + 4 + ", got " + currentOptions.size());
            Toast.makeText(this, "Error generating options. Not enough unique vocabularies.", Toast.LENGTH_SHORT).show(); // Translated
            finish(); // Cannot proceed with the game
            return;
        }

        Collections.shuffle(currentOptions);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyResponse option : currentOptions) {
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_game2_answer, gridGame2Answers, false); // Corrected grid name

            ImageView imageView = cardView.findViewById(R.id.imageGame2Answer);
            TextView textView = cardView.findViewById(R.id.textGame2AnswerDefinition);

            textView.setText(option.getDefinition());
            Glide.with(this)
                    .load(option.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageView);

            cardView.setTag(option);

            cardView.setOnClickListener(v -> {
                VocabularyResponse selected = (VocabularyResponse) v.getTag();
                if (selected != null) {
                    checkAnswer(selected, (CardView) v); // Pass selected option and card
                } else {
                    Log.e(TAG, "Failed to retrieve VocabularyResponse from tag!");
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin),
                    getResources().getDimensionPixelSize(R.dimen.grid_item_margin));
            cardView.setLayoutParams(params);

            gridGame2Answers.addView(cardView); // Corrected grid name
        }

        // Word pronunciation is now only triggered by button click
        // speakWord(); // NO LONGER CALLED AUTOMATICALLY
    }

    // Phát âm thanh từ tiếng Anh
    private void speakWord() {
        if (ttsInitialized && currentCorrectVocab != null) {
            String wordToSpeak = currentCorrectVocab.getWord();
            if (wordToSpeak != null && !wordToSpeak.isEmpty()) {
                tts.stop(); // Stop any ongoing speech
                tts.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "Speak_" + currentCorrectVocab.getId());
            } else {
                Log.w(TAG, "speakWord: currentCorrectVocab or its word is null/empty.");
            }
        } else if (!ttsInitialized) {
            Log.w(TAG, "speakWord called but TTS not initialized.");
            // Error message already handled in onInit/handleTTSInitializationFailure
        }
    }


    private void highlightCorrectAnswer() {
        for (int i = 0; i < gridGame2Answers.getChildCount(); i++) { // Corrected grid name
            View child = gridGame2Answers.getChildAt(i); // Corrected grid name
            if (child instanceof MaterialCardView) {
                MaterialCardView cardView = (MaterialCardView) child;
                VocabularyResponse option = (VocabularyResponse) cardView.getTag();
                if (option != null && currentCorrectVocab != null && option.getId() != null && option.getId().equals(currentCorrectVocab.getId())) {
                    cardView.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                    cardView.setStrokeColor(ContextCompat.getColor(this, R.color.highlight_blue));
                    break;
                }
            }
        }
    }

    // Kiểm tra đáp án người dùng chọn
    private void checkAnswer(VocabularyResponse selectedOption, CardView selectedCardView) {
        disableAnswerButtons();
        buttonGame2PlaySound.setEnabled(false); // Disable sound button during check

        boolean isCorrect = selectedOption != null && currentCorrectVocab != null &&
                selectedOption.getId() != null && currentCorrectVocab.getId() != null &&
                selectedOption.getId().equals(currentCorrectVocab.getId());

        // Play sound feedback
        if (soundPool != null) {
            if (isCorrect) {
                if (soundCorrect != 0) soundPool.play(soundCorrect, 1, 1, 0, 0, 1);
            } else {
                if (soundIncorrect != 0) soundPool.play(soundIncorrect, 1, 1, 0, 0, 1);
            }
        }

        long currentVocabId = currentCorrectVocab != null && currentCorrectVocab.getId() != null ? currentCorrectVocab.getId() : -1L;

        if (isCorrect) {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_green));
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show(); // Translated
            if (currentVocabId != -1L) recordLearningProgress(currentVocabId, true);

        } else {
            selectedCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.incorrect_red));
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show(); // Translated
            highlightCorrectAnswer();
            if (currentVocabId != -1L) recordLearningProgress(currentVocabId, false);
        }

        // Wait a bit then go to the next question
        mainHandler.postDelayed(this::goToNextQuestion, 1500); // Wait 1.5 seconds (can adjust)
    }

    // Helper to disable all card buttons
    private void disableAnswerButtons() {
        for (int i = 0; i < gridGame2Answers.getChildCount(); i++) { // Corrected grid name
            View child = gridGame2Answers.getChildAt(i); // Corrected grid name
            child.setClickable(false);
            // Optional: Change alpha or appearance
        }
    }

    // Helper to enable/disable game controls (like buttons, but not answer cards here)
    private void setGameControlsEnabled(boolean enabled) {
        // Enable/disable buttons explicitly
        buttonGame2Skip.setEnabled(enabled);
        // Play sound button depends on TTS state AND overall enabled state
        buttonGame2PlaySound.setEnabled(enabled && ttsInitialized);

        // Card clicks are enabled/disabled by checkAnswer/disableAnswerButtons
        // This function primarily controls the buttons outside the grid.
        Log.d(TAG, "Game controls enabled: " + enabled);
    }


    // Chuyển câu hỏi tiếp theo
    private void goToNextQuestion() {
        currentQuestionIndex++;
        // Kích hoạt lại nút loa và các nút trả lời cho câu hỏi mới
        // buttonGame2PlaySound.setEnabled(ttsInitialized); // Handled by setGameControlsEnabled in loadQuestion
        // enableAnswerButtons(); // Re-enable cards when loading new question
        loadQuestion(); // Load the new question (handles enabling controls)
    }

    // End the game
    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(ResultActivity.EXTRA_GAME_TYPE, "Game 2: Listen & Match"); // Translated
        startActivity(intent);
        finish(); // Close this activity
    }

    // Method to record learning progress using API call and loading counter
    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized. Cannot record learning progress.");
            return;
        }

        // Start API loading indicator
        startApiCall("Saving progress..."); // Translated loading message

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(CURRENT_USER_ID);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishApiCall(); // Hide dialog when API call finishes

                if (response.isSuccessful()) {
                    Log.i(TAG, "Learning recorded successfully for vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered); // Translated
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.w(TAG, "Failed to record learning for vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message() + " Body: " + errorBody); // Translated
                    // Optional: Toast.makeText(Game2Activity.this, "Failed to record progress.", Toast.LENGTH_SHORT).show(); // Translated
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                finishApiCall(); // Hide dialog when API call finishes
                Log.e(TAG, "Network error recording learning for vocab ID: " + vocabularyId, t); // Translated
                // Optional: Toast.makeText(Game2Activity.this, "Network error saving progress.", Toast.LENGTH_SHORT).show(); // Translated
            }
        });
    }

    // --- Loading Logic using DialogFragment ---
    private synchronized void startApiCall() {
        startApiCall(null); // Default message
    }

    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading..."; // Translated default message
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }

    // --- Override onBackPressed() ---
    @Override
    public void onBackPressed() {
        // Show confirmation dialog if game is in progress
        if (vocabularyList != null && currentQuestionIndex < vocabularyList.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Game?") // English Title
                    .setMessage("Are you sure you want to leave the game? Your progress for this round will not be saved.") // English Message
                    .setPositiveButton("Leave", (dialog, which) -> { // English Positive Button
                        // User clicked Yes, finish the activity
                        Game2Activity.super.onBackPressed(); // Call the default back press behavior
                    })
                    .setNegativeButton("Stay", (dialog, which) -> { // English Negative Button
                        // User clicked No, just dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        } else {
            // Game is not in progress (e.g., loading failed or finished), just use default behavior
            super.onBackPressed();
        }
    }
    // --- End Override onBackPressed() ---


    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTS Shutdown.");
        }
        if (soundPool != null) {
            soundPool.release(); // Release SoundPool resources
            soundPool = null;
            Log.d(TAG, "SoundPool Released.");
        }
        // Ensure loading dialog is dismissed if activity is destroyed while loading
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
        super.onDestroy();
    }
}