package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.content.Context;
import android.content.Intent; // Thêm import Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.SentenceSummary;
import com.example.echoenglish_mobile.model.response.SentenceAnalysisMetadata;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.SummaryResultsActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList; // Thêm import nếu dùng new ArrayList

public class SpeakingResultAdapter extends RecyclerView.Adapter<SpeakingResultAdapter.SpeakingViewHolder> {

    private List<SentenceAnalysisResult> analysisResults;
    private final Context context;
    private final DateTimeFormatter dateTimeFormatter;


    public SpeakingResultAdapter(Context context, List<SentenceAnalysisResult> analysisResults) {
        this.context = context;
        this.analysisResults = analysisResults != null ? analysisResults : new ArrayList<>();
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    }

    @NonNull
    @Override
    public SpeakingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_speak_analyze_result, parent, false);
        return new SpeakingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpeakingViewHolder holder, int position) {
        final SentenceAnalysisResult result = analysisResults.get(position); // Đặt là final để dùng trong listener

        if (result == null) {
            holder.itemView.setVisibility(View.GONE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        SentenceAnalysisMetadata metadata = result.getMetadata();
        SentenceSummary summary = result.getSummary();

        // --- Binding ---
        String title = (metadata != null && metadata.getTargetWord() != null && !metadata.getTargetWord().isEmpty())
                ? metadata.getTargetWord()
                : result.getText();
        holder.tvTitle.setText(title != null ? title : "N/A");

        if (metadata != null && metadata.getCreatedAt() != null) {
            try {
                LocalDateTime createdAt = metadata.getCreatedAt();
                holder.tvDate.setText(createdAt.format(dateTimeFormatter));
            } catch (Exception e) {
                holder.tvDate.setText("N/A");
            }
        } else {
            holder.tvDate.setText("N/A");
        }
        holder.tvCategory.setText("Free speech");

        if (summary != null) {
//            holder.tvScore.setText(String.format(Locale.getDefault(), "%.0f%%", summary.getAccuracyScore()));
//            holder.tvPronunciation.setText(String.format(Locale.getDefault(), "%.0f%%", summary.getPronAccuracyScore()));
//            holder.tvRhythm.setText(String.format(Locale.getDefault(), "%.0f%%", summary.getPronFluencyScore()));
//            holder.tvAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", summary.getPronCompletenessScore()));
        } else {
            holder.tvScore.setText("0%");
            holder.tvPronunciation.setText("0%");
            holder.tvRhythm.setText("0%");
            holder.tvAccuracy.setText("0%");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SummaryResultsActivity.class);
                intent.putExtra(SummaryResultsActivity.ANALYSIS_RESULT, result);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return analysisResults.size();
    }

    public void updateData(List<SentenceAnalysisResult> newResults) {
        this.analysisResults.clear();
        if (newResults != null) {
            this.analysisResults.addAll(newResults);
        }
        notifyDataSetChanged();
    }

    static class SpeakingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCategory, tvScore, tvPronunciation, tvRhythm, tvAccuracy;

        public SpeakingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSpeakingItemTitle);
            tvDate = itemView.findViewById(R.id.tvSpeakingDate);
            tvCategory = itemView.findViewById(R.id.tvSpeakingCategory);
            tvScore = itemView.findViewById(R.id.tvSpeakingScore);
            tvPronunciation = itemView.findViewById(R.id.tvPronunciation);
            tvRhythm = itemView.findViewById(R.id.tvRhythm);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);
        }
    }
}