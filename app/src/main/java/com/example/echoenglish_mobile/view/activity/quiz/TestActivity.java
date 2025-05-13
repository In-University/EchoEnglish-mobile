package com.example.echoenglish_mobile.view.activity.quiz; // Thay package phù hợp

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer; // Import CountDownTimer
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Gravity; // Import Gravity
import android.view.LayoutInflater; // Import LayoutInflater
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;

// SỬA LẠI IMPORT MODEL VỀ ĐÚNG PACKAGE CỦA BẠN
// Bỏ các import không cần thiết (request/response/history)
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.ResultActivity;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestChoice;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestPart;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestion;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestionContent;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestionGroup;
import com.google.android.material.bottomsheet.BottomSheetDialog; // Import BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator;


import java.io.IOException;
import java.util.ArrayList;
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

    // Views
    private TextView tvQuestionIndicator, tvQuestionTextPart5;
    private LinearProgressIndicator progressIndicator;
    private ImageView imgQuestionPart1;
    private View audioPlayerView;
    private ImageView btnAudioPlayPause;
    private SeekBar audioSeekBar;
    private TextView tvAudioCurrentTime, tvAudioTotalTime;
    private Button btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD;
    private Button btnNext, btnBack, btnShowExplanation;
    // private CardView cardExplanation; // Bỏ CardView explanation
    private ProgressBar loadingProgressBar; // Cần thêm vào layout nếu muốn dùng
    private TextView tvTimer; // TextView hiển thị timer tổng

    // Data & State
    private ApiService apiService;
    private TestPart currentTestPart;
    private List<TestQuestionGroup> questionGroups;
    private List<TestQuestion> allQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int partNumber;
    private Integer currentTestId; // Lấy từ Intent
    private Integer currentPartId = null; // Lấy từ currentTestPart
    private int totalQuestionsInPart = 0; // Tổng số câu hỏi trong part này

    // Answer Tracking
    private Map<Integer, Integer> userAnswers = new HashMap<>(); // <QuestionID, ChoiceID>
    private Map<Integer, Boolean> answerCorrectness = new HashMap<>(); // <QuestionID, IsCorrect>

    // Media Player
    private MediaPlayer mediaPlayer;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    private boolean isAudioPrepared = false;
    private boolean autoPlayedOnce = false;

    // Timer cho cả bài thi
    private CountDownTimer totalTestTimer; // Timer tổng
    private long totalTimeMillis = 0; // Tổng thời gian (ms)
    private long millisRemaining = 0; // <-- THÊM BIẾN NÀY

    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        apiService = ApiClient.getApiService();

        if (!getIntentData()) {
            finishWithError("Invalid Test ID or Part Number received.");
            return;
        }

        bindViews();
        setupListeners();
        fetchTestData();
    }

    // --- Timer Logic cho Toàn Bài Thi ---
    private void calculateAndStartTotalTimer() {
        if (totalQuestionsInPart <= 0) { Log.w(TAG, "Cannot start timer, no questions."); return; }
        totalTimeMillis = (long) totalQuestionsInPart * 60 * 1000;
        startTotalTestTimer(totalTimeMillis);
    }


    private boolean getIntentData() {
        Intent intent = getIntent();
        partNumber = intent.getIntExtra(Constants.EXTRA_PART_NUMBER, -1);
        currentTestId = intent.getIntExtra(Constants.EXTRA_TEST_ID, -1);
        Log.d(TAG, "Received partNumber: " + partNumber + ", testId: " + currentTestId);
        return partNumber != -1 && currentTestId != -1;
    }

    private void bindViews() {
        tvQuestionIndicator = findViewById(R.id.question_indicator_textview);
        progressIndicator = findViewById(R.id.question_progress_indicator);
        imgQuestionPart1 = findViewById(R.id.img_question_part1);
        tvQuestionTextPart5 = findViewById(R.id.question_textview_part5);
        audioPlayerView = findViewById(R.id.audio_player_part1);
        btnChoiceA = findViewById(R.id.btn_choice_a);
        btnChoiceB = findViewById(R.id.btn_choice_b);
        btnChoiceC = findViewById(R.id.btn_choice_c);
        btnChoiceD = findViewById(R.id.btn_choice_d);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);
        btnShowExplanation = findViewById(R.id.btn_show_explanation);
        // cardExplanation = findViewById(R.id.card_explanation); // Bỏ dòng này
        // tvExplanationDetail = findViewById(R.id.tv_explanation_detail); // Bỏ dòng này
        tvTimer = findViewById(R.id.timer_textview);

        // Audio Player Views
        btnAudioPlayPause = audioPlayerView.findViewById(R.id.audio_btn_play_pause);
        audioSeekBar = audioPlayerView.findViewById(R.id.audio_seekbar_progress);
        tvAudioCurrentTime = audioPlayerView.findViewById(R.id.audio_txt_current_time);
        tvAudioTotalTime = audioPlayerView.findViewById(R.id.audio_txt_total_time);

        // loadingProgressBar = findViewById(R.id.loading_progress_bar); // Uncomment nếu có
    }

    private void setupListeners() {
        btnChoiceA.setOnClickListener(this);
        btnChoiceB.setOnClickListener(this);
        btnChoiceC.setOnClickListener(this);
        btnChoiceD.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnShowExplanation.setOnClickListener(this);
        btnAudioPlayPause.setOnClickListener(this);
        audioSeekBar.setOnSeekBarChangeListener(this);
    }

    private void fetchTestData() {
        showLoading(true);
        Log.d(TAG, "Fetching test part details for testId: " + currentTestId + ", partNumber: " + partNumber);

        apiService.getDetailedTestPartByNumber(currentTestId, partNumber).enqueue(new Callback<TestPart>() {
            @Override
            public void onResponse(@NonNull Call<TestPart> call, @NonNull Response<TestPart> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentTestPart = response.body();
                    if (currentTestPart.getPartId() == null) { finishWithError("Fetched TestPart missing ID."); return; }
                    currentPartId = currentTestPart.getPartId();
                    Log.d(TAG, "Successfully fetched TestPart, partId: " + currentPartId);

                    // Kiểm tra/cập nhật testId nếu cần
                    if (currentTestPart.getTest() != null && currentTestPart.getTest().getTestId() != null) {
                        Integer fetchedTestId = currentTestPart.getTest().getTestId();
                        if (!currentTestId.equals(fetchedTestId)) {
                            Log.w(TAG, "Using fetched testId " + fetchedTestId);
                            currentTestId = fetchedTestId;
                        }
                    } else { Log.w(TAG, "Fetched TestPart missing parent Test info."); }

                    prepareTestData();
                    if (!allQuestions.isEmpty()) {
                        totalQuestionsInPart = allQuestions.size();
                        calculateAndStartTotalTimer();
                        loadQuestion(0);
                    } else {
                        finishWithError("No questions found.");
                    }
                } else {
                    String errorMsg = "Failed load data: " + response.code();
                    try { if (response.errorBody() != null) errorMsg += " | " + response.errorBody().string();} catch (IOException e) {}
                    finishWithError(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TestPart> call, @NonNull Throwable t) {
                showLoading(false);
                finishWithError("Network error: " + t.getMessage());
            }
        });
    }

    private void prepareTestData() {
        if (currentTestPart == null || currentTestPart.getGroups() == null) { return; }
        if (currentTestPart.getGroups() instanceof List) { questionGroups = (List<TestQuestionGroup>) currentTestPart.getGroups(); }
        else { questionGroups = new ArrayList<>(currentTestPart.getGroups()); }
        try { questionGroups.sort(Comparator.comparing(TestQuestionGroup::getGroupIndex, Comparator.nullsLast(Comparator.naturalOrder()))); }
        catch (Exception e) { Log.e(TAG, "Sort groups error", e); }

        allQuestions.clear();
        userAnswers.clear();
        answerCorrectness.clear();

        for (TestQuestionGroup group : questionGroups) {
            if (group != null && group.getQuestions() != null) {
                try {
                    if (group.getQuestions() instanceof List) { ((List<TestQuestion>)group.getQuestions()).sort(Comparator.comparing(TestQuestion::getQuestionNumber, Comparator.nullsLast(Comparator.naturalOrder()))); }
                } catch (Exception e) { Log.e(TAG, "Sort questions error", e); }
                allQuestions.addAll(group.getQuestions());
            }
        }
        totalQuestionsInPart = allQuestions.size(); // Cập nhật tổng số câu ở đây
        Log.d(TAG, "Prepared data. Questions: " + totalQuestionsInPart);
        if (progressIndicator != null) {
            progressIndicator.setMax(totalQuestionsInPart);
            progressIndicator.setProgressCompat(0, false);
        }
    }

    private void loadQuestion(int index) {
        if (allQuestions.isEmpty() || index < 0 || index >= allQuestions.size()) { if (!allQuestions.isEmpty() && index >= allQuestions.size()) finishTest(); return; }
        currentQuestionIndex = index;
        TestQuestion question = allQuestions.get(index);
        if (question == null || question.getQuestionId() == null) { handleNext(); return; }

        resetUIForNewQuestion();

        tvQuestionIndicator.setText(String.format(Locale.getDefault(), "Question %d/%d", index + 1, totalQuestionsInPart)); // Dùng totalQuestionsInPart
        progressIndicator.setProgressCompat(index + 1, true);

        if (partNumber == 1) loadPart1UI(question);
        else loadPart5UI(question);

        loadChoices(question);
        restoreAnswerState(question.getQuestionId());
        updateNavigationButtons();
    }

    private void resetUIForNewQuestion(){
        resetChoiceButtonStates();
        // cardExplanation.setVisibility(View.GONE); // Bỏ dòng này
        // tvExplanationDetail.setText(""); // Bỏ dòng này
        stopAudio();
        isAudioPrepared = false;
        autoPlayedOnce = false;
        imgQuestionPart1.setVisibility(View.GONE);
        audioPlayerView.setVisibility(View.GONE);
        tvQuestionTextPart5.setVisibility(View.GONE);
        btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px);
        audioSeekBar.setProgress(0);
        tvAudioCurrentTime.setText("0:00");
        tvAudioTotalTime.setText("0:00");
        audioSeekBar.setEnabled(false);
    }

    private void resetChoiceButtonStates() {
        Button[] buttons = {btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD};
        int colorTextDefault = ContextCompat.getColor(this, R.color.black);
        for (Button btn : buttons) {
            btn.setBackgroundResource(R.drawable.button_background_default);
            btn.setTextColor(colorTextDefault);
            btn.setEnabled(true);
            if (partNumber == 1) { btn.setGravity(Gravity.CENTER); }
            else { btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); }
        }
    }

    private void loadPart1UI(TestQuestion question) {
        TestQuestionGroup group = findGroupForQuestion(question); if (group == null) return;
        String imageUrl = null; String audioUrl = null;
        if (group.getContents() != null) { for (TestQuestionContent content : group.getContents()) { if (content == null) continue; if ("IMAGE".equalsIgnoreCase(content.getContentType())) imageUrl = content.getContentData(); else if ("AUDIO".equalsIgnoreCase(content.getContentType())) audioUrl = content.getContentData(); } }
        imgQuestionPart1.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE); if (imageUrl != null) { Glide.with(this).load(imageUrl).placeholder(R.color.gray).error(R.drawable.ic_xml_broken_image_24px).into(imgQuestionPart1); }
        audioPlayerView.setVisibility(audioUrl != null ? View.VISIBLE : View.GONE); if (audioUrl != null) { prepareAudio(audioUrl); }
    }

    private void loadPart5UI(TestQuestion question) {
        imgQuestionPart1.setVisibility(View.GONE); audioPlayerView.setVisibility(View.GONE);
        tvQuestionTextPart5.setVisibility(View.VISIBLE); tvQuestionTextPart5.setText(question.getQuestionText() != null ? question.getQuestionText() : "...");
    }

    private TestQuestionGroup findGroupForQuestion(TestQuestion question) {
        if (questionGroups == null || question == null || question.getQuestionId() == null) return null;
        for (TestQuestionGroup group : questionGroups) { if (group == null || group.getQuestions() == null) continue; for (TestQuestion q : group.getQuestions()) { if (q != null && question.getQuestionId().equals(q.getQuestionId())) return group; } }
        return null;
    }

    private void loadChoices(TestQuestion question) {
        if (question == null || question.getChoices() == null || question.getChoices().isEmpty()) { btnChoiceA.setVisibility(View.GONE); btnChoiceB.setVisibility(View.GONE); btnChoiceC.setVisibility(View.GONE); btnChoiceD.setVisibility(View.GONE); return; }
        List<TestChoice> choices; if (question.getChoices() instanceof List) { choices = (List<TestChoice>) question.getChoices(); } else { choices = new ArrayList<>(question.getChoices()); }
        try { choices.sort(Comparator.comparing(TestChoice::getChoiceLabel, Comparator.nullsLast(Comparator.naturalOrder()))); } catch (Exception e) { Log.e(TAG, "Sort choices error", e); }
        Button[] buttons = {btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD};
        for (int i = 0; i < buttons.length; i++) { if (i < choices.size() && choices.get(i) != null) { setupChoiceButtonText(buttons[i], choices.get(i)); buttons[i].setTag(choices.get(i).getChoiceId()); buttons[i].setVisibility(View.VISIBLE); } else { buttons[i].setVisibility(View.GONE); } }
    }

    private void setupChoiceButtonText(Button button, TestChoice choice) {
        if (choice == null) { button.setVisibility(View.GONE); return; }
        String label = choice.getChoiceLabel() != null ? choice.getChoiceLabel() : "?";
        String textToShow;
        if (partNumber == 1) { textToShow = String.format("(%s)", label); button.setGravity(Gravity.CENTER); }
        else { String content = choice.getChoiceText(); if (content == null || content.trim().isEmpty()) { content = choice.getChoiceExplanation(); } textToShow = String.format("(%s) %s", label, content != null ? content : ""); button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); }
        button.setText(textToShow);
    }

    private void restoreAnswerState(int questionId) {
        Integer answeredChoiceId = userAnswers.get(questionId);
        Boolean isCorrect = answerCorrectness.get(questionId);
        if (answeredChoiceId != null && isCorrect != null) {
            if (currentQuestionIndex < allQuestions.size()) {
                TestQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
                if (currentQuestion != null && currentQuestion.getQuestionId() == questionId) {
                    showFeedback(answeredChoiceId, currentQuestion.getCorrectAnswerLabel());
                    lockChoices();
                }
            }
        } else {
            enableChoices();
        }
    }

    private void updateNavigationButtons() {
        btnBack.setVisibility(currentQuestionIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setText(currentQuestionIndex == allQuestions.size() - 1 ? "Finish" : "Next");
    }

    private void handleChoiceSelection(Button selectedButton) {
        if (selectedButton.getTag() == null || !(selectedButton.getTag() instanceof Integer)) return;
        if (currentQuestionIndex < 0 || currentQuestionIndex >= allQuestions.size()) return;
        TestQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
        if (currentQuestion == null || currentQuestion.getQuestionId() == null) return;
        int currentQuestionId = currentQuestion.getQuestionId();
        if (userAnswers.containsKey(currentQuestionId)) return; // Đã trả lời

        int selectedChoiceId = (Integer) selectedButton.getTag();
        TestChoice selectedChoice = findChoiceById(currentQuestion, selectedChoiceId);
        if (selectedChoice == null) return;

        boolean isCorrect = false;
        if (selectedChoice.getChoiceLabel() != null && currentQuestion.getCorrectAnswerLabel() != null) { isCorrect = selectedChoice.getChoiceLabel().equalsIgnoreCase(currentQuestion.getCorrectAnswerLabel()); }
        userAnswers.put(currentQuestionId, selectedChoiceId);
        answerCorrectness.put(currentQuestionId, isCorrect);
        Log.d(TAG, "QID: " + currentQuestionId + " Answered: " + selectedChoiceId + " Correct: " + isCorrect);

        showFeedback(selectedChoiceId, currentQuestion.getCorrectAnswerLabel());
        lockChoices();
    }

    private TestChoice findChoiceById(TestQuestion question, int choiceId) {
        if (question == null || question.getChoices() == null) return null; for (TestChoice choice : question.getChoices()) { if (choice != null && choice.getChoiceId() != null && choice.getChoiceId() == choiceId) return choice; } return null;
    }

    private Button findButtonByChoiceId(int choiceId) {
        Button[] buttons = {btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD}; for (Button btn : buttons) { if (btn.getTag() != null && btn.getTag() instanceof Integer && (Integer) btn.getTag() == choiceId) return btn; } return null;
    }

    private void showFeedback(int selectedChoiceId, String correctAnswerLabel) {
        Button selectedButton = findButtonByChoiceId(selectedChoiceId);
        Button correctButton = findButtonByCorrectLabel(correctAnswerLabel);
        int colorCorrect = ContextCompat.getColor(this, R.color.green_correct);
        int colorIncorrect = ContextCompat.getColor(this, R.color.red_incorrect);
        int colorTextWhite = Color.WHITE;
        int colorTextDefault = ContextCompat.getColor(this, R.color.black);

        Button[] buttons = {btnChoiceA, btnChoiceB, btnChoiceC, btnChoiceD};
        for (Button btn : buttons) {
            if (btn.getVisibility() == View.GONE || btn.getTag() == null || !(btn.getTag() instanceof Integer)) continue;
            int currentChoiceId = (Integer) btn.getTag();

            if (btn == correctButton) {
                btn.setBackgroundResource(R.drawable.button_background_correct);
                btn.setTextColor(colorTextWhite);
            } else if (currentChoiceId == selectedChoiceId) {
                btn.setBackgroundResource(R.drawable.button_background_incorrect);
                btn.setTextColor(colorTextWhite);
            } else {
                btn.setBackgroundResource(R.drawable.button_background_default);
                btn.setTextColor(colorTextDefault);
            }
        }
    }

    private Button findButtonByCorrectLabel(String correctLabel) {
        if (correctLabel == null || currentQuestionIndex < 0 || currentQuestionIndex >= allQuestions.size()) return null; TestQuestion q = allQuestions.get(currentQuestionIndex); if(q == null || q.getChoices() == null) return null; for(TestChoice c : q.getChoices()) { if(c != null && c.getChoiceLabel() != null && correctLabel.equalsIgnoreCase(c.getChoiceLabel()) && c.getChoiceId() != null) return findButtonByChoiceId(c.getChoiceId()); } return null;
    }

    private void lockChoices() { btnChoiceA.setEnabled(false); btnChoiceB.setEnabled(false); btnChoiceC.setEnabled(false); btnChoiceD.setEnabled(false); }
    private void enableChoices() { btnChoiceA.setEnabled(true); btnChoiceB.setEnabled(true); btnChoiceC.setEnabled(true); btnChoiceD.setEnabled(true); }

    private void handleNext() { if (currentQuestionIndex < allQuestions.size() - 1) { loadQuestion(currentQuestionIndex + 1); } else { finishTest(); } }
    private void handleBack() { if (currentQuestionIndex > 0) { loadQuestion(currentQuestionIndex - 1); } }

    // Sửa lại handleShowExplanation để dùng BottomSheet
    private void handleShowExplanation() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= allQuestions.size()) return;
        TestQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
        if (currentQuestion == null) return;

        // 1. Chuẩn bị nội dung Explanation
        StringBuilder explanationContent = new StringBuilder();
        String title = "Explanation / Transcript"; // Title mặc định

        if (partNumber == 1) { // Part 1: Hiển thị giải thích của từng choice
            title = "Transcripts / Options";
            if (currentQuestion.getChoices() != null && !currentQuestion.getChoices().isEmpty()) {
                List<TestChoice> choices; if (currentQuestion.getChoices() instanceof List) { choices = (List<TestChoice>) currentQuestion.getChoices(); } else { choices = new ArrayList<>(currentQuestion.getChoices()); } try { choices.sort(Comparator.comparing(TestChoice::getChoiceLabel, Comparator.nullsLast(Comparator.naturalOrder()))); } catch (Exception e) {}
                for (TestChoice choice : choices) { if (choice != null && choice.getChoiceLabel() != null && choice.getChoiceExplanation() != null) { boolean isThisCorrect = choice.getChoiceLabel().equalsIgnoreCase(currentQuestion.getCorrectAnswerLabel()); if (isThisCorrect) explanationContent.append("<b><font color='#4CAF50'>"); explanationContent.append(choice.getChoiceLabel()).append(". "); explanationContent.append(choice.getChoiceExplanation()); if (isThisCorrect) explanationContent.append("</font></b>"); explanationContent.append("<br/><br/>"); } }
                if (currentQuestion.getExplanation() != null && !currentQuestion.getExplanation().trim().isEmpty()) { explanationContent.append("<hr><b>More Info:</b><br/>").append(currentQuestion.getExplanation()); }
            } else { explanationContent.append("Choice details not available."); }
        } else { // Part 5 (và các part khác): Hiển thị explanation của Question
            String explanation = currentQuestion.getExplanation(); if (explanation != null && !explanation.trim().isEmpty()) { explanationContent.append(explanation); } else { explanationContent.append("No detailed explanation available."); }
        }

        // 2. Tạo và hiển thị BottomSheetDialog
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        // Inflate layout mới (root là null)
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_explanation, null);

        // Tìm các View bên trong bottomSheetView
        TextView tvTitle = bottomSheetView.findViewById(R.id.tv_bottom_sheet_title);
        TextView tvDetail = bottomSheetView.findViewById(R.id.tv_bottom_sheet_explanation_detail);
        Button btnClose = bottomSheetView.findViewById(R.id.btn_close_bottom_sheet);

        // Set Title
        if (tvTitle != null) tvTitle.setText(title);

        // Set nội dung Explanation
        if (tvDetail != null) {
            if (explanationContent.length() > 0) {
                try { tvDetail.setText(Html.fromHtml(explanationContent.toString(), Html.FROM_HTML_MODE_COMPACT)); }
                catch (Exception e) { tvDetail.setText(explanationContent.toString().replace("<br/>","\n").replace("<hr>","\n---\n").replaceAll("<[^>]*>", "")); }
            } else { tvDetail.setText("Explanation not available."); }
        }

        // Set sự kiện cho nút Close
        if (btnClose != null) btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    private void finishTest() {
        stopAudio();
        stopTotalTestTimer();
        Log.d(TAG, "Finishing test locally.");
        int correctCount = 0; for(Boolean c : answerCorrectness.values()) { if(Boolean.TRUE.equals(c)) correctCount++; }
        // int totalQuestionsInPart đã gán ở prepareTestData
        Log.d(TAG, "Local score: " + correctCount + "/" + totalQuestionsInPart);
        Intent intent = new Intent(TestActivity.this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, correctCount);
        intent.putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, totalQuestionsInPart);
        startActivity(intent);
        finish();
    }

    private void disableTestInteraction() { lockChoices(); }


    private void startTotalTestTimer(long duration) {
        stopTotalTestTimer(); // Dừng timer cũ nếu đang chạy
        Log.d(TAG, "Starting total timer for " + duration + " ms");
        totalTestTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished; // <-- LƯU THỜI GIAN CÒN LẠI Ở ĐÂY
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );
                if (millisUntilFinished <= 60 * 1000) {
                    tvTimer.setTextColor(ContextCompat.getColor(TestActivity.this, R.color.red_incorrect));
                } else {
                    // Có thể thay đổi màu cho đẹp hơn, ví dụ màu của theme
                    tvTimer.setTextColor(ContextCompat.getColor(TestActivity.this, android.R.color.tab_indicator_text)); // Màu xanh mặc định
                    // Hoặc dùng màu theme: tvTimer.setTextColor(getColor(R.color.blue)); // Nếu bạn định nghĩa màu xanh riêng
                }
                tvTimer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                tvTimer.setTextColor(ContextCompat.getColor(TestActivity.this, R.color.red_incorrect));
                Log.d(TAG, "Total test time finished!");
                millisRemaining = 0; // Đảm bảo reset về 0 khi hết giờ
                handleTotalTimeUp();
            }
        }.start();
    }

    private void stopTotalTestTimer() { if (totalTestTimer != null) { totalTestTimer.cancel(); totalTestTimer = null; Log.d(TAG, "Total timer stopped."); } }

    private void handleTotalTimeUp() {
        Toast.makeText(this, "Time's up! Submitting...", Toast.LENGTH_LONG).show();
        btnNext.setEnabled(false); btnBack.setEnabled(false); btnShowExplanation.setEnabled(false); lockChoices();
        finishTest(); // Tự động nộp bài
    }


    // --- Audio Player Logic ---
    private void prepareAudio(String url) {
        stopAudio(); mediaPlayer = new MediaPlayer(); try { mediaPlayer.setDataSource(url); mediaPlayer.setOnPreparedListener(this); mediaPlayer.setOnCompletionListener(this); mediaPlayer.setOnErrorListener((mp, what, extra) -> { Log.e(TAG, "MP Error: " + what + "," + extra + " URL: " + url); Toast.makeText(this, "Audio Error", Toast.LENGTH_SHORT).show(); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); audioSeekBar.setEnabled(false); return true; }); mediaPlayer.prepareAsync(); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_hourglass_top_24px); audioSeekBar.setEnabled(false); } catch (Exception e) { Log.e(TAG, "DataSource Error: " + url, e); Toast.makeText(this, "Cannot load audio", Toast.LENGTH_SHORT).show(); audioPlayerView.setVisibility(View.GONE); }
    }
    private void playAudio() { if (mediaPlayer != null && isAudioPrepared) { try { mediaPlayer.start(); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_pause_24px); updateAudioProgress(); } catch (IllegalStateException e) { Log.e(TAG, "MP start error: ", e); } } }
    private void pauseAudio() { if (mediaPlayer != null && isAudioPrepared && mediaPlayer.isPlaying()) { try { mediaPlayer.pause(); } catch (IllegalStateException e) { Log.e(TAG, "MP pause error: ", e); } btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); audioHandler.removeCallbacks(audioProgressRunnable); } }
    private void stopAudio() { audioHandler.removeCallbacksAndMessages(null); if (mediaPlayer != null) { try { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); mediaPlayer.reset(); mediaPlayer.release(); } catch (Exception e) { Log.e(TAG, "MP stop/release error", e); } mediaPlayer = null; } isAudioPrepared = false; }
    private final Runnable audioProgressRunnable = new Runnable() { @Override public void run() { if (mediaPlayer != null && isAudioPrepared) { try { if (mediaPlayer.isPlaying()) { int pos = mediaPlayer.getCurrentPosition(); audioSeekBar.setProgress(pos); tvAudioCurrentTime.setText(formatTime(pos)); audioHandler.postDelayed(this, 500); } } catch (IllegalStateException e) { Log.e(TAG, "MP get pos error: ", e); audioHandler.removeCallbacks(this); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); } } } };
    private void updateAudioProgress() { audioHandler.removeCallbacks(audioProgressRunnable); audioHandler.post(audioProgressRunnable); }
    @Override public void onPrepared(MediaPlayer mp) { Log.d(TAG, "Audio prepared."); isAudioPrepared = true; if (mediaPlayer == null) return; try { int duration = mediaPlayer.getDuration(); audioSeekBar.setMax(duration); tvAudioTotalTime.setText(formatTime(duration)); audioSeekBar.setEnabled(true); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); if (partNumber == 1 && !autoPlayedOnce) { playAudio(); autoPlayedOnce = true; } } catch (IllegalStateException e) { Log.e(TAG, "onPrepared error", e); } }
    @Override public void onCompletion(MediaPlayer mp) { Log.d(TAG, "Audio completed."); btnAudioPlayPause.setImageResource(R.drawable.ic_xml_play_arrow_24px); if (mediaPlayer != null && isAudioPrepared) { try { audioSeekBar.setProgress(audioSeekBar.getMax()); tvAudioCurrentTime.setText(tvAudioTotalTime.getText()); } catch (IllegalStateException e) { Log.e(TAG, "onCompletion error", e); } } audioHandler.removeCallbacks(audioProgressRunnable); }
    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { if (mediaPlayer != null && isAudioPrepared && fromUser) { try { mediaPlayer.seekTo(progress); tvAudioCurrentTime.setText(formatTime(progress)); } catch (IllegalStateException e) { Log.e(TAG, "Seek error", e); } } }
    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    private void toggleAudioPlayback() { if (!isAudioPrepared && mediaPlayer != null) return; if (mediaPlayer != null && isAudioPrepared) { if (mediaPlayer.isPlaying()) pauseAudio(); else { try { if (!mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() >= mediaPlayer.getDuration() - 100) { mediaPlayer.seekTo(0); tvAudioCurrentTime.setText("0:00"); audioSeekBar.setProgress(0); } } catch (IllegalStateException e) { Log.e(TAG, "Seek 0 error", e); } playAudio(); } } else if (partNumber == 1){ if (!allQuestions.isEmpty() && currentQuestionIndex < allQuestions.size()) { TestQuestion q = allQuestions.get(currentQuestionIndex); TestQuestionGroup g = findGroupForQuestion(q); if (g != null && g.getContents() != null) { for (TestQuestionContent c : g.getContents()) { if ("AUDIO".equalsIgnoreCase(c.getContentType())) { prepareAudio(c.getContentData()); break; } } } } } }
    private String formatTime(int ms) { if (ms < 0) ms = 0; long min = TimeUnit.MILLISECONDS.toMinutes(ms); long sec = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(min); return String.format(Locale.getDefault(), "%01d:%02d", min, sec); }
    private void finishWithError(String message) { Log.e(TAG, "Error: " + message); runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show()); finish(); }
    private void showLoading(boolean show) { Log.d(TAG, "Loading: " + show); /* TODO: Implement ProgressBar visibility */ }

    @Override
    protected void onDestroy() {
        super.onDestroy(); Log.d(TAG, "onDestroy"); stopAudio(); stopTotalTestTimer(); audioHandler.removeCallbacksAndMessages(null);
    }
    @Override
    public void onBackPressed() {
        // Khi bấm back, dừng timer hiện tại trước khi hiển thị dialog
        stopTotalTestTimer();

        new AlertDialog.Builder(this)
                .setTitle("Exit Test?")
                // Sửa lại thông báo cho chính xác hơn
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    // Xử lý khi chọn Exit
                    stopAudio(); // Dừng audio
                    super.onBackPressed(); // Quay lại hoặc thoát activity
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Xử lý khi chọn Cancel
                    // Khởi động lại timer với thời gian còn lại
                    if (millisRemaining > 0) { // Chỉ khởi động lại nếu còn thời gian
                        startTotalTestTimer(millisRemaining);
                    } else {
                        // Nếu millisRemaining == 0 (đã hết giờ trước khi dialog bật lên)
                        // thì không cần làm gì cả, timer đã finish
                        tvTimer.setText("00:00"); // Đảm bảo hiển thị 00:00
                        tvTimer.setTextColor(ContextCompat.getColor(TestActivity.this, R.color.red_incorrect));
                    }
                    dialog.dismiss(); // Đóng dialog
                })
                .setCancelable(false) // Người dùng phải chọn 1 trong 2 nút
                .show();
    }

    // --- Click Listener ---
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_choice_a || id == R.id.btn_choice_b || id == R.id.btn_choice_c || id == R.id.btn_choice_d) { handleChoiceSelection((Button) v); }
        else if (id == R.id.btn_next) { handleNext(); }
        else if (id == R.id.btn_back) { handleBack(); }
        else if (id == R.id.audio_btn_play_pause) { toggleAudioPlayback(); }
        else if (id == R.id.btn_show_explanation) { handleShowExplanation(); }
    }
}