package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // Using Glide for image loading
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestChoice;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestHistory;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestPart;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestion;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestionContent;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestionGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private TextView tvQuestionIndicator, tvTimer, tvQuestionTextPart5;
    private LinearProgressIndicator progressIndicator;
    private ImageView imgQuestionPart1;
    private View audioPlayerView; // The include layout
    private ImageView btnAudioPlayPause;
    private SeekBar audioSeekBar;
    private TextView tvAudioCurrentTime, tvAudioTotalTime;
    private Button btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD;
    private Button btnNext, btnBack;

    private ApiService apiService;
    private TestPart currentTestPart;
    private List<TestQuestionGroup> questionGroups;
    private List<TestQuestion> allQuestions = new ArrayList<>(); // Flattened list for easier access by index
    private int currentQuestionIndex = 0;
    private int partNumber; // 1 or 5
    private int testId;
    private int partId;
    private long historyId = -1; // Store the ID from startTest API

    private Map<Integer, Integer> userAnswers = new HashMap<>(); // <QuestionID, ChoiceID>
    private Integer selectedChoiceId = null; // Currently selected choice for the visible question

    private MediaPlayer mediaPlayer;
    private Handler audioHandler = new Handler();
    private boolean isAudioPrepared = false;
    private boolean autoPlayedOnce = false;

    private CountDownTimer countDownTimer;
    private static final long TEST_DURATION_MS = 10 * 60 * 1000; // Example: 10 minutes
    private static final Long USER_ID = 27L;

    private static final String TAG = "TestActivity"; // For logging


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        apiService = ApiClient.getApiService();

        if (!getIntentData()) {
            finishWithError("Invalid test data received.");
            return;
        }

        bindViews();
        setupListeners();
        startNewTest(); // Call API to start history record, then fetch part details
    }

    private boolean getIntentData() {
        Intent intent = getIntent();
        testId = intent.getIntExtra(Constants.EXTRA_TEST_ID, -1);
        partId = intent.getIntExtra(Constants.EXTRA_PART_ID, -1);
        partNumber = intent.getIntExtra(Constants.EXTRA_PART_NUMBER, -1);
        return testId != -1 && partId != -1 && partNumber != -1;
    }

    private void bindViews() {
        tvQuestionIndicator = findViewById(R.id.question_indicator_textview);
        tvTimer = findViewById(R.id.timer_indicator_textview);
        progressIndicator = findViewById(R.id.question_progress_indicator);
        imgQuestionPart1 = findViewById(R.id.img_question_part1);
        tvQuestionTextPart5 = findViewById(R.id.question_textview_part5);
        audioPlayerView = findViewById(R.id.audio_player_part1); // The included layout view
        btnChoiceA = findViewById(R.id.btn_choice_a);
        btnChoiceB = findViewById(R.id.btn_choice_b);
        btnChoiceC = findViewById(R.id.btn_choice_c);
        btnChoiceD = findViewById(R.id.btn_choice_d);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);

        // Views within the audio player include
        btnAudioPlayPause = audioPlayerView.findViewById(R.id.audio_btn_play_pause);
        audioSeekBar = audioPlayerView.findViewById(R.id.audio_seekbar_progress);
        tvAudioCurrentTime = audioPlayerView.findViewById(R.id.audio_txt_current_time);
        tvAudioTotalTime = audioPlayerView.findViewById(R.id.audio_txt_total_time);

    }

    private void setupListeners() {
        btnChoiceA.setOnClickListener(this);
        btnChoiceB.setOnClickListener(this);
        btnChoiceC.setOnClickListener(this);
        btnChoiceD.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        btnAudioPlayPause.setOnClickListener(this);
        audioSeekBar.setOnSeekBarChangeListener(this);
    }

    private void startNewTest() {
        // Replace "DUMMY_USER_ID" with actual user ID retrieval
        Long userId = USER_ID;
        if (userId == null) {
            finishWithError("User not logged in.");
            return;
        }

        StartTestRequest request = new StartTestRequest(userId, testId, partId);
        apiService.startTest(request).enqueue(new Callback<StartTestResponse>() {
            @Override
            public void onResponse(Call<StartTestResponse> call, Response<StartTestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyId = response.body().getHistoryId();
                    Log.d(TAG, "Test started successfully. History ID: " + historyId);
                    // Now fetch the actual test part data
                    fetchTestPartDetails();
                } else {
                    finishWithError("Failed to start test session: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StartTestResponse> call, Throwable t) {
                finishWithError("Network error starting test: " + t.getMessage());
            }
        });
    }


    private void fetchTestPartDetails() {
        // Add a loading indicator here
        apiService.getTestPartDetails(testId, partId).enqueue(new Callback<TestPart>() {
            @Override
            public void onResponse(Call<TestPart> call, Response<TestPart> response) {
                // Hide loading indicator
                if (response.isSuccessful() && response.body() != null) {
                    currentTestPart = response.body();
                    prepareTestData();
                    if (!allQuestions.isEmpty()) {
                        loadQuestion(currentQuestionIndex);
                        startTimer(TEST_DURATION_MS); // Start the overall test timer
                    } else {
                        finishWithError("No questions found in this part.");
                    }
                } else {
                    finishWithError("Failed to load test details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TestPart> call, Throwable t) {
                // Hide loading indicator
                finishWithError("Network error loading test details: " + t.getMessage());
            }
        });
    }

    private void prepareTestData() {
        if (currentTestPart == null || currentTestPart.getGroups() == null) {
            return;
        }
        questionGroups = currentTestPart.getGroups();
        // Sort groups by groupIndex just in case
        Collections.sort(questionGroups, Comparator.comparing(TestQuestionGroup::getGroupIndex, Comparator.nullsLast(Comparator.naturalOrder())));

        allQuestions.clear();
        for (TestQuestionGroup group : questionGroups) {
            if (group.getQuestions() != null) {
                // Sort questions within the group by questionNumber
                Collections.sort(group.getQuestions(), Comparator.comparing(TestQuestion::getQuestionNumber, Comparator.nullsLast(Comparator.naturalOrder())));
                allQuestions.addAll(group.getQuestions());
            }
        }
        progressIndicator.setMax(allQuestions.size());
    }

    private void loadQuestion(int index) {
        if (index < 0 || index >= allQuestions.size()) {
            Log.e(TAG, "Invalid question index: " + index);
            return;
        }
        currentQuestionIndex = index;
        TestQuestion question = allQuestions.get(index);
        resetUIForNewQuestion();

        // Update progress
        tvQuestionIndicator.setText(String.format(Locale.getDefault(), "Question %d/%d", index + 1, allQuestions.size()));
        progressIndicator.setProgressCompat(index + 1, true);

        // Restore previously selected answer for this question
        selectedChoiceId = userAnswers.get(question.getQuestionId());


        // --- Part Specific Loading ---
        if (partNumber == 1) {
            loadPart1Question(question);
        } else if (partNumber == 5) {
            loadPart5Question(question);
        }

        // Update button states based on selectedChoiceId
        updateChoiceButtonStates();
        updateNavigationButtons();
    }

    private void resetUIForNewQuestion(){
        // Reset common elements
        selectedChoiceId = null;
        resetChoiceButtonBackgrounds();
        stopAudio(); // Stop audio from previous question if any
        isAudioPrepared = false;
        autoPlayedOnce = false;
        imgQuestionPart1.setVisibility(View.GONE);
        audioPlayerView.setVisibility(View.GONE);
        tvQuestionTextPart5.setVisibility(View.GONE);

        // Reset audio player UI
        btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px);
        audioSeekBar.setProgress(0);
        tvAudioCurrentTime.setText("0:00");
        tvAudioTotalTime.setText("0:00");
        audioSeekBar.setEnabled(false); // Disable until prepared
    }

    private void loadPart1Question(TestQuestion question) {
        TestQuestionGroup group = findGroupForQuestion(question);
        if (group == null || group.getContents() == null) {
            Log.e(TAG, "Could not find group or content for question ID: " + question.getQuestionId());
            return; // Or show error placeholder
        }

        String imageUrl = null;
        String audioUrl = null;

        // Find image and audio URLs from group content
        for (TestQuestionContent content : group.getContents()) {
            if ("IMAGE".equalsIgnoreCase(content.getContentType())) {
                imageUrl = content.getContentData();
            } else if ("AUDIO".equalsIgnoreCase(content.getContentType())) {
                audioUrl = content.getContentData();
            }
        }

        // Load Image
        if (imageUrl != null) {
            imgQuestionPart1.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.color.gray) // Placeholder color/drawable
                    .error(R.drawable.ic_xml_broken_image_24px) // Error image
                    .into(imgQuestionPart1);
        } else {
            imgQuestionPart1.setVisibility(View.GONE); // Or show placeholder
        }

        // Load Audio
        if (audioUrl != null) {
            audioPlayerView.setVisibility(View.VISIBLE);
            prepareAudio(audioUrl);
        } else {
            audioPlayerView.setVisibility(View.GONE);
        }

        // Load Choices (Using choiceExplanation for Part 1)
        loadChoices(question, true);
    }

    private void loadPart5Question(TestQuestion question) {
        imgQuestionPart1.setVisibility(View.GONE);
        audioPlayerView.setVisibility(View.GONE);
        tvQuestionTextPart5.setVisibility(View.VISIBLE);
        tvQuestionTextPart5.setText(question.getQuestionText() != null ? question.getQuestionText() : "Question text not available.");

        // Load Choices (Using choiceText for Part 5)
        loadChoices(question, false);
    }

    // Helper to find the group a question belongs to
    private TestQuestionGroup findGroupForQuestion(TestQuestion question) {
        // REMOVE this line: if (question.getGroup() != null) return question.getGroup(); // If relationship is loaded

        // Keep the fallback loop:
        if (questionGroups == null) { // Add a null check for safety
            Log.e(TAG, "questionGroups list is null in findGroupForQuestion");
            return null;
        }

        for (TestQuestionGroup group : questionGroups) {
            if (group.getQuestions() != null) {
                for (TestQuestion q : group.getQuestions()) {
                    // Make sure to compare IDs safely
                    if (q.getQuestionId() != null && q.getQuestionId().equals(question.getQuestionId())) {
                        return group; // Found the group containing this question
                    }
                }
            }
        }
        Log.w(TAG, "Could not find parent group for Question ID: " + (question.getQuestionId() != null ? question.getQuestionId() : "null"));
        return null; // Group not found
    }

    private void loadChoices(TestQuestion question, boolean useExplanation) {
        List<TestChoice> choices = question.getChoices();
        if (choices == null || choices.size() < 4) {
            Log.e(TAG, "Insufficient choices for question ID: " + question.getQuestionId());
            // Hide buttons or show error
            btnChoiceA.setVisibility(View.GONE);
            btnChoiceB.setVisibility(View.GONE);
            btnChoiceC.setVisibility(View.GONE);
            btnChoiceD.setVisibility(View.GONE);
            return;
        }

        // Sort choices by label (A, B, C, D)
        Collections.sort(choices, Comparator.comparing(TestChoice::getChoiceLabel, Comparator.nullsLast(Comparator.naturalOrder())));

        // Assume choices are ordered A, B, C, D after sorting
        if (choices.size() > 0) setupChoiceButton(btnChoiceA, choices.get(0), useExplanation);
        if (choices.size() > 1) setupChoiceButton(btnChoiceB, choices.get(1), useExplanation);
        if (choices.size() > 2) setupChoiceButton(btnChoiceC, choices.get(2), useExplanation);
        if (choices.size() > 3) setupChoiceButton(btnChoiceD, choices.get(3), useExplanation);

        // Hide unused buttons if fewer than 4 choices (optional)
        btnChoiceA.setVisibility(choices.size() > 0 ? View.VISIBLE : View.GONE);
        btnChoiceB.setVisibility(choices.size() > 1 ? View.VISIBLE : View.GONE);
        btnChoiceC.setVisibility(choices.size() > 2 ? View.VISIBLE : View.GONE);
        btnChoiceD.setVisibility(choices.size() > 3 ? View.VISIBLE : View.GONE);
    }

    private void setupChoiceButton(Button button, TestChoice choice, boolean useExplanation) {
        String textToShow = useExplanation ? choice.getChoiceExplanation() : choice.getChoiceText();
        button.setText(String.format("(%s) %s", choice.getChoiceLabel(), textToShow != null ? textToShow : ""));
        button.setTag(choice.getChoiceId()); // Store choice ID in the button's tag
        button.setEnabled(true);
    }

    private void updateChoiceButtonStates() {
        resetChoiceButtonBackgrounds();
        int selectedColor = ContextCompat.getColor(this, R.color.blue); // Your selection color
        ColorStateList selectedTint = ColorStateList.valueOf(selectedColor);

        if (selectedChoiceId != null) {
            if (btnChoiceA.getTag() != null && selectedChoiceId.equals(btnChoiceA.getTag())) btnChoiceA.setBackgroundTintList(selectedTint);
            if (btnChoiceB.getTag() != null && selectedChoiceId.equals(btnChoiceB.getTag())) btnChoiceB.setBackgroundTintList(selectedTint);
            if (btnChoiceC.getTag() != null && selectedChoiceId.equals(btnChoiceC.getTag())) btnChoiceC.setBackgroundTintList(selectedTint);
            if (btnChoiceD.getTag() != null && selectedChoiceId.equals(btnChoiceD.getTag())) btnChoiceD.setBackgroundTintList(selectedTint);
        }
    }


    private void resetChoiceButtonBackgrounds() {
        int defaultColor = ContextCompat.getColor(this, R.color.gray);
        ColorStateList defaultTint = ColorStateList.valueOf(defaultColor);
        btnChoiceA.setBackgroundTintList(defaultTint);
        btnChoiceB.setBackgroundTintList(defaultTint);
        btnChoiceC.setBackgroundTintList(defaultTint);
        btnChoiceD.setBackgroundTintList(defaultTint);
    }

    private void updateNavigationButtons() {
        btnBack.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setText(currentQuestionIndex == allQuestions.size() - 1 ? "Finish" : "Next");
    }

    private void submitAnswerToServer(int questionId, int choiceId) {
        if (historyId == -1) {
            Log.e(TAG, "Cannot submit answer, invalid historyId.");
            // Maybe show a toast or retry starting the test
            return;
        }
        SubmitAnswerRequest request = new SubmitAnswerRequest(historyId, questionId, choiceId);
        apiService.submitAnswer(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Answer submitted successfully for QID: " + questionId);
                } else {
                    Log.e(TAG, "Failed to submit answer for QID: " + questionId + " - Code: " + response.code() + " Msg: "+response.message());
                    // Handle error: Maybe show a message or implement retry logic
                    Toast.makeText(TestActivity.this, "Failed to save answer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error submitting answer for QID: " + questionId, t);
                Toast.makeText(TestActivity.this, "Network error saving answer", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void finishTest() {
        stopTimer();
        stopAudio();
        // Disable UI
        disableTestInteraction();

        if (historyId == -1) {
            finishWithError("Cannot finish test, invalid session.");
            return;
        }

        Log.d(TAG, "Finishing test. History ID: " + historyId);
        // Call API to mark test as complete
        apiService.completeTest(historyId).enqueue(new Callback<TestHistory>() {
            @Override
            public void onResponse(Call<TestHistory> call, Response<TestHistory> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Test completed successfully on server.");
                    TestHistory resultHistory = response.body();
                    // Navigate to Result Activity
                    Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                    intent.putExtra(Constants.EXTRA_HISTORY_ID, historyId);
                    // Pass score details IF the server calculated and returned them
                    if (resultHistory.getScore() != null) {
                        intent.putExtra(Constants.EXTRA_SCORE, resultHistory.getScore());
                    }
                    if (resultHistory.getTotalQuestions() != null) {
                        intent.putExtra(Constants.EXTRA_TOTAL_QUESTIONS, resultHistory.getTotalQuestions());
                    }
                    if (resultHistory.getCorrectAnswers() != null) {
                        intent.putExtra(Constants.EXTRA_CORRECT_ANSWERS, resultHistory.getCorrectAnswers());
                    }
                    // Pass local calculation as fallback if server doesn't return score
                    else {
                        calculateAndPassLocalScore(intent);
                    }

                    startActivity(intent);
                    finish(); // Finish TestActivity
                } else {
                    Log.e(TAG, "Failed to complete test on server: " + response.code() + " " + response.message());
                    // Handle error - maybe allow retry or show manual result calculation
                    Toast.makeText(TestActivity.this, "Failed to finalize test: " + response.message(), Toast.LENGTH_LONG).show();
                    // As a fallback, calculate score locally and go to results
                    Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                    intent.putExtra(Constants.EXTRA_HISTORY_ID, historyId);
                    calculateAndPassLocalScore(intent);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TestHistory> call, Throwable t) {
                Log.e(TAG, "Network error completing test", t);
                Toast.makeText(TestActivity.this, "Network error finishing test: " + t.getMessage(), Toast.LENGTH_LONG).show();
                // Fallback: Calculate score locally and go to results
                Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                intent.putExtra(Constants.EXTRA_HISTORY_ID, historyId);
                calculateAndPassLocalScore(intent);
                startActivity(intent);
                finish();
            }
        });
    }

    // Fallback method if server doesn't calculate score
    private void calculateAndPassLocalScore(Intent intent) {
        int correctCount = 0;
        for(TestQuestion q : allQuestions) {
            Integer userAnswerChoiceId = userAnswers.get(q.getQuestionId());
            if (userAnswerChoiceId != null) {
                TestChoice correctChoice = findChoiceByLabel(q, q.getCorrectAnswerLabel());
                if (correctChoice != null && userAnswerChoiceId.equals(correctChoice.getChoiceId())) {
                    correctCount++;
                }
            }
        }
        double score = (double) correctCount / allQuestions.size() * 100;
        intent.putExtra(Constants.EXTRA_SCORE, score);
        intent.putExtra(Constants.EXTRA_TOTAL_QUESTIONS, allQuestions.size());
        intent.putExtra(Constants.EXTRA_CORRECT_ANSWERS, correctCount);
    }

    private TestChoice findChoiceByLabel(TestQuestion question, String label) {
        if (question.getChoices() == null || label == null) return null;
        for (TestChoice choice : question.getChoices()) {
            if (label.equalsIgnoreCase(choice.getChoiceLabel())) {
                return choice;
            }
        }
        return null;
    }


    private void disableTestInteraction() {
        btnChoiceA.setEnabled(false);
        btnChoiceB.setEnabled(false);
        btnChoiceC.setEnabled(false);
        btnChoiceD.setEnabled(false);
        btnNext.setEnabled(false);
        btnBack.setEnabled(false);
        // Consider disabling audio controls too
        btnAudioPlayPause.setEnabled(false);
        audioSeekBar.setEnabled(false);
    }


    // --- Timer Logic ---
    private void startTimer(long duration) {
        stopTimer(); // Ensure previous timer is stopped
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                tvTimer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                Toast.makeText(TestActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                finishTest(); // Auto-finish when time runs out
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    // --- Audio Player Logic ---
    private void prepareAudio(String url) {
        stopAudio(); // Release previous player if exists
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer Error: what=" + what + ", extra=" + extra);
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                // Reset UI
                btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px);
                audioSeekBar.setEnabled(false);
                return true; // Indicate we handled the error
            });
            mediaPlayer.prepareAsync(); // Prepare asynchronously
            btnAudioPlayPause.setImageResource(R.drawable.ic_xml_hourglass_top_24px); // Show loading
            audioSeekBar.setEnabled(false);
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + url, e);
            Toast.makeText(this, "Cannot load audio", Toast.LENGTH_SHORT).show();
            audioPlayerView.setVisibility(View.GONE); // Hide player if error
        }
    }

    private void playAudio() {
        if (mediaPlayer != null && isAudioPrepared) {
            mediaPlayer.start();
            btnAudioPlayPause.setImageResource(R.drawable.ic_xml_pause_24px);
            updateAudioProgress();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px);
            audioHandler.removeCallbacks(audioProgressRunnable);
        }
    }

    private void stopAudio() {
        audioHandler.removeCallbacks(audioProgressRunnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isAudioPrepared = false;
    }

    private Runnable audioProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isAudioPrepared && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                audioSeekBar.setProgress(currentPosition);
                tvAudioCurrentTime.setText(formatTime(currentPosition));
                audioHandler.postDelayed(this, 500); // Update every 500ms
            }
        }
    };

    private void updateAudioProgress() {
        audioHandler.post(audioProgressRunnable);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "Audio prepared.");
        isAudioPrepared = true;
        int duration = mp.getDuration();
        audioSeekBar.setMax(duration);
        tvAudioTotalTime.setText(formatTime(duration));
        audioSeekBar.setEnabled(true);
        btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); // Ready to play
        // Auto-play ONCE for Part 1
        if (partNumber == 1 && !autoPlayedOnce) {
            playAudio();
            autoPlayedOnce = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "Audio completed.");
        btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px);
        audioSeekBar.setProgress(0); // Reset progress to start visually
        tvAudioCurrentTime.setText("0:00");
        audioHandler.removeCallbacks(audioProgressRunnable);
        // Optional: Seek to beginning if user wants to replay from start easily
        // if (mediaPlayer != null && isAudioPrepared) {
        //     mediaPlayer.seekTo(0);
        // }
    }


    // --- SeekBar Listener ---
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mediaPlayer != null && isAudioPrepared && fromUser) {
            mediaPlayer.seekTo(progress);
            tvAudioCurrentTime.setText(formatTime(progress));
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Pause playback while seeking? Optional.
        // pauseAudio();
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Resume playback after seeking? Optional.
        // playAudio();
    }

    // --- Click Listener ---
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_choice_a || id == R.id.btn_choice_b || id == R.id.btn_choice_c || id == R.id.btn_choice_d) {
            handleChoiceSelection((Button) v);
        } else if (id == R.id.btn_next) {
            handleNext();
        } else if (id == R.id.btn_back) {
            handleBack();
        } else if (id == R.id.audio_btn_play_pause) {
            toggleAudioPlayback();
        }
    }

    private void handleChoiceSelection(Button selectedButton) {
        if (selectedButton.getTag() == null) return; // Should not happen if setup correctly

        selectedChoiceId = (Integer) selectedButton.getTag();
        TestQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
        userAnswers.put(currentQuestion.getQuestionId(), selectedChoiceId); // Store answer locally

        Log.d(TAG, "User selected Choice ID: " + selectedChoiceId + " for Question ID: " + currentQuestion.getQuestionId());

        updateChoiceButtonStates(); // Update UI highlighting
        submitAnswerToServer(currentQuestion.getQuestionId(), selectedChoiceId); // Send to backend
    }

    private void handleNext() {
        // Optional: Check if an answer was selected before proceeding
        // TestQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
        // if(userAnswers.get(currentQuestion.getQuestionId()) == null) {
        //      Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
        //      return;
        // }

        if (currentQuestionIndex < allQuestions.size() - 1) {
            loadQuestion(currentQuestionIndex + 1);
        } else {
            // This is the finish button
            finishTest();
        }
    }

    private void handleBack() {
        if (currentQuestionIndex > 0) {
            loadQuestion(currentQuestionIndex - 1);
        }
    }

    private void toggleAudioPlayback() {
        if (mediaPlayer != null && isAudioPrepared) {
            if (mediaPlayer.isPlaying()) {
                pauseAudio();
            } else {
                playAudio();
            }
        } else if (partNumber == 1){
            // If not prepared (e.g., error occurred), try preparing again
            TestQuestion question = allQuestions.get(currentQuestionIndex);
            TestQuestionGroup group = findGroupForQuestion(question);
            if (group != null && group.getContents() != null) {
                for (TestQuestionContent content : group.getContents()) {
                    if ("AUDIO".equalsIgnoreCase(content.getContentType())) {
                        prepareAudio(content.getContentData());
                        break;
                    }
                }
            }
        }
    }


    // --- Utility Methods ---
    private String formatTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds);
    }


    private void finishWithError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    // --- Lifecycle Methods ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio(); // Release MediaPlayer resources
        stopTimer(); // Stop countdown timer
        audioHandler.removeCallbacksAndMessages(null); // Clean up handler
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting test
        new AlertDialog.Builder(this)
                .setTitle("Exit Test?")
                .setMessage("Are you sure you want to exit the test? Your progress on the current attempt might be saved, but the attempt will be marked incomplete.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    stopTimer();
                    stopAudio();
                    // Optionally call an API to mark as incomplete/abandoned if needed
                    super.onBackPressed(); // Exit activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}