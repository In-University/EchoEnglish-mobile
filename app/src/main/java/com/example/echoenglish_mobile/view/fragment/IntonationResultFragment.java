package com.example.echoenglish_mobile.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
}