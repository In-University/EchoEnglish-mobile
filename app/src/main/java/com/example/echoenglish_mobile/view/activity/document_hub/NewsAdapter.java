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
import com.example.echoenglish_mobile.view.activity.document_hub.dto.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsItem> newsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NewsItem newsItem);
    }

    public NewsAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.newsList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem currentItem = newsList.get(position);
        holder.bind(currentItem, listener);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void addNewsItems(List<NewsItem> newItems) {
        int startPosition = newsList.size();
        newsList.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    public void clearItems() {
        int size = newsList.size();
        newsList.clear();
        notifyItemRangeRemoved(0, size);
    }


    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTime, newsSource, newsHeadline;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTime = itemView.findViewById(R.id.newsTime);
            newsSource = itemView.findViewById(R.id.newsSource);
            newsHeadline = itemView.findViewById(R.id.newsHeadline);
        }

        public void bind(final NewsItem newsItem, final OnItemClickListener listener) {
            newsHeadline.setText(newsItem.getTitle());
            newsSource.setText(newsItem.getSource() != null ? newsItem.getSource().toUpperCase() : "UNKNOWN SOURCE");
            newsTime.setText(newsItem.getFormattedTimeAgo());

            String imageUrl = newsItem.getImageUrl();
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image_background_intro)
                    .error(R.drawable.image_background_intro)
                    .centerCrop()
                    .into(newsImage);

            itemView.setOnClickListener(v -> listener.onItemClick(newsItem));
        }
    }
}
