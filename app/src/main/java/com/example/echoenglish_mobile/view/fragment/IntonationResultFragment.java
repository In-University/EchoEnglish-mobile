package com.example.echoenglish_mobile.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.WordDetail;
import com.example.echoenglish_mobile.view.customview.PhonemeTextView;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class IntonationResultFragment extends Fragment {
    private SentenceAnalysisResult result;
    private FlexboxLayout container;
    private static final String ARG_RESULT = "sentence_analysis_result";
    public static IntonationResultFragment newInstance(SentenceAnalysisResult result) {
        IntonationResultFragment fragment = new IntonationResultFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intonation_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = view.findViewById(R.id.myAnswerContainer);

        if (getArguments() != null && getArguments().containsKey(ARG_RESULT)) {
            result = (SentenceAnalysisResult) getArguments().getSerializable(ARG_RESULT);
        }

        addPhonemeTextView();

        TextView tvSkillTitle = view.findViewById(R.id.tvSkillTitle);
        TextView tvSkillFeedback = view.findViewById(R.id.tvSkillFeedback);
        TextView tvSkillProgress = view.findViewById(R.id.tvSkillProgress);

        tvSkillTitle.setText("Average Pitch");

        double avgPitch = calculateAveragePitch(result);
        tvSkillFeedback.setText(analyzePitchSimple(avgPitch));
        tvSkillProgress.setText(String.format("%.2f Hz", avgPitch));
    }

    private void addPhonemeTextView() {
        if (container != null) {
            List<WordDetail> phonemeData = result.getChunks();
            for (WordDetail wordDetail : phonemeData) {
                PhonemeTextView phonemeTextView = new PhonemeTextView(requireContext());
                phonemeTextView.setTextSize(18);
                phonemeTextView.setPhonemeData(wordDetail.getText(), wordDetail.getAnalysis().getStress_level());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd(12);
                phonemeTextView.setLayoutParams(params);
                container.setAlignItems(AlignItems.BASELINE);
                container.addView(phonemeTextView);
            }
        }
    }

    public double calculateAveragePitch(SentenceAnalysisResult result) {
        if (result == null || result.getChunks() == null || result.getChunks().isEmpty()) {
            return 0.0;
        }

        List<WordDetail> words = result.getChunks();
        double totalPitch = 0.0;
        int count = 0;

        for (WordDetail word : words) {
            if (word != null && word.getAnalysis() != null) {
                totalPitch += word.getAnalysis().getPitch();
                count++;
            }
        }

        return count == 0 ? 0.0 : totalPitch / count;
    }

    public static String analyzePitchSimple(double avgPitch) {
        StringBuilder result = new StringBuilder();

        if (avgPitch < 100) {
            result.append("The pitch is quite low. It may sound flat, monotone, or emotionless.\n");
        } else if (avgPitch > 300) {
            result.append("The pitch is too high and might sound unnatural or annoying to listeners.\n");
        } else {
            result.append("The pitch is in a comfortable and natural range.\n");
        }
        result.append("Pitch mainly helps express emotions and emphasis in speech. A natural range makes your speech more engaging and easier to understand.\n");
        return result.toString();
    }
}