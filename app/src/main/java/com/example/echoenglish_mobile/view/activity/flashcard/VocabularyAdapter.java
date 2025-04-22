package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Filter; // Import Filter
import android.widget.Filterable; // Import Filterable
import java.util.Locale;

// Implement Filterable
public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements Filterable {

    private Context context;
    private List<VocabularyResponse> vocabularyListFull; // Danh sách gốc
    private List<VocabularyResponse> vocabularyListFiltered; // Danh sách hiển thị (đã lọc)
    private OnVocabularyDeleteClickListener deleteListener;

    public interface OnVocabularyDeleteClickListener {
        void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition); // Truyền vị trí gốc
    }

    public VocabularyAdapter(Context context, List<VocabularyResponse> vocabularyList, OnVocabularyDeleteClickListener deleteListener) {
        this.context = context;
        this.vocabularyListFull = new ArrayList<>(vocabularyList != null ? vocabularyList : new ArrayList<>());
        this.vocabularyListFiltered = new ArrayList<>(this.vocabularyListFull);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        // Luôn lấy từ danh sách đã lọc
        if (position < 0 || position >= vocabularyListFiltered.size()) return; // Kiểm tra an toàn
        VocabularyResponse vocab = vocabularyListFiltered.get(position);

        holder.textViewWord.setText(vocab.getWord());
        holder.textViewDefinition.setText(vocab.getDefinition());
        holder.textViewPhonetic.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
        holder.textViewPhonetic.setVisibility(vocab.getPhonetic() != null ? View.VISIBLE : View.GONE);
        holder.textViewExample.setText(vocab.getExample() != null ? "Ví dụ: " + vocab.getExample() : "");
        holder.textViewExample.setVisibility(vocab.getExample() != null ? View.VISIBLE : View.GONE);

        // Xử lý click xóa
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                int currentPosition = holder.getAdapterPosition(); // Lấy vị trí mới nhất trong list đã lọc
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                    VocabularyResponse itemToDelete = vocabularyListFiltered.get(currentPosition);
                    int originalPosition = findPositionInFullList(itemToDelete.getId()); // Tìm vị trí trong list gốc
                    if (originalPosition != -1) {
                        deleteListener.onVocabularyDeleteClick(itemToDelete, originalPosition); // Gọi listener với vị trí gốc
                    } else {
                        // Hiếm khi xảy ra nếu logic đúng, nhưng nên log lại
                        Log.w("VocabularyAdapter", "Could not find original position for item to delete: ID " + itemToDelete.getId());
                        // Có thể fallback gọi listener với vị trí hiện tại và để Activity xử lý tìm kiếm lại
                        // deleteListener.onVocabularyDeleteClick(itemToDelete, currentPosition);
                    }
                }
            }
        });
        // Chỉ hiển thị nút xóa nếu có listener (dùng trong DetailActivity)
        holder.buttonDelete.setVisibility(deleteListener != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return vocabularyListFiltered == null ? 0 : vocabularyListFiltered.size(); // Kích thước của list đã lọc
    }

    // Cập nhật cả hai danh sách khi có dữ liệu mới
    public void updateData(List<VocabularyResponse> newVocabularies) {
        if (newVocabularies != null) {
            this.vocabularyListFull = new ArrayList<>(newVocabularies);
            this.vocabularyListFiltered = new ArrayList<>(newVocabularies);
        } else {
            this.vocabularyListFull.clear();
            this.vocabularyListFiltered.clear();
        }
        notifyDataSetChanged(); // Cần dùng DiffUtil cho hiệu năng tốt hơn
    }

    // Xóa item khỏi cả hai danh sách (dựa vào vị trí GỐC)
    public void removeItem(int originalPosition) {
        if (originalPosition >= 0 && originalPosition < vocabularyListFull.size()) {
            VocabularyResponse removedItem = vocabularyListFull.remove(originalPosition); // Xóa khỏi list gốc

            // Tìm và xóa khỏi list đã lọc (nếu nó đang hiển thị)
            int filteredPosition = findPositionInFilteredList(removedItem.getId());
            if (filteredPosition != -1) {
                vocabularyListFiltered.remove(filteredPosition);
                notifyItemRemoved(filteredPosition); // Thông báo xóa ở vị trí đã lọc
                // Cập nhật lại các vị trí sau đó trong list đã lọc
                notifyItemRangeChanged(filteredPosition, vocabularyListFiltered.size());
            }
            // Nếu không tìm thấy trong list đã lọc (tức là đang không hiển thị do filter)
            // thì không cần làm gì với RecyclerView cả, vì nó không nhìn thấy item đó
        }
    }

    // Lấy item từ danh sách đã lọc
    public VocabularyResponse getItem(int position) {
        if (position >= 0 && position < vocabularyListFiltered.size()) {
            return vocabularyListFiltered.get(position);
        }
        return null;
    }

    // Hàm để Activity lấy danh sách đầy đủ (cần cho Game)
    public List<VocabularyResponse> getFullList() {
        return vocabularyListFull;
    }

    // Helper tìm vị trí trong list gốc
    private int findPositionInFullList(Long vocabId) {
        if (vocabId == null || vocabularyListFull == null) return -1;
        for (int i = 0; i < vocabularyListFull.size(); i++) {
            if (vocabId.equals(vocabularyListFull.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }
    // Helper tìm vị trí trong list đã lọc
    private int findPositionInFilteredList(Long vocabId) {
        if (vocabId == null || vocabularyListFiltered == null) return -1;
        for (int i = 0; i < vocabularyListFiltered.size(); i++) {
            if (vocabId.equals(vocabularyListFiltered.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }


    // --- Implement Filterable ---
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
                    // Lọc theo word hoặc definition
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
            notifyDataSetChanged(); // Cập nhật RecyclerView
        }
    };
    // --- Kết thúc Filterable ---

    // ViewHolder giữ nguyên
    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord, textViewPhonetic, textViewDefinition, textViewExample;
        ImageButton buttonDelete;
        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWord = itemView.findViewById(R.id.textViewWord);
            textViewPhonetic = itemView.findViewById(R.id.textViewPhonetic);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewExample = itemView.findViewById(R.id.textViewExample);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteVocabulary);
        }
    }
}