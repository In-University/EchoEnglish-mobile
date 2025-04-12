package com.example.echoenglish_mobile.view.fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.view.customview.PhonemeTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;

public class PhoneticFeedbackFragment extends DialogFragment {

    // UI Components
    private PhonemeTextView tvWord;
    private TableLayout tablePhonemes;
    private ImageButton btnPlayWord;
    private ImageButton btnSlowPlay;

    // Data
    private String word;
    private List<PhonemeComparison> phonemeComparisons;

    private static final String ARG_COMPARISON_LIST = "comparison_list";
    private static final String ARG_WORD = "word";

    public static PhoneticFeedbackFragment newInstance(List<PhonemeComparison> comparisonList, String word) {
        PhoneticFeedbackFragment fragment = new PhoneticFeedbackFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COMPARISON_LIST, (Serializable) comparisonList);
        args.putString(ARG_WORD, word);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        if (getArguments() != null && getArguments().containsKey(ARG_COMPARISON_LIST)) {
            phonemeComparisons = (List<PhonemeComparison>) getArguments().getSerializable(ARG_COMPARISON_LIST);
            word = getArguments().getString(ARG_WORD, "");
        } else {
            loadData(); // fake data
        }

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
        String[] transcriptionNoStress = {"k", "ə", "m", "j", "u", "n", "ɪ", "k", "eɪ", "ʃ", "ə", "n"};
        String targetWord = "communication";

        StringBuilder sb = new StringBuilder();
        for (String p : transcriptionNoStress) {
            sb.append(p);
        }
        String result = sb.toString();

        word = targetWord;
        phonemeComparisons = new ArrayList<>();

        int currentIndex = 0;
        for (int i = 0; i < transcriptionNoStress.length; i++) {
            String correctPhoneme = transcriptionNoStress[i];
            String actualPhoneme = correctPhoneme;
            if (i == 3) {
                actualPhoneme = "incorrect";
            }
            int start = currentIndex;
            int end = currentIndex + correctPhoneme.length();
            currentIndex = end;
            phonemeComparisons.add(new PhonemeComparison(result, actualPhoneme, correctPhoneme, start, end));
        }
    }
    private void updateUI() {
        tvWord.setText(word);
        tvWord.setPhonemeData(word, phonemeComparisons);
        int headerRowCount = 1;
        if (tablePhonemes.getChildCount() > headerRowCount) {
            tablePhonemes.removeViews(headerRowCount, tablePhonemes.getChildCount() - headerRowCount);
        }

        for (PhonemeComparison dto : phonemeComparisons) {
            addPhonemeRow(dto);
        }
    }

    private void addPhonemeRow(PhonemeComparison dto) {
        LayoutInflater inflater = getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_phonetic_feedback_row, tablePhonemes, false);

        TextView tvPhoneme = rowView.findViewById(R.id.tv_phoneme);
        TextView tvResult = rowView.findViewById(R.id.tv_result);
        TextView tvWrongPhoneme = rowView.findViewById(R.id.tv_wrong_phoneme);
        ImageButton btnPlayPhoneme = rowView.findViewById(R.id.btn_play_phoneme);

        tvPhoneme.setText("/" + dto.getCorrectPhoneme() + "/");

        btnPlayPhoneme.setOnClickListener(v -> playPhonemeAudio(dto.getCorrectPhoneme()));

        boolean isCorrect = dto.getResult().equals("correct");
        if (isCorrect) {
            tvResult.setText("Correct!");
            tvResult.setTextColor(Color.parseColor("#4CAF50"));
            tvWrongPhoneme.setVisibility(View.GONE);
        } else {
            tvResult.setVisibility(View.GONE);
            tvWrongPhoneme.setText("/" + dto.getActualPhoneme() + "/");
        }

        // Thêm dòng vào TableLayout
        tablePhonemes.addView(rowView);
    }

    private void playPhonemeAudio(String phoneme) {
        showToast("Playing pronunciation for phoneme: " + phoneme);
    }

    private void playWordAudio() {
        showToast("Playing pronunciation for: " + word);
    }

    private void playSlowAudio() {
        showToast("Playing slow pronunciation for: " + word);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
