package com.example.echoenglish_mobile.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.GrammarErrorAdapter;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.dto.SpeakingFeedback;
import com.google.gson.Gson;
public class GrammarResultFragment extends Fragment {

    private TextView overviewText;
    private TextView suggestionText;
    private RecyclerView errorsRecyclerView;
    private LinearLayout expandableLayout;
    private LinearLayout expansionHeader;
    private ImageView expandIcon;
    private boolean isExpanded = false;
    private GrammarErrorAdapter grammarErrorAdapter;
    private SpeakingFeedback feedback;
    private static final String ARG_RESULT = "grammar_feedbacks";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grammar_result, container, false); // Sử dụng layout của fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_RESULT)) {
            feedback = (SpeakingFeedback) getArguments().getSerializable(ARG_RESULT);
        }

        overviewText = view.findViewById(R.id.overviewText);
        suggestionText = view.findViewById(R.id.suggestionText);
        errorsRecyclerView = view.findViewById(R.id.errorsRecyclerView);
        expandableLayout = view.findViewById(R.id.expandableLayout);
        expansionHeader = view.findViewById(R.id.expansionHeader);
        expandIcon = view.findViewById(R.id.expandIcon);

        expandableLayout.setVisibility(View.GONE);
        expandIcon.setImageResource(android.R.drawable.arrow_down_float);
        isExpanded = false;

        setupExpansionPanel();
        displayFeedback(feedback);
    }

    public static GrammarResultFragment newInstance(SpeakingFeedback feedback) {
        GrammarResultFragment fragment = new GrammarResultFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT, feedback);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupExpansionPanel() {
        expansionHeader.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            expandIcon.setImageResource(
                    isExpanded ? android.R.drawable.arrow_up_float
                            : android.R.drawable.arrow_down_float
            );
        });
    }

    private void displayFeedback(SpeakingFeedback feedback) {
        if (feedback == null) {
            if (overviewText != null) {
                overviewText.setText("No data!");
            }
            return;
        }

        try {
            if (overviewText != null) overviewText.setText(feedback.getOverview());
            if (suggestionText != null) suggestionText.setText(feedback.getSuggestion());

            if (errorsRecyclerView != null && feedback.getErrors() != null && getContext() != null) {
                errorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                grammarErrorAdapter = new GrammarErrorAdapter(feedback.getErrors());
                errorsRecyclerView.setAdapter(grammarErrorAdapter);
            } else if (overviewText != null && (feedback.getErrors() == null || feedback.getErrors().isEmpty())) {
                if (expandableLayout != null) expandableLayout.setVisibility(View.GONE);
                if (expansionHeader != null) expansionHeader.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (overviewText != null) {
                overviewText.setText("Cannot show result!");
            }
        }
    }
}