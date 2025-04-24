package com.example.echoenglish_mobile.view.activity.video_youtube;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.response.TranscriptItem;

import java.util.List;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {
    private Context context;
    private List<TranscriptItem> transcriptItems;
    private OnTranscriptItemClickListener listener;

    public interface OnTranscriptItemClickListener {
        void onTranscriptItemClick(TranscriptItem item);
    }

    public TranscriptAdapter(Context context, List<TranscriptItem> transcriptItems, OnTranscriptItemClickListener listener) {
        this.context = context;
        this.transcriptItems = transcriptItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video_transcript, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TranscriptItem item = transcriptItems.get(position);
        holder.transcriptText.setText(item.getText());
        holder.timestamp.setText(item.getFormattedTime());

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTranscriptItemClick(item);
            }
        });

        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTranscriptItemClick(item);
            }
        });

        holder.btnTranslate.setOnClickListener(v -> {
            // Translation functionality would go here
        });
    }

    @Override
    public int getItemCount() {
        return transcriptItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView transcriptText, timestamp;
        ImageButton btnPlay, btnTranslate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transcriptText = itemView.findViewById(R.id.transcript_text);
            timestamp = itemView.findViewById(R.id.timestamp);
            btnPlay = itemView.findViewById(R.id.btn_play);
            btnTranslate = itemView.findViewById(R.id.btn_translate);
        }
    }
}