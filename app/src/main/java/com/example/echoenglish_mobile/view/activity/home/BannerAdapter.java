// File: com.example.echoenglish_mobile.adapter.BannerAdapter.java
package com.example.echoenglish_mobile.view.activity.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<String> imageUrls;
    private Context context;

    public BannerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_banner_image.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_image, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.image_wainting_banner) // Thay placeholder nếu cần
                .error(R.drawable.ic_xml_launcher_foreground) // Thay error image nếu cần
                .into(holder.imageViewBanner);
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewBanner;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            // itemView là CardView, tìm ImageView bên trong nó
            imageViewBanner = itemView.findViewById(R.id.imageViewBanner);
        }
    }
}