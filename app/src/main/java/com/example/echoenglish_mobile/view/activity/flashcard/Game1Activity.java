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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayout.LayoutParams;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Game1Activity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Long currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();
    public static final String EXTRA_VOCAB_LIST = "VOCABULARY_LIST";
    private static final String TAG = "Game1Activity";

    private TextView textGame1Progress;
    private ImageView imageGame1Word;
    private TextView textGame1Definition;

    private LinearLayout layoutDefinitionArea;
    private FlexboxLayout layoutAnswer;
    private FlexboxLayout layoutChoices;
    private MaterialButton buttonSkip;
    private MaterialButton buttonCheck;
    private ImageButton buttonGame1PlaySound;

    private List<VocabularyResponse> vocabularyList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String currentCorrectWord;

    private List<TextView> choiceTextViews = new ArrayList<>();

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
        setContentView(R.layout.activity_game1);
        setTitle("Game: Arrange Letters");

        textGame1Progress = findViewById(R.id.textGame1Progress);
        imageGame1Word = findViewById(R.id.imageGame1Word);
        layoutDefinitionArea = findViewById(R.id.layoutDefinitionArea);
        textGame1Definition = findViewById(R.id.textGame1Definition);
        layoutAnswer = findViewById(R.id.layoutGame1Answer);
        layoutChoices = findViewById(R.id.layoutGame1Choices);
        buttonSkip = findViewById(R.id.buttonGame1Skip);
        buttonCheck = findViewById(R.id.buttonGame1Check);
        buttonGame1PlaySound = findViewById(R.id.buttonGame1PlaySound);

        apiService = ApiClient.getApiService();

        try {
            vocabularyList = (ArrayList<VocabularyResponse>) getIntent().getSerializableExtra(EXTRA_VOCAB_LIST);
        } catch (Exception e) {
            Log.e(TAG, "Error receiving vocabulary list", e);
            vocabularyList = null;
        }

        if (vocabularyList == null || vocabularyList.isEmpty()) {
            Log.e(TAG, "No vocabulary list received for game.");
            Toast.makeText(this, "Error loading game data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Collections.shuffle(vocabularyList);

        tts = new TextToSpeech(this, this);
        buttonGame1PlaySound.setEnabled(false);

        loadSounds();

        setGameControlsEnabled(false);

        buttonSkip.setOnClickListener(v -> {
            setGameControlsEnabled(false);
            if (currentQuestionIndex < vocabularyList.size()) {
                long vocabId = vocabularyList.get(currentQuestionIndex).getId();
                recordLearningProgress(vocabId, false);
            }
            goToNextQuestion();
        });
        buttonCheck.setOnClickListener(v -> checkAnswer());
        buttonGame1PlaySound.setOnClickListener(v -> speakWord());
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
                    Log.e(TAG, "TTS Language (US English) not supported");
                    Toast.makeText(this, "TTS language (US English) not supported.", Toast.LENGTH_LONG).show();
                    handleTTSInitializationFailure("Language not supported.");
                } else {
                    ttsInitialized = true;
                    buttonGame1PlaySound.setEnabled(true);
                    Log.d(TAG, "TTS Initialized successfully.");
                    loadQuestion();
                    setGameControlsEnabled(true);
                }
            } else {
                Log.e(TAG, "TTS Initialization failed with status: " + status);
                Toast.makeText(this, "TTS initialization failed.", Toast.LENGTH_LONG).show();
                handleTTSInitializationFailure("Initialization failed.");
            }
        });
    }

    private void handleTTSInitializationFailure(String message) {
        buttonGame1PlaySound.setEnabled(false);
        Log.e(TAG, "TTS functionality disabled due to failure: " + message);
        Toast.makeText(this, "Vocabulary audio function unavailable.", Toast.LENGTH_LONG).show();
    }

    private void loadQuestion() {
        setGameControlsEnabled(true);
        buttonCheck.setVisibility(View.GONE);

        if (currentQuestionIndex >= vocabularyList.size()) {
            endGame();
            return;
        }

        VocabularyResponse currentVocab = vocabularyList.get(currentQuestionIndex);
        currentCorrectWord = currentVocab.getWord().trim().replaceAll("[^a-zA-Z]", "").toUpperCase(Locale.ROOT);

        if (currentCorrectWord.isEmpty()) {
            Log.w(TAG, "Skipping vocab with empty or invalid word: " + currentVocab.getId());
            goToNextQuestion();
            return;
        }

        textGame1Progress.setText(String.format(Locale.getDefault(), "Question %d / %d",
                currentQuestionIndex + 1, vocabularyList.size()));

        if (currentVocab.getImageUrl() != null && !currentVocab.getImageUrl().isEmpty()) {
            imageGame1Word.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentVocab.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(imageGame1Word);
        } else {
            imageGame1Word.setVisibility(View.GONE);
            Glide.with(this).clear(imageGame1Word);
        }

        textGame1Definition.setText("Meaning: " + (currentVocab.getDefinition() != null ? currentVocab.getDefinition() : "Updating..."));

        layoutAnswer.removeAllViews();
        layoutChoices.removeAllViews();
        choiceTextViews.clear();

        for (int i = 0; i < currentCorrectWord.length(); i++) {
            TextView answerBox = createLetterTextView(R.layout.item_game1_letter, ' ');
            answerBox.setBackgroundResource(R.drawable.bg_game_letter_answer);
            layoutAnswer.addView(answerBox);
            setLayoutParamsWithMargin(answerBox, layoutAnswer);
            answerBox.setOnClickListener(v -> returnLetterToChoices((TextView)v));
        }

        List<Character> shuffledChars = shuffleString(currentCorrectWord);
        for (char c : shuffledChars) {
            TextView choiceBox = createLetterTextView(R.layout.item_game1_letter, c);
            choiceBox.setBackgroundResource(R.drawable.bg_game_letter_choice);
            choiceBox.setOnClickListener(v -> selectLetter((TextView)v));
            layoutChoices.addView(choiceBox);
            setLayoutParamsWithMargin(choiceBox, layoutChoices);
            choiceTextViews.add(choiceBox);
        }
    }

    private void speakWord() {
        if (ttsInitialized && currentCorrectWord != null && !currentCorrectWord.isEmpty()) {
            tts.stop();
            tts.speak(currentCorrectWord, TextToSpeech.QUEUE_FLUSH, null, "Speak_" + vocabularyList.get(currentQuestionIndex).getId());
        } else if (!ttsInitialized) {
            Log.w(TAG, "speakWord called but TTS not initialized.");
        }
    }

    private void setLayoutParamsWithMargin(View view, View parent) {
        int marginInDp = 4;
        int marginInPx = (int) (marginInDp * getResources().getDisplayMetrics().density);

        if (parent instanceof FlexboxLayout) {
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            view.setLayoutParams(params);
        }
    }

    private TextView createLetterTextView(int layoutResId, char letter) {
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView textView = (TextView) inflater.inflate(layoutResId, null, false);

        if (letter != ' ') {
            textView.setText(String.valueOf(letter));
            textView.setTag(letter);
        } else {
            textView.setText("");
        }
        return textView;
    }

    private List<Character> shuffleString(String input) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters);
        return characters;
    }

    private void selectLetter(TextView choiceTextView) {
        if (choiceTextView.getVisibility() == View.VISIBLE) {
            TextView targetAnswerBox = null;
            for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
                TextView answerBox = (TextView) layoutAnswer.getChildAt(i);
                if (answerBox.getText().toString().trim().isEmpty()) {
                    targetAnswerBox = answerBox;
                    break;
                }
            }

            if (targetAnswerBox != null) {
                char selectedChar = (char) choiceTextView.getTag();
                targetAnswerBox.setText(String.valueOf(selectedChar));
                targetAnswerBox.setBackgroundResource(R.drawable.bg_game_letter_selected_answer);
                targetAnswerBox.setTag(choiceTextView);
                choiceTextView.setVisibility(View.INVISIBLE);
                checkIfAnswerComplete();
            }
        }
    }

    private void returnLetterToChoices(TextView answerBox) {
        Object tag = answerBox.getTag();
        if (tag instanceof TextView && !answerBox.getText().toString().trim().isEmpty()) {
            TextView originalChoiceBox = (TextView) tag;
            originalChoiceBox.setVisibility(View.VISIBLE);
            answerBox.setText("");
            answerBox.setBackgroundResource(R.drawable.bg_game_letter_answer);
            answerBox.setTag(null);
            buttonCheck.setVisibility(View.GONE);
        }
    }

    private void checkIfAnswerComplete() {
        boolean complete = true;
        for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
            TextView tv = (TextView) layoutAnswer.getChildAt(i);
            if (tv.getText().toString().trim().isEmpty()) {
                complete = false;
                break;
            }
        }
        if (complete) {
            buttonCheck.setVisibility(View.VISIBLE);
        } else {
            buttonCheck.setVisibility(View.GONE);
        }
    }

    private void checkAnswer() {
        StringBuilder userAnswerBuilder = new StringBuilder();
        for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
            TextView tv = (TextView) layoutAnswer.getChildAt(i);
            userAnswerBuilder.append(tv.getText().toString());
        }
        String userAnswer = userAnswerBuilder.toString().toUpperCase(Locale.ROOT);

        boolean isCorrect = userAnswer.equals(currentCorrectWord);

        disableInteraction();

        long currentVocabId = vocabularyList.get(currentQuestionIndex).getId();

        if (soundPool != null) {
            if (isCorrect) {
                if (soundCorrect != 0) soundPool.play(soundCorrect, 1, 1, 0, 0, 1);
            } else {
                if (soundIncorrect != 0) soundPool.play(soundIncorrect, 1, 1, 0, 0, 1);
            }
        }

        if (isCorrect) {
            score++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            recordLearningProgress(currentVocabId, true);
        } else {
            Toast.makeText(this, "Incorrect! Answer: " + currentCorrectWord, Toast.LENGTH_LONG).show();
            recordLearningProgress(currentVocabId, false);
        }

        int feedbackColor = ContextCompat.getColor(this, isCorrect ? R.color.correct_green : R.color.incorrect_red);
        for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
            TextView tv = (TextView) layoutAnswer.getChildAt(i);
            tv.setBackgroundColor(feedbackColor);
            tv.setClickable(false);
        }

        mainHandler.postDelayed(this::goToNextQuestion, isCorrect ? 1500 : 2500);
    }

    private void setGameControlsEnabled(boolean enabled) {
        int correctColor = ContextCompat.getColor(this, R.color.correct_green);
        int incorrectColor = ContextCompat.getColor(this, R.color.incorrect_red);

        for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
            View child = layoutAnswer.getChildAt(i);
            boolean hasFeedbackColor = false;
            if (child.getBackground() instanceof android.graphics.drawable.ColorDrawable) {
                int color = ((android.graphics.drawable.ColorDrawable) child.getBackground()).getColor();
                if (color == correctColor || color == incorrectColor) {
                    hasFeedbackColor = true;
                }
            }
            child.setClickable(enabled && !hasFeedbackColor);
        }

        for (TextView tv : choiceTextViews) {
            if (tv.getVisibility() == View.VISIBLE) {
                tv.setClickable(enabled);
            } else {
                tv.setClickable(false);
            }
        }
        buttonCheck.setEnabled(enabled);
        buttonSkip.setEnabled(enabled);
        buttonGame1PlaySound.setEnabled(enabled && ttsInitialized);
        Log.d(TAG, "Game controls enabled: " + enabled);
    }

    private void disableInteraction() {
        for (int i = 0; i < layoutAnswer.getChildCount(); i++) {
            layoutAnswer.getChildAt(i).setClickable(false);
        }
        for (TextView tv : choiceTextViews) {
            tv.setClickable(false);
        }
        buttonCheck.setEnabled(false);
        buttonSkip.setEnabled(false);
        buttonGame1PlaySound.setEnabled(false);
        Log.d(TAG, "Interaction disabled.");
    }


    private void goToNextQuestion() {
        currentQuestionIndex++;
        loadQuestion();
    }

    private void endGame() {
        Log.d(TAG, "Game Ended. Score: " + score + "/" + vocabularyList.size());
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, vocabularyList.size());
        intent.putExtra(ResultActivity.EXTRA_GAME_TYPE, "Game 1: Arrange Letters");
        startActivity(intent);
        finish();
    }

    private void recordLearningProgress(long vocabularyId, boolean isRemembered) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized. Cannot record learning progress.");
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
                    .setMessage("Are you sure you want to leave the game?")
                    .setPositiveButton("Leave", (dialog, which) -> {
                        Game1Activity.super.onBackPressed();
                    })
                    .setNegativeButton("Stay", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
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