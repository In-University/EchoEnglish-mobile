package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements Filterable {

    private static final String TAG = "VocabularyAdapter"; // Use a TAG here
    private Context context;
    private List<VocabularyResponse> vocabularyListFull;
    private List<VocabularyResponse> vocabularyListFiltered;
    private OnVocabularyActionsListener actionsListener;
    private boolean isPublicContext; // <-- Thêm field này

    public interface OnVocabularyActionsListener {
        // Listener method signature remains the same for simplicity
        void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition);
        void onVocabularyItemClick(VocabularyResponse vocabulary); // Click để sửa (hoặc xem chi tiết ở chế độ công khai)
    }

    // <-- Constructor mới nhận flag isPublicContext -->
    public VocabularyAdapter(Context context, List<VocabularyResponse> vocabularyList, OnVocabularyActionsListener listener, boolean isPublicContext) {
        this.context = context;
        this.vocabularyListFull = new ArrayList<>(vocabularyList != null ? vocabularyList : new ArrayList<>());
        this.vocabularyListFiltered = new ArrayList<>(this.vocabularyListFull);
        this.actionsListener = listener;
        this.isPublicContext = isPublicContext; // <-- Lưu flag
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_vocabulary.xml như đã thống nhất
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        if (position < 0 || position >= vocabularyListFiltered.size()) {
            Log.w(TAG, "onBindViewHolder: Invalid position " + position + ". List size: " + vocabularyListFiltered.size());
            return; // Avoid crashing on invalid position
        }
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
            holder.textViewExample.setText("Example: " + vocab.getExample()); // Translated prefix
            holder.textViewExample.setVisibility(View.VISIBLE);
        } else {
            holder.textViewExample.setVisibility(View.GONE);
        }

        // <-- Thiết lập visibility cho nút Sửa/Xóa dựa vào ngữ cảnh -->
        holder.buttonEdit.setVisibility(isPublicContext ? View.GONE : View.VISIBLE);
        holder.buttonDelete.setVisibility(isPublicContext ? View.GONE : View.VISIBLE);
        // --> Kết thúc thiết lập visibility <--


        // Xử lý click nút Sửa (chỉ active khi nút hiển thị)
        // Listener gọi onVocabularyItemClick (Activity sẽ quyết định có cho sửa hay không)
        if (holder.buttonEdit != null) {
            if (!isPublicContext) { // Chỉ set listener nếu không ở chế độ công khai
                holder.buttonEdit.setOnClickListener(v -> {
                    if (actionsListener != null) {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                            actionsListener.onVocabularyItemClick(vocabularyListFiltered.get(currentPosition));
                        } else {
                            Log.w(TAG, "Edit click: Invalid adapter position or list size mismatch.");
                        }
                    }
                });
            } else { // Nếu là công khai, loại bỏ listener (hoặc set null) để đảm bảo không click được
                holder.buttonEdit.setOnClickListener(null);
            }
        }


        // Xử lý click nút Xóa (chỉ active khi nút hiển thị)
        // Listener gọi onVocabularyDeleteClick (Activity sẽ quyết định có cho xóa hay không)
        if (holder.buttonDelete != null) {
            if (!isPublicContext) { // Chỉ set listener nếu không ở chế độ công khai
                holder.buttonDelete.setOnClickListener(v -> {
                    if (actionsListener != null) {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                            VocabularyResponse itemToDelete = vocabularyListFiltered.get(currentPosition);
                            // Tìm vị trí gốc trong list đầy đủ
                            int originalPosition = findPositionInFullList(itemToDelete.getId());
                            // Truyền cả item và vị trí gốc để Activity xử lý xóa đúng
                            if (originalPosition != -1) {
                                actionsListener.onVocabularyDeleteClick(itemToDelete, originalPosition);
                            } else {
                                Log.w(TAG, "Could not find original position for delete: ID " + itemToDelete.getId() + ". Item not in full list?");
                                // Fallback: Gọi với vị trí hiện tại (adapter position). Activity cần xử lý tìm lại hoặc reload.
                                actionsListener.onVocabularyDeleteClick(itemToDelete, currentPosition);
                            }
                        } else {
                            Log.w(TAG, "Delete click: Invalid adapter position or list size mismatch.");
                        }
                    }
                });
            } else { // Nếu là công khai, loại bỏ listener
                holder.buttonDelete.setOnClickListener(null);
            }
        }

        // Xử lý click vào vùng text (Word/Phonetic).
        // Listener gọi onVocabularyItemClick (Activity sẽ quyết định có cho sửa/xem chi tiết hay không)
        if (holder.clickableArea != null) {
            // Không cần kiểm tra isPublicContext ở đây, Activity sẽ xử lý logic dựa trên ngữ cảnh của nó
            holder.clickableArea.setOnClickListener(v -> {
                if (actionsListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                        actionsListener.onVocabularyItemClick(vocabularyListFiltered.get(currentPosition));
                    } else {
                        Log.w(TAG, "Text area click: Invalid adapter position or list size mismatch.");
                    }
                }
            });
        }
        // Note: Card view has clickable=false in XML, so holder.itemView listener is not needed here.
        // Clicks are handled by the layout_vocab_clickable_area.
    }

    @Override
    public int getItemCount() {
        return vocabularyListFiltered == null ? 0 : vocabularyListFiltered.size();
    }

    // Updates both full and filtered lists
    public void updateData(List<VocabularyResponse> newVocabularies) {
        if (newVocabularies != null) {
            this.vocabularyListFull = new ArrayList<>(newVocabularies);
            this.vocabularyListFiltered = new ArrayList<>(newVocabularies);
        } else {
            this.vocabularyListFull.clear();
            this.vocabularyListFiltered.clear();
        }
        // For simplicity, using notifyDataSetChanged. Consider DiffUtil for performance.
        notifyDataSetChanged();
    }

    // Removes an item from BOTH lists and notifies adapter
    public void removeItem(int originalPositionInFullList) {
        if (originalPositionInFullList >= 0 && originalPositionInFullList < vocabularyListFull.size()) {
            VocabularyResponse removedItem = vocabularyListFull.get(originalPositionInFullList); // Get item first
            vocabularyListFull.remove(originalPositionInFullList); // Remove from full list

            // Now find and remove from the filtered list if it exists
            int filteredPosition = findPositionInFilteredList(removedItem.getId());
            if (filteredPosition != -1) {
                vocabularyListFiltered.remove(filteredPosition);
                // Notify view based on filtered list position
                notifyItemRemoved(filteredPosition);
                // Notify adjacent items to correct positions after removal
                notifyItemRangeChanged(filteredPosition, vocabularyListFiltered.size());
            }
            // If item wasn't in filtered list, only removed from full list, no UI update needed for filtered view.
        } else {
            Log.w(TAG, "removeItem: Invalid original position provided: " + originalPositionInFullList + ". Full list size: " + (vocabularyListFull != null ? vocabularyListFull.size() : "null"));
            // If position is invalid, adapter state might be inconsistent.
            // Calling Activity should consider a full reload.
        }
    }


    // Gets an item from the FILTERED list by position
    public VocabularyResponse getItem(int position) {
        if (position >= 0 && position < vocabularyListFiltered.size()) {
            return vocabularyListFiltered.get(position);
        }
        return null;
    }

    // Gets the FULL (unfiltered) list
    public List<VocabularyResponse> getFullList() {
        return vocabularyListFull;
    }

    // Finds position in the FULL list by ID
    private int findPositionInFullList(Long vocabId) {
        if (vocabId == null || vocabularyListFull == null) return -1;
        for (int i = 0; i < vocabularyListFull.size(); i++) {
            VocabularyResponse vocab = vocabularyListFull.get(i);
            if (vocab != null && vocabId.equals(vocab.getId())) return i;
        }
        return -1;
    }

    // Finds position in the FILTERED list by ID
    private int findPositionInFilteredList(Long vocabId) {
        if (vocabId == null || vocabularyListFiltered == null) return -1;
        for (int i = 0; i < vocabularyListFiltered.size(); i++) {
            VocabularyResponse vocab = vocabularyListFiltered.get(i);
            if (vocab != null && vocabId.equals(vocab.getId())) return i;
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
                    boolean matches = false;
                    if (item != null) { // Null check for item
                        // Check word, definition, phonetic, and example for the filter pattern
                        if (item.getWord() != null && item.getWord().toLowerCase(Locale.getDefault()).contains(filterPattern)) matches = true;
                        if (!matches && item.getDefinition() != null && item.getDefinition().toLowerCase(Locale.getDefault()).contains(filterPattern)) matches = true;
                        if (!matches && item.getPhonetic() != null && item.getPhonetic().toLowerCase(Locale.getDefault()).contains(filterPattern)) matches = true;
                        if (!matches && item.getExample() != null && item.getExample().toLowerCase(Locale.getDefault()).contains(filterPattern)) matches = true;
                    }

                    if (matches) {
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
            notifyDataSetChanged(); // Update the RecyclerView
        }
    };


    // ViewHolder - Ánh xạ các View từ item_vocabulary.xml
    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord, textViewPhonetic, textViewDefinition, textViewExample;
        ImageButton buttonDelete, buttonEdit;
        LinearLayout clickableArea; // Vùng text có thể click

        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWord = itemView.findViewById(R.id.textViewWord);
            textViewPhonetic = itemView.findViewById(R.id.textViewPhonetic);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewExample = itemView.findViewById(R.id.textViewExample);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteVocabulary);
            buttonEdit = itemView.findViewById(R.id.buttonEditVocabulary);
            clickableArea = itemView.findViewById(R.id.layout_vocab_clickable_area);
        }
    }
}