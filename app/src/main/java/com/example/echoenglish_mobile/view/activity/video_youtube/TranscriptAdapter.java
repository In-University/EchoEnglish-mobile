package com.example.echoenglish_mobile.view.activity.video_youtube;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Thêm để ví dụ

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.response.TranscriptItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {
    private Context context;
    private List<TranscriptItem> transcriptItems;
    private OnTranscriptItemClickListener itemClickListener;
    private OnWordClickListener wordClickListener;

    public interface OnTranscriptItemClickListener {
        void onTranscriptItemClick(TranscriptItem item);
    }

    public interface OnWordClickListener {
        void onWordClicked(String word);
    }

    public TranscriptAdapter(Context context, List<TranscriptItem> transcriptItems,
                             OnTranscriptItemClickListener itemClickListener,
                             OnWordClickListener wordClickListener) { // Thêm wordClickListener vào constructor
        this.context = context;
        this.transcriptItems = transcriptItems;
        this.itemClickListener = itemClickListener;
        this.wordClickListener = wordClickListener;
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
        holder.timestamp.setText(item.getFormattedTime());

        String fullText = item.getText();
        SpannableString spannableString = new SpannableString(fullText);
        Pattern wordPattern = Pattern.compile("\\b[a-zA-Z'-]+\\b");
        Matcher matcher = wordPattern.matcher(fullText);

        while (matcher.find()) {
            String word = matcher.group(0);
            int start = matcher.start();
            int end = matcher.end();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (wordClickListener != null) {
                        wordClickListener.onWordClicked(word);
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(
                    new ForegroundColorSpan(Color.BLACK),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        holder.transcriptText.setText(spannableString);
        holder.transcriptText.setMovementMethod(LinkMovementMethod.getInstance());
        holder.transcriptText.setHighlightColor(android.graphics.Color.TRANSPARENT);



        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onTranscriptItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transcriptItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView transcriptText, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transcriptText = itemView.findViewById(R.id.txtSubtitle);
            timestamp = itemView.findViewById(R.id.txtTimestamp);
        }
    }
}