package com.example.echoenglish_mobile.view.activity.document_hub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.document_hub.dto.VideoItem;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoItem> videoList;
    private OnVideoItemClickListener listener;

    public interface OnVideoItemClickListener {
        void onVideoClick(VideoItem videoItem);
    }

    public VideoAdapter(Context context, List<VideoItem> videoList, OnVideoItemClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem videoItem = videoList.get(position);
        holder.bind(videoItem, listener);
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnailImageView;
        TextView badgeTextView;
        TextView durationTextView;
        TextView videoTitleTextView;
        TextView lessonInfoTextView;
        TextView levelInfoTextView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnailImageView = itemView.findViewById(R.id.videoThumbnailImageView);
            badgeTextView = itemView.findViewById(R.id.badgeTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            videoTitleTextView = itemView.findViewById(R.id.videoTitleTextView);
            lessonInfoTextView = itemView.findViewById(R.id.lessonInfoTextView);
            levelInfoTextView = itemView.findViewById(R.id.levelInfoTextView);
        }

        public void bind(final VideoItem videoItem, final OnVideoItemClickListener listener) {
            videoTitleTextView.setText(videoItem.getTitle());
            badgeTextView.setText(videoItem.getBadgeText());
            durationTextView.setText(videoItem.getDuration());
            lessonInfoTextView.setText(videoItem.getLessonInfo());
            levelInfoTextView.setText(videoItem.getLevelInfo());

            Glide.with(itemView.getContext())
                    .load(videoItem.getThumbnailUrl())
                    .placeholder(R.drawable.background_part_of_speech)
                    .error(R.drawable.ic_computer)
                    .into(videoThumbnailImageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoClick(videoItem);
                }
            });
        }
    }
}