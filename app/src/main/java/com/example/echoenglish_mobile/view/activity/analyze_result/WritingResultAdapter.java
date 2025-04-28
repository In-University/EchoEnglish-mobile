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

public class WritingResultAdapter extends RecyclerView.Adapter<WritingResultAdapter.WritingViewHolder> {

    private final List<WritingResult> writingResults;
    private final Context context;

    public WritingResultAdapter(Context context, List<WritingResult> writingResults) {
        this.context = context;
        this.writingResults = writingResults;
    }

    @NonNull
    @Override
    public WritingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_writing_analyze_result, parent, false);
        return new WritingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WritingViewHolder holder, int position) {
        WritingResult result = writingResults.get(position);
        holder.tvTitle.setText(result.getTitle());
        holder.tvDate.setText(result.getDate());
        holder.tvType.setText(result.getType());
        // Use String.format for localization and clarity
        holder.tvWordCount.setText(String.format(Locale.getDefault(), "%d từ", result.getWordCount()));
    }

    @Override
    public int getItemCount() {
        return writingResults.size();
    }

    static class WritingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvType, tvWordCount;

        public WritingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWritingItemTitle);
            tvDate = itemView.findViewById(R.id.tvWritingDate);
            tvType = itemView.findViewById(R.id.tvWritingType);
            tvWordCount = itemView.findViewById(R.id.tvWordCount);
        }
    }
}