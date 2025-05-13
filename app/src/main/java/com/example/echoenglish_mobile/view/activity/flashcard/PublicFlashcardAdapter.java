package com.example.echoenglish_mobile.view.activity.flashcard;

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
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningProgressResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PublicFlashcardAdapter extends RecyclerView.Adapter<PublicFlashcardAdapter.FlashcardViewHolder> {

    private Long currentUserId = SharedPrefManager.getInstance(MyApp.getAppContext()).getUserInfo().getId();
    private Context context;
    private List<FlashcardBasicResponse> flashcardList;
    public interface OnPublicFlashcardClickListener {
        void onPublicFlashcardClick(FlashcardBasicResponse flashcard);
    }
    private OnPublicFlashcardClickListener listener;

    private ApiService apiService;

    public PublicFlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList, OnPublicFlashcardClickListener listener) {
        this.context = context;
        this.flashcardList = flashcardList != null ? flashcardList : new ArrayList<>();
        this.listener = listener;
        this.apiService = ApiClient.getApiService();
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_public, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        if (position < 0 || position >= flashcardList.size()) {
            Log.w("PublicFlashcardAdapter", "onBindViewHolder: Invalid position " + position + ". List size: " + flashcardList.size());
            return;
        }
        FlashcardBasicResponse flashcard = flashcardList.get(position);

        holder.textFlashcardNumber.setText(String.format(Locale.getDefault(), "#%d", position + 1));
        holder.textViewName.setText(flashcard.getName());

        Glide.with(context)
                .load(flashcard.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(holder.imageView);


        loadProgress(holder, flashcard.getId());


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < flashcardList.size()) {
                    listener.onPublicFlashcardClick(flashcardList.get(currentPosition));
                } else {
                    Log.w("PublicFlashcardAdapter", "Item click: Invalid adapter position or list size mismatch.");
                }
            }
        });
    }

    private void loadProgress(FlashcardViewHolder holder, Long flashcardId) {
        holder.textViewVocabCount.setText("...");
        holder.progressBarCompletion.setProgress(0);
        holder.textViewProgressPercentage.setText("");
        holder.progressBarCompletion.setVisibility(View.INVISIBLE);
        holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);

        if (flashcardId == null) {
            Log.w("PublicFlashcardAdapter", "loadProgress: flashcardId is null, skipping API call.");
            holder.textViewVocabCount.setText("? words");
            holder.progressBarCompletion.setVisibility(View.INVISIBLE);
            holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
            return;
        }


        apiService.getLearningProgress(currentUserId, flashcardId).enqueue(new Callback<LearningProgressResponse>() {
            @Override
            public void onResponse(Call<LearningProgressResponse> call, Response<LearningProgressResponse> response) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || flashcardList == null || currentPosition >= flashcardList.size() || flashcardList.get(currentPosition) == null || !flashcardList.get(currentPosition).getId().equals(flashcardId) ) {
                    Log.w("PublicFlashcardAdapter", "Progress response for old/invalid holder position or mismatched ID. Skipping UI update.");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    LearningProgressResponse progress = response.body();
                    holder.textViewVocabCount.setText(String.format(Locale.getDefault(), "%d words", progress.getTotalVocabularies()));
                    int percentage = (int) progress.getCompletionPercentage();
                    holder.progressBarCompletion.setProgress(percentage);
                    holder.textViewProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
                    holder.progressBarCompletion.setVisibility(View.VISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.VISIBLE);
                } else {
                    holder.textViewVocabCount.setText("? words");
                    holder.progressBarCompletion.setVisibility(View.INVISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
                    Log.w("PublicFlashcardAdapter", "Failed to get progress for " + flashcardId + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LearningProgressResponse> call, Throwable t) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || flashcardList == null || currentPosition >= flashcardList.size() || flashcardList.get(currentPosition) == null || !flashcardList.get(currentPosition).getId().equals(flashcardId) ) {
                    Log.w("PublicFlashcardAdapter", "Progress failure for old/invalid holder position or mismatched ID. Skipping UI update.");
                    return;
                }
                holder.textViewVocabCount.setText("? words");
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
        this.flashcardList = newFlashcards != null ? newFlashcards : new ArrayList<>();
        notifyDataSetChanged();
    }


    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textFlashcardNumber, textViewName, textViewVocabCount, textViewProgressPercentage;
        ImageView imageView;
        ProgressBar progressBarCompletion;

        FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textFlashcardNumber = itemView.findViewById(R.id.textFlashcardNumber);
            textViewName = itemView.findViewById(R.id.textViewFlashcardName);
            textViewVocabCount = itemView.findViewById(R.id.textViewVocabCount);
            textViewProgressPercentage = itemView.findViewById(R.id.textViewProgressPercentage);
            imageView = itemView.findViewById(R.id.imageViewFlashcard);
            progressBarCompletion = itemView.findViewById(R.id.progressBarCompletion);
        }
    }
}