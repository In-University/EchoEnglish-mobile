package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private Context context;
    private List<FlashcardBasicResponse> flashcardList;

    private OnFlashcardClickListener listener;
    private OnFlashcardDeleteClickListener deleteListener;
    private OnFlashcardEditClickListener editListener;

    public interface OnFlashcardClickListener {
        void onFlashcardClick(FlashcardBasicResponse flashcard);
    }

    public interface OnFlashcardDeleteClickListener {
        void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position);
    }

    public interface OnFlashcardEditClickListener {
        void onFlashcardEditClick(FlashcardBasicResponse flashcard, int position);
    }

    // Constructor hỗ trợ click, xoá, sửa
    public FlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList,
                            OnFlashcardClickListener listener,
                            OnFlashcardDeleteClickListener deleteListener,
                            OnFlashcardEditClickListener editListener) {
        this.context = context;
        this.flashcardList = new ArrayList<>(flashcardList);
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
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
        holder.textViewCreator.setText("by " +
                (flashcard.getCreatorName() != null ? flashcard.getCreatorName() : "ID: " + flashcard.getCreatorId()));

        String imageUrl = flashcard.getImageUrl();
        if (holder.imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(holder.imageView);
        } else if (holder.imageView != null) {
            holder.imageView.setImageResource(R.drawable.ic_placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFlashcardClick(flashcard);
            }
        });

        if (holder.buttonDelete != null) {
            holder.buttonDelete.setVisibility(deleteListener != null ? View.VISIBLE : View.GONE);
            holder.buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onFlashcardDeleteClick(flashcard, holder.getAdapterPosition());
                }
            });
        }

        if (holder.buttonEdit != null) {
            holder.buttonEdit.setVisibility(editListener != null ? View.VISIBLE : View.GONE);
            holder.buttonEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onFlashcardEditClick(flashcard, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return flashcardList == null ? 0 : flashcardList.size();
    }

    public void updateData(List<FlashcardBasicResponse> newFlashcards) {
        this.flashcardList.clear();
        if (newFlashcards != null) {
            this.flashcardList.addAll(newFlashcards);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < flashcardList.size()) {
            flashcardList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewCreator;
        ImageButton buttonDelete;
        ImageButton buttonEdit;

        FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewFlashcard);
            textViewName = itemView.findViewById(R.id.textViewFlashcardName);
            textViewCreator = itemView.findViewById(R.id.textViewCreatorName);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteFlashcard);
            buttonEdit = itemView.findViewById(R.id.buttonEditFlashcard);
        }
    }
}
