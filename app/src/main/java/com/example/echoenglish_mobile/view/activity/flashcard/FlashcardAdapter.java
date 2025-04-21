package com.example.echoenglish_mobile.view.activity.flashcard;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private Context context;
    private List<FlashcardBasicResponse> flashcardList;
    private OnFlashcardClickListener listener;

    private OnFlashcardDeleteClickListener deleteListener;

    // Interface for delete click
    public interface OnFlashcardDeleteClickListener {
        void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position);
    }

    // Modify constructor to accept delete listener
    public FlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList,
                            OnFlashcardClickListener listener, OnFlashcardDeleteClickListener deleteListener) {
        this.context = context;
        this.flashcardList = flashcardList;
        this.listener = listener;
        this.deleteListener = deleteListener; // Assign delete listener
    }

    public interface OnFlashcardClickListener {
        void onFlashcardClick(FlashcardBasicResponse flashcard);
    }

    public FlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList, OnFlashcardClickListener listener) {
        this.context = context;
        this.flashcardList = flashcardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        FlashcardBasicResponse flashcard = flashcardList.get(position);
        holder.textViewName.setText(flashcard.getName());
        // Đảm bảo kiểm tra null cho creatorName
        holder.textViewCreator.setText("by " + (flashcard.getCreatorName() != null ? flashcard.getCreatorName() : "ID: " + flashcard.getCreatorId()));

        // *** KIỂM TRA KỸ ĐOẠN NÀY ***
        String imageUrl = flashcard.getImageUrl(); // Lấy URL ảnh

        // Kiểm tra xem ImageView có tồn tại và URL có hợp lệ không
        if (holder.imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context) // Context của Adapter
                    .load(imageUrl) // **Tải URL ảnh**
                    .placeholder(R.drawable.ic_placeholder_image) // Ảnh hiển thị khi đang tải
                    .error(R.drawable.ic_placeholder_image)       // Ảnh hiển thị khi lỗi (URL sai, không mạng,...)
                    .into(holder.imageView); // **Đặt ảnh vào ImageView mục tiêu**
        } else {
            // Nếu không có URL hoặc ImageView, đặt ảnh placeholder mặc định
            if(holder.imageView != null) {
                holder.imageView.setImageResource(R.drawable.ic_placeholder_image);
            }
        }
        // *** KẾT THÚC KIỂM TRA ***

        // Giữ lại các listener khác
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFlashcardClick(flashcard);
            }
        });

//        holder.buttonDelete.setOnClickListener(v -> {
//            if (deleteListener != null) {
//                deleteListener.onFlashcardDeleteClick(flashcard, holder.getAdapterPosition());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return flashcardList == null ? 0 : flashcardList.size();
    }

    // Cập nhật dữ liệu cho adapter
    public void updateData(List<FlashcardBasicResponse> newFlashcards) {
        this.flashcardList.clear();
        if (newFlashcards != null) {
            this.flashcardList.addAll(newFlashcards);
        }
        notifyDataSetChanged(); // Thông báo thay đổi toàn bộ list (có thể tối ưu hơn)
    }

    // Method to remove item after successful deletion
    public void removeItem(int position) {
        if (position >= 0 && position < flashcardList.size()) {
            flashcardList.remove(position);
            notifyItemRemoved(position);
            // Optional: notifyItemRangeChanged if positions below change meaning
            notifyItemRangeChanged(position, getItemCount());
        }
    }


    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewCreator;
        ImageButton buttonDelete;

        FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewFlashcard);
            textViewName = itemView.findViewById(R.id.textViewFlashcardName);
            textViewCreator = itemView.findViewById(R.id.textViewCreatorName);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteFlashcard);
        }
    }
}