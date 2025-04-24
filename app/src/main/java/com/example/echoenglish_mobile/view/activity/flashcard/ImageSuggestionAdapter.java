package com.example.echoenglish_mobile.view.activity.flashcard;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsPhoto;

import java.util.ArrayList; // Import ArrayList
import java.util.List;

public class ImageSuggestionAdapter extends RecyclerView.Adapter<ImageSuggestionAdapter.ImageViewHolder> {

    private Context context;
    private List<PexelsPhoto> imageList; // Dùng PexelsPhoto
    private OnImageSelectedListener listener;

    public interface OnImageSelectedListener {
        void onImageSelected(PexelsPhoto image); // Truyền PexelsPhoto
    }

    public ImageSuggestionAdapter(Context context, List<PexelsPhoto> imageList, OnImageSelectedListener listener) {
        this.context = context;
        this.imageList = imageList != null ? imageList : new ArrayList<>(); // Khởi tạo an toàn
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_suggestion, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position < 0 || position >= imageList.size()) return; // Kiểm tra bounds
        PexelsPhoto image = imageList.get(position);

        String imageUrl = null;
        if (image.getSrc() != null) {
            imageUrl = image.getSrc().getTiny(); // Ưu tiên ảnh nhỏ nhất cho thumbnail
            if (imageUrl == null || imageUrl.isEmpty()) imageUrl = image.getSrc().getSmall();
            if (imageUrl == null || imageUrl.isEmpty()) imageUrl = image.getSrc().getMedium(); // Fallback
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(holder.imageView);

        // Hiển thị tên tác giả nếu có view và dữ liệu
        if (holder.textViewPhotographer != null) {
            if (image.getPhotographer() != null && !image.getPhotographer().isEmpty()) {
                holder.textViewPhotographer.setText(image.getPhotographer());
                holder.textViewPhotographer.setVisibility(View.VISIBLE);
            } else {
                holder.textViewPhotographer.setVisibility(View.GONE);
            }
        }

//        holder.itemView.setOnClickListener(v -> { // Listener đặt trên itemView (toàn bộ item)
        holder.imageView.setOnClickListener(v -> { // Đặt listener vào ImageView
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    PexelsPhoto clickedImage = imageList.get(currentPosition); // Lấy lại ảnh đúng vị trí
                    Log.d("ImageAdapter", "ImageView clicked at position: " + currentPosition + ", Image ID: " + clickedImage.getId());
                    listener.onImageSelected(clickedImage);
                }
            } else {
                Log.w("ImageAdapter", "Listener is null for position: " + holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList == null ? 0 : imageList.size();
    }

    public void updateData(List<PexelsPhoto> newImages) { // Nhận PexelsPhoto
        this.imageList = newImages != null ? newImages : new ArrayList<>();
        notifyDataSetChanged(); // Cần DiffUtil
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewPhotographer; // Tham chiếu TextView tác giả

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewSuggestion);
            textViewPhotographer = itemView.findViewById(R.id.textViewPhotographer); // Tìm TextView
        }
    }
}