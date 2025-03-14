package com.example.echoenglish_mobile.ui.pronunciation_assessment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.example.echoenglish_mobile.R;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;

public class PhoneticFeedbackFragment extends DialogFragment {

    // UI Components
    private TextView tvWord;
    private TableLayout tablePhonemes;
    private ImageButton btnPlayWord;
    private ImageButton btnSlowPlay;

    // Data
    private String word;
    private List<PhonemeResult> phonemeResults;

    public static PhoneticFeedbackFragment newInstance() {
        return new PhoneticFeedbackFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phonetic_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvWord = view.findViewById(R.id.tv_word);
        tablePhonemes = view.findViewById(R.id.table_pronunciation);
        btnPlayWord = view.findViewById(R.id.btn_play_word);
        btnSlowPlay = view.findViewById(R.id.btn_slow_play);

        // Set click listeners
        btnPlayWord.setOnClickListener(v -> playWordAudio());
        btnSlowPlay.setOnClickListener(v -> playSlowAudio());

        // Load data
        loadData();

        // Update UI
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialog();
    }

    private void configureDialog() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.CENTER;
            // Làm mờ nền phía sau
            params.dimAmount = 0.6f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void loadData() {
        // fake data
        word = "communication";
        phonemeResults = new ArrayList<>();
        phonemeResults.add(new PhonemeResult("/k/", true, null, null));
        phonemeResults.add(new PhonemeResult("/ə/", true, null, null));
        phonemeResults.add(new PhonemeResult("/m/", true, null, null));
        phonemeResults.add(new PhonemeResult("/j/", false, "/u/", null));
    }

    private void updateUI() {
        tvWord.setText(word);

        int headerRowCount = 1;
        if (tablePhonemes.getChildCount() > headerRowCount) {
            tablePhonemes.removeViews(headerRowCount, tablePhonemes.getChildCount() - headerRowCount);
        }

        for (PhonemeResult result : phonemeResults) {
            addPhonemeRow(result);
        }
    }

    private void addPhonemeRow(PhonemeResult result) {
        Context context = requireContext();
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Column 1: Phoneme
        TextView tvPhoneme = new TextView(context);
        tvPhoneme.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        tvPhoneme.setText(result.getPhoneme());
        tvPhoneme.setTextSize(18);
        tvPhoneme.setTextColor(Color.BLACK);
        tvPhoneme.setPadding(16, 16, 16, 16);
        tvPhoneme.setGravity(Gravity.CENTER);
        row.addView(tvPhoneme);

        // Column 2: User's pronunciation result
        LinearLayout resultLayout = new LinearLayout(context);
        resultLayout.setOrientation(LinearLayout.VERTICAL);
        resultLayout.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        resultLayout.setGravity(Gravity.CENTER);
        resultLayout.setPadding(16, 16, 16, 16);

        // Add correct/incorrect icon
        ImageView iconResult = new ImageView(context);
        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
        iconResult.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));

        if (result.isCorrect()) {
            iconResult.setImageResource(R.drawable.ic_correct);
            iconResult.setContentDescription("Correct pronunciation");
        } else {
            iconResult.setImageResource(R.drawable.ic_incorrect);
            iconResult.setContentDescription("Incorrect pronunciation");

            // Add user's pronunciation if incorrect
            if (result.getUserPronunciation() != null) {
                TextView tvUserPronunciation = new TextView(context);
                tvUserPronunciation.setTextSize(16);
                tvUserPronunciation.setTextColor(Color.RED);
                tvUserPronunciation.setText(result.getUserPronunciation());
                tvUserPronunciation.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.topMargin = 8;
                tvUserPronunciation.setLayoutParams(params);
                resultLayout.addView(tvUserPronunciation);
            }
        }

        resultLayout.addView(iconResult);
        row.addView(resultLayout);

        // Add row to table
        tablePhonemes.addView(row);

        // If there's feedback, add a new row for it
        if (result.getFeedback() != null && !result.isCorrect()) {
            addFeedbackRow(result.getFeedback());
        }
    }

    private void addFeedbackRow(String feedback) {
        Context context = requireContext();
        TableRow feedbackRow = new TableRow(context);
        feedbackRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        feedbackRow.setBackgroundColor(Color.parseColor("#F5F5F5"));

        // Create feedback text view that spans two columns
        TextView tvFeedback = new TextView(context);
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 2; // Span across both columns
        params.width = 0;
        params.weight = 1;
        tvFeedback.setLayoutParams(params);

        tvFeedback.setPadding(16, 16, 16, 16);
        tvFeedback.setText(feedback);
        tvFeedback.setTextColor(Color.parseColor("#666666"));
        tvFeedback.setTextSize(14);

        feedbackRow.addView(tvFeedback);
        tablePhonemes.addView(feedbackRow);
    }

    private void playWordAudio() {
        // In a real app, this would play the audio for the word
        showToast("Playing pronunciation for: " + word);
    }

    private void playSlowAudio() {
        // In a real app, this would play the slowed-down audio
        showToast("Playing slow pronunciation for: " + word);
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    // Model class for phoneme results
    public static class PhonemeResult {
        private String phoneme;
        private boolean isCorrect;
        private String userPronunciation;
        private String feedback;

        public PhonemeResult(String phoneme, boolean isCorrect, String userPronunciation, String feedback) {
            this.phoneme = phoneme;
            this.isCorrect = isCorrect;
            this.userPronunciation = userPronunciation;
            this.feedback = feedback;
        }

        public String getPhoneme() {
            return phoneme;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public String getUserPronunciation() {
            return userPronunciation;
        }

        public String getFeedback() {
            return feedback;
        }
    }
}