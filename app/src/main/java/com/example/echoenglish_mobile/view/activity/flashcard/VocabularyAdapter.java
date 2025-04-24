package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements Filterable {

    private Context context;
    private List<VocabularyResponse> vocabularyListFull;
    private List<VocabularyResponse> vocabularyListFiltered;
    private OnVocabularyActionsListener actionsListener;

    public interface OnVocabularyActionsListener {
        void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition);
        void onVocabularyItemClick(VocabularyResponse vocabulary); // Click để sửa
    }

    public VocabularyAdapter(Context context, List<VocabularyResponse> vocabularyList, OnVocabularyActionsListener listener) {
        this.context = context;
        this.vocabularyListFull = new ArrayList<>(vocabularyList != null ? vocabularyList : new ArrayList<>());
        this.vocabularyListFiltered = new ArrayList<>(this.vocabularyListFull);
        this.actionsListener = listener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        if (position < 0 || position >= vocabularyListFiltered.size()) return;
        VocabularyResponse vocab = vocabularyListFiltered.get(position);

        holder.textViewWord.setText(vocab.getWord());
        holder.textViewDefinition.setText(vocab.getDefinition());

        if (vocab.getPhonetic() != null && !vocab.getPhonetic().isEmpty()) {
            holder.textViewPhonetic.setText(vocab.getPhonetic());
            holder.textViewPhonetic.setVisibility(View.VISIBLE);
        } else {
            holder.textViewPhonetic.setVisibility(View.GONE);
        }

        if (vocab.getExample() != null && !vocab.getExample().isEmpty()) {
            holder.textViewExample.setText("Ví dụ: " + vocab.getExample());
            holder.textViewExample.setVisibility(View.VISIBLE);
        } else {
            holder.textViewExample.setVisibility(View.GONE);
        }

        // Xử lý click nút Sửa
        if (holder.buttonEdit != null) {
            holder.buttonEdit.setOnClickListener(v -> {
                if (actionsListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                        actionsListener.onVocabularyItemClick(vocabularyListFiltered.get(currentPosition));
                    }
                }
            });
            // Chỉ hiển thị nút sửa nếu listener tồn tại (Activity truyền listener vào)
            holder.buttonEdit.setVisibility(actionsListener != null ? View.VISIBLE : View.GONE);
        }


        // Xử lý click nút Xóa
        if (holder.buttonDelete != null) {
            holder.buttonDelete.setOnClickListener(v -> {
                if (actionsListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                        VocabularyResponse itemToDelete = vocabularyListFiltered.get(currentPosition);
                        int originalPosition = findPositionInFullList(itemToDelete.getId());
                        if (originalPosition != -1) {
                            actionsListener.onVocabularyDeleteClick(itemToDelete, originalPosition);
                        } else {
                            Log.w("VocabularyAdapter", "Could not find original position for delete: ID " + itemToDelete.getId());
                            // Fallback: Gọi với vị trí hiện tại, Activity cần xử lý tìm lại
                            // actionsListener.onVocabularyDeleteClick(itemToDelete, currentPosition);
                        }
                    }
                }
            });
            // Chỉ hiển thị nút xóa nếu listener tồn tại
            holder.buttonDelete.setVisibility(actionsListener != null ? View.VISIBLE : View.GONE);
        }

        // Xử lý click vào vùng text (word/phonetic) để mở màn hình sửa
        // Thay vì đặt listener vào itemView, đặt vào LinearLayout chứa text
        if (holder.clickableArea != null) {
            holder.clickableArea.setOnClickListener(v -> {
                if (actionsListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                        actionsListener.onVocabularyItemClick(vocabularyListFiltered.get(currentPosition));
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return vocabularyListFiltered == null ? 0 : vocabularyListFiltered.size();
    }

    public void updateData(List<VocabularyResponse> newVocabularies) {
        if (newVocabularies != null) {
            this.vocabularyListFull = new ArrayList<>(newVocabularies);
            this.vocabularyListFiltered = new ArrayList<>(newVocabularies);
        } else {
            this.vocabularyListFull.clear();
            this.vocabularyListFiltered.clear();
        }
        notifyDataSetChanged();
    }

    public void removeItem(int originalPosition) {
        if (originalPosition >= 0 && originalPosition < vocabularyListFull.size()) {
            VocabularyResponse removedItem = vocabularyListFull.remove(originalPosition);
            int filteredPosition = findPositionInFilteredList(removedItem.getId());
            if (filteredPosition != -1) {
                vocabularyListFiltered.remove(filteredPosition);
                notifyItemRemoved(filteredPosition);
                notifyItemRangeChanged(filteredPosition, vocabularyListFiltered.size());
            }
        }
    }

    public VocabularyResponse getItem(int position) {
        if (position >= 0 && position < vocabularyListFiltered.size()) {
            return vocabularyListFiltered.get(position);
        }
        return null;
    }

    public List<VocabularyResponse> getFullList() {
        return vocabularyListFull;
    }

    private int findPositionInFullList(Long vocabId) {
        if (vocabId == null || vocabularyListFull == null) return -1;
        for (int i = 0; i < vocabularyListFull.size(); i++) {
            if (vocabId.equals(vocabularyListFull.get(i).getId())) return i;
        }
        return -1;
    }

    private int findPositionInFilteredList(Long vocabId) {
        if (vocabId == null || vocabularyListFiltered == null) return -1;
        for (int i = 0; i < vocabularyListFiltered.size(); i++) {
            if (vocabId.equals(vocabularyListFiltered.get(i).getId())) return i;
        }
        return -1;
    }

    @Override
    public Filter getFilter() {
        return vocabularyFilter;
    }

    private Filter vocabularyFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<VocabularyResponse> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0 || vocabularyListFull == null) {
                filteredList.addAll(vocabularyListFull != null ? vocabularyListFull : new ArrayList<>());
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                for (VocabularyResponse item : vocabularyListFull) {
                    if ((item.getWord() != null && item.getWord().toLowerCase(Locale.getDefault()).contains(filterPattern)) ||
                            (item.getDefinition() != null && item.getDefinition().toLowerCase(Locale.getDefault()).contains(filterPattern))) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            vocabularyListFiltered.clear();
            if (results.values != null) {
                vocabularyListFiltered.addAll((List<VocabularyResponse>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord, textViewPhonetic, textViewDefinition, textViewExample;
        ImageButton buttonDelete, buttonEdit; // Thêm buttonEdit
        LinearLayout clickableArea; // Vùng text có thể click

        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWord = itemView.findViewById(R.id.textViewWord);
            textViewPhonetic = itemView.findViewById(R.id.textViewPhonetic);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewExample = itemView.findViewById(R.id.textViewExample);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteVocabulary);
            buttonEdit = itemView.findViewById(R.id.buttonEditVocabulary); // Tìm nút sửa
            clickableArea = itemView.findViewById(R.id.layout_vocab_clickable_area); // Tìm vùng click
        }
    }
}