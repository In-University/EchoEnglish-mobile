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
import com.bumptech.glide.Glide; // Still needed for image loading
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient; // Still needed for ApiService
import com.example.echoenglish_mobile.network.ApiService; // Still needed for API call
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse; // Correct DTO for Flashcard Sets
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningProgressResponse; // Still needed for progress

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


// This adapter is intended to list Flashcard Sets (FlashcardBasicResponse)
// It should use the item layout that represents a Flashcard Set (item_flashcard_public.xml)
public class PublicFlashcardAdapter extends RecyclerView.Adapter<PublicFlashcardAdapter.FlashcardViewHolder> { // Renamed ViewHolder

    private Long currentUserId = SharedPrefManager.getInstance(MyApp.getAppContext()).getUserInfo().getId();
    private Context context;
    private List<FlashcardBasicResponse> flashcardList; // Correct list type
    // Updated interface to match Activity's listener implementation
    public interface OnPublicFlashcardClickListener {
        void onPublicFlashcardClick(FlashcardBasicResponse flashcard); // Listener for clicking a Flashcard Set
    }
    private OnPublicFlashcardClickListener listener;

    private ApiService apiService; // Keep ApiService for loading progress

    // Updated constructor
    public PublicFlashcardAdapter(Context context, List<FlashcardBasicResponse> flashcardList, OnPublicFlashcardClickListener listener) {
        this.context = context;
        this.flashcardList = flashcardList != null ? flashcardList : new ArrayList<>(); // Handle null list
        this.listener = listener;
        this.apiService = ApiClient.getApiService(); // Get ApiService
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the CORRECT item layout for a Flashcard Set
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_public, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        if (position < 0 || position >= flashcardList.size()) {
            Log.w("PublicFlashcardAdapter", "onBindViewHolder: Invalid position " + position + ". List size: " + flashcardList.size());
            return; // Avoid crashing on invalid position
        }
        FlashcardBasicResponse flashcard = flashcardList.get(position);

        holder.textFlashcardNumber.setText(String.format(Locale.getDefault(), "#%d", position + 1)); // Item number
        holder.textViewName.setText(flashcard.getName()); // Flashcard set name

        // Load image using Glide
        Glide.with(context)
                .load(flashcard.getImageUrl())
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(holder.imageView);

        // Removed lock icon update logic

        // Call API to load progress (Still needed per Flashcard Set)
        loadProgress(holder, flashcard.getId());


        // Set click listener on the whole item view (the MaterialCardView)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Pass the Flashcard Set item object
                int currentPosition = holder.getAdapterPosition(); // Get current position reliably
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < flashcardList.size()) {
                    listener.onPublicFlashcardClick(flashcardList.get(currentPosition)); // Updated call
                } else {
                    Log.w("PublicFlashcardAdapter", "Item click: Invalid adapter position or list size mismatch.");
                }
            }
        });
    }

    // Function to call API to get progress for each Flashcard Set
    private void loadProgress(FlashcardViewHolder holder, Long flashcardId) {
        // Reset UI before calling API
        holder.textViewVocabCount.setText("...");
        holder.progressBarCompletion.setProgress(0);
        holder.textViewProgressPercentage.setText("");
        holder.progressBarCompletion.setVisibility(View.INVISIBLE); // Hide small progress bar when loading
        holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);

        if (flashcardId == null) {
            Log.w("PublicFlashcardAdapter", "loadProgress: flashcardId is null, skipping API call.");
            holder.textViewVocabCount.setText("? words"); // Show default
            holder.progressBarCompletion.setVisibility(View.INVISIBLE);
            holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
            return;
        }

        // Add check to ensure holder is still bound to the correct item before making API call (optimization)
        // This requires storing the flashcardId in the holder or tagging the call, but for simplicity,
        // we rely on checking position/ID in the callback.

        apiService.getLearningProgress(currentUserId, flashcardId).enqueue(new Callback<LearningProgressResponse>() {
            @Override
            public void onResponse(Call<LearningProgressResponse> call, Response<LearningProgressResponse> response) {
                // Check if holder is still valid AND associated data matches before updating UI
                int currentPosition = holder.getAdapterPosition();
                // Add null checks for flashcardList and the item itself
                if (currentPosition == RecyclerView.NO_POSITION || flashcardList == null || currentPosition >= flashcardList.size() || flashcardList.get(currentPosition) == null || !flashcardList.get(currentPosition).getId().equals(flashcardId) ) {
                    Log.w("PublicFlashcardAdapter", "Progress response for old/invalid holder position or mismatched ID. Skipping UI update.");
                    return; // Avoid updating wrong or recycled views
                }

                if (response.isSuccessful() && response.body() != null) {
                    LearningProgressResponse progress = response.body();
                    holder.textViewVocabCount.setText(String.format(Locale.getDefault(), "%d words", progress.getTotalVocabularies())); // Translated
                    int percentage = (int) progress.getCompletionPercentage(); // Get integer part
                    holder.progressBarCompletion.setProgress(percentage);
                    holder.textViewProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
                    // Show progress bar and text again
                    holder.progressBarCompletion.setVisibility(View.VISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.VISIBLE);
                } else {
                    // Error or no progress data, keep "?" or show error
                    holder.textViewVocabCount.setText("? words"); // Translated
                    holder.progressBarCompletion.setVisibility(View.INVISIBLE);
                    holder.textViewProgressPercentage.setVisibility(View.INVISIBLE);
                    Log.w("PublicFlashcardAdapter", "Failed to get progress for " + flashcardId + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LearningProgressResponse> call, Throwable t) {
                // Network error, keep "?" or show error
                int currentPosition = holder.getAdapterPosition();
                // Add null checks for flashcardList and the item itself
                if (currentPosition == RecyclerView.NO_POSITION || flashcardList == null || currentPosition >= flashcardList.size() || flashcardList.get(currentPosition) == null || !flashcardList.get(currentPosition).getId().equals(flashcardId) ) {
                    Log.w("PublicFlashcardAdapter", "Progress failure for old/invalid holder position or mismatched ID. Skipping UI update.");
                    return; // Avoid updating wrong or recycled views
                }
                holder.textViewVocabCount.setText("? words"); // Translated
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

    // Method to update the adapter data
    public void updateData(List<FlashcardBasicResponse> newFlashcards) {
        this.flashcardList = newFlashcards != null ? newFlashcards : new ArrayList<>(); // Handle null list
        // Using notifyDataSetChanged is simple but less efficient than DiffUtil for updates.
        // For simplicity here, we keep it as is, but recommend DiffUtil for better performance.
        notifyDataSetChanged();
    }


    // ViewHolder - Matches the item_flashcard_public.xml structure
    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textFlashcardNumber, textViewName, textViewVocabCount, textViewProgressPercentage;
        ImageView imageView;
        // Removed imageViewLockStatus
        ProgressBar progressBarCompletion;

        FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textFlashcardNumber = itemView.findViewById(R.id.textFlashcardNumber);
            textViewName = itemView.findViewById(R.id.textViewFlashcardName);
            textViewVocabCount = itemView.findViewById(R.id.textViewVocabCount);
            textViewProgressPercentage = itemView.findViewById(R.id.textViewProgressPercentage);
            imageView = itemView.findViewById(R.id.imageViewFlashcard);
            // Removed imageViewLockStatus = itemView.findViewById(R.id.imageViewLockStatus);
            progressBarCompletion = itemView.findViewById(R.id.progressBarCompletion);
        }
    }
}