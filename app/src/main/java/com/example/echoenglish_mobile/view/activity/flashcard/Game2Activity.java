package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Game2Activity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private Long currentUserId;

    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game2Activity";

    private ConstraintLayout gameContent;

    private TextView textGame2Progress;
    private ImageButton buttonGame2PlaySound;
    private TextView textViewListenInstruction;
    private GridLayout gridGame2Answers;
    private MaterialButton buttonGame2Skip;

    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private VocabularyResponse currentCorrectVocab;
    private List<VocabularyResponse> currentOptions = new ArrayList<>();

    private TextToSpeech tts;
    private boolean ttsInitialized = false;
    private SoundPool soundPool;
    private int soundCorrect;
    private int soundIncorrect;

    private ApiService apiService;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
        setTitle("Game: Listen & Match Image");

        currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();
        if (currentUserId == null) {
            Log.e(TAG, "User ID is null. Cannot proceed with game.");
            Toast.makeText(this, "Error: User not logged in or ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gameContent = findViewById(R.id.gameContent);

        textGame2Progress = findViewById(R.id.textGame2Progress);
        buttonGame2PlaySound = findViewById(R.id.buttonGame2PlaySound);
        textViewListenInstruction = findViewById(R.id.textViewListenInstruction);
        gridGame2Answers = findViewById(R.id.gridGame2Answers);
        buttonGame2Skip = findViewById(R.id.buttonGame2Skip);

        apiService = ApiClient.getApiService();

        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        if (vocabularyList == null || vocabularyList.size() < 4) {
            Log.e(TAG, "Not enough vocabularies received for game (need at least 4).");
            Toast.makeText(this, "Not enough vocabularies for this game.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Collections.shuffle(vocabularyList);

        tts = new TextToSpeech(this, this);
        buttonGame2PlaySound.setEnabled(false);

        loadSounds();

        setGameControlsEnabled(false);

        buttonGame2PlaySound.setOnClickListener(v -> speakWord());
        buttonGame2Skip.setOnClickListener(v -> {
            setGameControlsEnabled(false);
            if (currentQuestionIndex < vocabularyList.size()) {
                long vocabId = vocabularyList.get(currentQuestionIndex).getId();
                recordLearningProgress(vocabId, false);
            }
            goToNextQuestion();
        });
    }

    private void loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        soundCorrect = soundPool.load(this, R.raw.correct_answer, 1);
        soundIncorrect = soundPool.load(this, R.raw.wrong_answer, 1);

        Log.d(TAG, "SoundPool initialized and loading sounds. Correct ID: " + soundCorrect + ", Incorrect ID: " + soundIncorrect);
    }


    @Override
    public void onInit(int status) {
        runOnUiThread(() -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS Language not supported");
                    Toast.makeText(this, "TTS language (US English) not supported. Audio unavailable.", Toast.LENGTH_LONG).show();
                    handleTTSInitializationFailure("Language not supported.");
                } else {
                    ttsInitialized = true;
                    Log.d(TAG, "TTS Initialized successfully.");
                    loadQuestion();
                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status: " + status);
                Toast.makeText(this, "TTS initialization failed. Audio unavailable.", Toast.LENGTH_LONG).show();
                handleTTSInitializationFailure("Initialization failed.");
            }
        });
    }

    private void handleTTSInitializationFailure(String message) {
        ttsInitialized = false;
        buttonGame2PlaySound.setEnabled(false);
        Log.e(TAG, "TTS functionality disabled due to failure: " + message);
    }


    private void loadQuestion() {
        setGameControlsEnabled(true);

        if (currentQuestionIndex >= vocabularyList.size()) {
            endGame();
            return;
        }

        currentCorrectVocab = vocabularyList.get(currentQuestionIndex);
        currentOptions.clear();
        gridGame2Answers.removeAllViews();

        textGame2Progress.setText(String.format(Locale.getDefault(), "Question %d / %d",
                currentQuestionIndex + 1, vocabularyList.size()));

        currentOptions.add(currentCorrectVocab);

        List<VocabularyResponse> wrongOptionsPool = new ArrayList<>(vocabularyList);

        boolean removed = false;
        for(int i = 0; i < wrongOptionsPool.size(); i++) {
            if (wrongOptionsPool.get(i) != null && currentCorrectVocab != null &&
                    wrongOptionsPool.get(i).getId() != null && currentCorrectVocab.getId() != null &&
                    wrongOptionsPool.get(i).getId().equals(currentCorrectVocab.getId())) {
                wrongOptionsPool.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed && vocabularyList.contains(currentCorrectVocab)) {
            Log.w(TAG, "Could not remove currentCorrectVocab from wrongOptionsPool by ID: " + (currentCorrectVocab != null ? currentCorrectVocab.getId() : "null"));
        }

        Collections.shuffle(wrongOptionsPool);

        int neededWrong = 3;
        int addedWrong = 0;
        for (int i = 0; i < wrongOptionsPool.size() && addedWrong < neededWrong; i++) {
            VocabularyResponse wrongOption = wrongOptionsPool.get(i);
            if (wrongOption == null || wrongOption.getId() == null) {
                Log.w(TAG, "Skipping null or invalid wrong option from pool.");
                continue;
            }

            boolean alreadyAdded = false;
            for (VocabularyResponse option : currentOptions) {
                if (option != null && option.getId() != null && option.getId().equals(wrongOption.getId())) {
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
            Toast.makeText(this, "Error generating options. Not enough unique vocabularies.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Collections.shuffle(currentOptions);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyResponse option : currentOptions) {
            if (option == null) {
                Log.w(TAG, "Skipping null option when inflating cards.");
                continue;
            }

            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_game2_answer, gridGame2Answers, false);

            ImageView imageView = cardView.findViewById(R.id.imageGame2Answer);
            TextView textView = cardView.findViewById(R.id.textGame2AnswerDefinition);

            textView.setText(option.getDefinition());
            String imageUrl = option.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(imageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.ic_placeholder_image)
                        .into(imageView);
                Log.w(TAG, "Image URL is null or empty for vocab ID: " + option.getId());
            }

            cardView.setTag(option);

            cardView.setOnClickListener(v -> {
                VocabularyResponse selected = (VocabularyResponse) v.getTag();
                if (selected != null) {
                    checkAnswer(selected, (CardView) v);
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

            gridGame2Answers.addView(cardView);
        }

        speakWord();
    }

    private void speakWord() {
        if (ttsInitialized && currentCorrectVocab != null) {
            String wordToSpeak = currentCorrectVocab.getWord();
            if (wordToSpeak != null && !wordToSpeak.isEmpty()) {
                tts.stop();
                tts.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "Speak_" + currentCorrectVocab.getId());
                Log.d(TAG, "Speaking: " + wordToSpeak);
            } else {
                Log.w(TAG, "speakWord: currentCorrectVocab or its word is null/empty for vocab ID: " + (currentCorrectVocab != null ? currentCorrectVocab.getId() : "null"));
            }
        } else if (!ttsInitialized) {
            Log.w(TAG, "speakWord called but TTS not initialized.");
        }
    }

    private void highlightCorrectAnswer() {
        for (int i = 0; i < gridGame2Answers.getChildCount(); i++) {
            View child = gridGame2Answers.getChildAt(i);
            if (child instanceof MaterialCardView) {
                MaterialCardView cardView = (MaterialCardView) child;
                VocabularyResponse option = (VocabularyResponse) cardView.getTag();
                if (option != null && currentCorrectVocab != null &&
                        option.getId() != null && currentCorrectVocab.getId() != null &&
                        option.getId().equals(currentCorrectVocab.getId())) {
                    cardView.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                    cardView.setStrokeColor(ContextCompat.getColor(this, R.color.highlight_blue));
                    Log.d(TAG, "Highlighted correct answer for vocab ID: " + currentCorrectVocab.getId());
                    break;
                }
            } else {
                Log.w(TAG, "Child view in grid is not a MaterialCardView.");
            }
        }
    }

    private void checkAnswer(VocabularyResponse selectedOption, CardView selectedCardView) {
        setGameControlsEnabled(false); // Disable controls during feedback delay

        boolean isCorrect = selectedOption != null && currentCorrectVocab != null &&
                selectedOption.getId() != null && currentCorrectVocab.getId() != null &&
                selectedOption.getId().equals(currentCorrectVocab.getId());

        if (soundPool != null) {
            if (isCorrect) {
                if (soundCorrect != 0) soundPool.play(soundCorrect, 1, 1, 0, 0, 1);
                Log.d(TAG, "Playing correct sound.");
            } else {
                if (soundIncorrect != 0) soundPool.play(soundIncorrect, 1, 1, 0, 0, 1);
                Log.d(TAG, "Playing incorrect sound.");
            }
        } else {
            Log.w(TAG, "SoundPool is null, cannot play feedback sound.");
        }

        if (selectedCardView instanceof MaterialCardView) {
            MaterialCardView mcv = (MaterialCardView) selectedCardView;
            mcv.setCardBackgroundColor(ContextCompat.getColor(this, isCorrect ? R.color.correct_green : R.color.incorrect_red));
            mcv.setStrokeWidth(0);
            mcv.setStrokeColor(ContextCompat.getColor(this, android.R.color.transparent));
        } else {
            selectedCardView.setBackgroundColor(ContextCompat.getColor(this, isCorrect ? R.color.correct_green : R.color.incorrect_red));
        }

        Toast.makeText(this, isCorrect ? "Correct!" : "Incorrect!", Toast.LENGTH_SHORT).show();

        if (!isCorrect) {
            highlightCorrectAnswer();
        }

        long currentVocabId = currentCorrectVocab != null && currentCorrectVocab.getId() != null ? currentCorrectVocab.getId() : -1L;
        if (currentVocabId != -1L) {
            recordLearningProgress(currentVocabId, isCorrect);
        } else {
            Log.e(TAG, "currentCorrectVocab or its ID is null, cannot record learning progress.");
        }

        mainHandler.postDelayed(this::goToNextQuestion, 1500);
    }

    private void setGameControlsEnabled(boolean enabled) {
        for (int i = 0; i < gridGame2Answers.getChildCount(); i++) {
            View child = gridGame2Answers.getChildAt(i);
            child.setClickable(enabled);
            // Removed child.setAlpha(enabled ? 1.0f : 0.7f);
        }

        buttonGame2PlaySound.setEnabled(enabled && ttsInitialized);

        buttonGame2Skip.setEnabled(enabled);

        Log.d(TAG, "Game controls enabled: " + enabled);
    }


    private void goToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < vocabularyList.size()) {
            loadQuestion();
        } else {
            endGame();
        }
    }

    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(ResultActivity.EXTRA_GAME_TYPE, "Game 2: Listen & Match");
        startActivity(intent);
        finish();
    }

    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (currentUserId == null) {
            Log.e(TAG, "currentUserId is null in recordLearningProgress. Cannot record.");
            return;
        }
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized. Cannot record learning progress.");
            return;
        }
        if (vocabularyId == -1L) {
            Log.e(TAG, "Invalid vocabularyId (-1) passed to recordLearningProgress. Cannot record.");
            return;
        }

        LearningRecordRequest request = new LearningRecordRequest();
        request.setUserId(currentUserId);
        request.setVocabularyId(vocabularyId);
        request.setIsRemembered(isRemembered);

        apiService.recordLearning(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Learning recorded successfully for vocab ID: " + vocabularyId + ", isRemembered: " + isRemembered);
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.w(TAG, "Failed to record learning for vocab ID: " + vocabularyId + ": " + response.code() + " - " + response.message() + " Body: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error recording learning for vocab ID: " + vocabularyId, t);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (vocabularyList != null && currentQuestionIndex < vocabularyList.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Game?")
                    .setMessage("Are you sure you want to leave the game? Your progress for this round will not be saved.")
                    .setPositiveButton("Leave", (dialog, which) -> {
                        mainHandler.removeCallbacksAndMessages(null);
                        Game2Activity.super.onBackPressed();
                    })
                    .setNegativeButton("Stay", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            mainHandler.removeCallbacksAndMessages(null);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTS Shutdown.");
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            Log.d(TAG, "SoundPool Released.");
        }
        super.onDestroy();
    }
}