package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;

import java.util.List;
import java.util.Locale;

public class SpeakingResultAdapter extends RecyclerView.Adapter<SpeakingResultAdapter.SpeakingViewHolder> {

    private final List<SpeakingResult> speakingResults;
    private final Context context;

    public SpeakingResultAdapter(Context context, List<SpeakingResult> speakingResults) {
        this.context = context;
        this.speakingResults = speakingResults;
    }

    @NonNull
    @Override
    public SpeakingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_speak_analyze_result, parent, false);
        return new SpeakingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpeakingViewHolder holder, int position) {
        SpeakingResult result = speakingResults.get(position);
        holder.tvTitle.setText(result.getTitle());
        holder.tvDate.setText(result.getDate());
        holder.tvCategory.setText(result.getCategory());
        holder.tvScore.setText(String.format(Locale.getDefault(), "%d%%", result.getOverallScore()));
        holder.tvPronunciation.setText(String.format(Locale.getDefault(), "%d%%", result.getPronunciationScore()));
        holder.tvRhythm.setText(String.format(Locale.getDefault(), "%d%%", result.getRhythmScore()));
        holder.tvAccuracy.setText(String.format(Locale.getDefault(), "%d%%", result.getAccuracyScore()));

        // Optional: Change score circle color based on value
        // Drawable scoreBg = ContextCompat.getDrawable(context, R.drawable.bg_score_circle);
        // if (scoreBg != null) {
        //     scoreBg.setTint(getScoreColor(result.getOverallScore())); // Implement getScoreColor logic
        //     holder.tvScore.setBackground(scoreBg);
        // }
    }

    @Override
    public int getItemCount() {
        return speakingResults.size();
    }

    // Optional: Helper method for score color
    // private int getScoreColor(int score) {
    //     if (score >= 85) return ContextCompat.getColor(context, R.color.score_high); // Define colors
    //     if (score >= 70) return ContextCompat.getColor(context, R.color.score_medium);
    //     else return ContextCompat.getColor(context, R.color.score_low);
    // }


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