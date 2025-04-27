package com.example.echoenglish_mobile.view.activity.flashcard;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient; // Import ApiClient
import com.example.echoenglish_mobile.network.ApiService; // Import ApiService
import com.example.echoenglish_mobile.util.PurchaseManager; // Import PurchaseManager
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningProgressResponse;

import java.util.List;
import java.util.Locale; // Import Locale

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PublicFlashcardAdapter extends RecyclerView.Adapter<PublicFlashcardAdapter.PublicFlashcardViewHolder> {

    private static final long CURRENT_USER_ID = 27L; // Hardcoded user ID
    private Context context;
    private List<FlashcardBasicResponse> flashcardList;
    private OnPublicFlashcardClickListener listener;
    private PurchaseManager purchaseManager;
    private ApiService apiService; // Thêm ApiService

    public interface OnPublicFlashcardClickListener {
        void onPublicFlashcardClick(FlashcardBasicResponse flashcard, boolean isPurchased);
    }



    public PublicFlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList, OnPublicFlashcardClickListener listener) {
        this.context = context;
        this.flashcardList = flashcardList;
        this.listener = listener;
        this.purchaseManager = new PurchaseManager(context); // Khởi tạo PurchaseManager
        this.apiService = ApiClient.getApiService(); // Lấy ApiService
    }

    @NonNull
    @Override
    public PublicFlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_public, parent, false);
        return new PublicFlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicFlashcardViewHolder holder, int position) {
        FlashcardBasicResponse flashcard = flashcardList.get(position);
        boolean isPurchased = purchaseManager.isPurchased(flashcard.getId());

        holder.textFlashcardNumber.setText(String.format(Locale.getDefault(), "#%d", position + 1));
        holder.textViewName.setText(flashcard.getName());
        // holder.textViewVocabCount.setText("? từ"); // Sẽ cập nhật khi có progress
        // holder.progressBarCompletion.setProgress(0); // Reset progress
        // holder.textViewProgressPercentage.setText("0%"); // Reset progress text

        Glide.with(context)
                .load(flashcard.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(holder.imageView);

        // Cập nhật icon khóa/mở
        holder.imageViewLockStatus.setImageResource(isPurchased ? R.drawable.ic_xml_lock_open_24px : R.drawable.ic_xml_lock_24px);

        // Gọi API lấy tiến độ
        loadProgress(holder, flashcard.getId());


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPublicFlashcardClick(flashcard, isPurchased);
            }
        });
    }

    // Hàm gọi API lấy tiến độ cho từng item
    private void loadProgress(PublicFlashcardViewHolder holder, Long flashcardId) {
        // Reset UI trước khi gọi API
        holder.textViewVocabCount.setText("...");
        holder.progressBarCompletion.setProgress(0);
        holder.textViewProgressPercentage.setText("");
        holder.progressBarCompletion.setVisibility(View.INVISIBLE); // Ẩn progress bar nhỏ khi đang load
        holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);

        if (flashcardId == null) return;

        apiService.getLearningProgress(CURRENT_USER_ID, flashcardId).enqueue(new Callback<LearningProgressResponse>() {
            @Override
            public void onResponse(Call<LearningProgressResponse> call, Response<LearningProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) { // Kiểm tra holder còn hợp lệ
                    LearningProgressResponse progress = response.body();
                    holder.textViewVocabCount.setText(String.format(Locale.getDefault(), "%d từ", progress.getTotalVocabularies()));
                    int percentage = (int) progress.getCompletionPercentage(); // Lấy phần nguyên
                    holder.progressBarCompletion.setProgress(percentage);
                    holder.textViewProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
                    // Hiển thị lại progress bar và text
                    holder.progressBarCompletion.setVisibility(View.VISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.VISIBLE);
                } else {
                    // Lỗi hoặc không có dữ liệu progress, giữ nguyên "?" hoặc hiển thị lỗi
                    holder.textViewVocabCount.setText("? từ");
                    holder.progressBarCompletion.setVisibility(View.INVISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
                    Log.w("PublicFlashcardAdapter", "Failed to get progress for " + flashcardId + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LearningProgressResponse> call, Throwable t) {
                // Lỗi mạng, giữ nguyên "?" hoặc hiển thị lỗi
                holder.textViewVocabCount.setText("? từ");
                holder.progressBarCompletion.setVisibility(View.INVISIBLE);
                holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
                Log.e("PublicFlashcardAdapter", "Error getting progress for " + flashcardId, t);
            }
        });
    }


    @Override
    public int getItemCount() {
        return flashcardList == null ? 0 : flashcardList.size();
    }

    public void updateData(List<FlashcardBasicResponse> newFlashcards) {
        this.flashcardList = newFlashcards;
        notifyDataSetChanged(); // Nên dùng DiffUtil
    }

    public List<FlashcardBasicResponse> getCurrentList() {
        return this.flashcardList;
    }


    // ViewHolder
    static class PublicFlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textFlashcardNumber, textViewName, textViewVocabCount, textViewProgressPercentage;
        ImageView imageView, imageViewLockStatus;
        ProgressBar progressBarCompletion;

        PublicFlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textFlashcardNumber = itemView.findViewById(R.id.textFlashcardNumber);
            textViewName = itemView.findViewById(R.id.textViewFlashcardName);
            textViewVocabCount = itemView.findViewById(R.id.textViewVocabCount);
            textViewProgressPercentage = itemView.findViewById(R.id.textViewProgressPercentage);
            imageView = itemView.findViewById(R.id.imageViewFlashcard);
            imageViewLockStatus = itemView.findViewById(R.id.imageViewLockStatus);
            progressBarCompletion = itemView.findViewById(R.id.progressBarCompletion);
        }
    }
}