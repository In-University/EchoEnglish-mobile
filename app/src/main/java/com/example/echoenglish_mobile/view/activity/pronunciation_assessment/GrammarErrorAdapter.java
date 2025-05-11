package com.example.echoenglish_mobile.view.activity.pronunciation_assessment;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.dto.GrammarError;

import java.util.List;

public class GrammarErrorAdapter extends RecyclerView.Adapter<GrammarErrorAdapter.ErrorViewHolder> {

    private List<GrammarError> errorList;

    public GrammarErrorAdapter(List<GrammarError> errorList) {
        this.errorList = errorList;
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grammar_error, parent, false);
        return new ErrorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder holder, int position) {
        GrammarError error = errorList.get(position);

        holder.errorTypeText.setText(error.getType());
        holder.originalText.setText("Original: " + error.getOriginalText());
        holder.correctionText.setText("Correction: " + error.getCorrectionText());
        holder.explanationText.setText("Explanation: " + error.getExplanation());

        String colorHex;
        switch (error.getSeverityColor().toLowerCase()) {
            case "high":
                colorHex = "#F44336"; // Red
                break;
            case "medium":
                colorHex = "#FF9800"; // Orange
                break;
            case "low":
                colorHex = "#4CAF50"; // Green
                break;
            default:
                colorHex = "#F44336"; // Default to red
                break;
        }

        int parsedColor = Color.parseColor(colorHex);
        holder.errorTypeIndicator.setBackgroundColor(parsedColor);
        holder.errorTypeText.setTextColor(parsedColor);
    }

    @Override
    public int getItemCount() {
        return errorList == null ? 0 : errorList.size();
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        TextView errorTypeText;
        TextView originalText;
        TextView correctionText;
        TextView explanationText;
        View errorTypeIndicator;

        ErrorViewHolder(View itemView) {
            super(itemView);
            errorTypeText = itemView.findViewById(R.id.errorTypeText);
            originalText = itemView.findViewById(R.id.originalText);
            correctionText = itemView.findViewById(R.id.correctionText);
            explanationText = itemView.findViewById(R.id.explanationText);
            errorTypeIndicator = itemView.findViewById(R.id.errorTypeIndicator);
        }
    }
}