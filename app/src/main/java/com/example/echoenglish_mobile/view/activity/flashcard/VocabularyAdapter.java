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

    private static final String TAG = "VocabularyAdapter";
    private Context context;
    private List<VocabularyResponse> vocabularyListFull;
    private List<VocabularyResponse> vocabularyListFiltered;
    private OnVocabularyActionsListener actionsListener;
    private boolean isPublicContext;

    public interface OnVocabularyActionsListener {
        void onVocabularyDeleteClick(VocabularyResponse vocabulary, int originalPosition);
        void onVocabularyItemClick(VocabularyResponse vocabulary);
    }

    public VocabularyAdapter(Context context, List<VocabularyResponse> vocabularyList, OnVocabularyActionsListener listener, boolean isPublicContext) {
        this.context = context;
        this.vocabularyListFull = new ArrayList<>(vocabularyList != null ? vocabularyList : new ArrayList<>());
        this.vocabularyListFiltered = new ArrayList<>(this.vocabularyListFull);
        this.actionsListener = listener;
        this.isPublicContext = isPublicContext;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        if (position < 0 || position >= vocabularyListFiltered.size()) {
            Log.w(TAG, "onBindViewHolder: Invalid position " + position + ". List size: " + vocabularyListFiltered.size());
            return;
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
            holder.textViewExample.setText("Example: " + vocab.getExample());
            holder.textViewExample.setVisibility(View.VISIBLE);
        } else {
            holder.textViewExample.setVisibility(View.GONE);
        }

        holder.buttonEdit.setVisibility(isPublicContext ? View.GONE : View.VISIBLE);
        holder.buttonDelete.setVisibility(isPublicContext ? View.GONE : View.VISIBLE);


        if (holder.buttonEdit != null) {
            if (!isPublicContext) {
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
            } else {
                holder.buttonEdit.setOnClickListener(null);
            }
        }


        if (holder.buttonDelete != null) {
            if (!isPublicContext) {
                holder.buttonDelete.setOnClickListener(v -> {
                    if (actionsListener != null) {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < vocabularyListFiltered.size()) {
                            VocabularyResponse itemToDelete = vocabularyListFiltered.get(currentPosition);
                            int originalPosition = findPositionInFullList(itemToDelete.getId());
                            if (originalPosition != -1) {
                                actionsListener.onVocabularyDeleteClick(itemToDelete, originalPosition);
                            } else {
                                Log.w(TAG, "Could not find original position for delete: ID " + itemToDelete.getId() + ". Item not in full list?");
                                actionsListener.onVocabularyDeleteClick(itemToDelete, currentPosition);
                            }
                        } else {
                            Log.w(TAG, "Delete click: Invalid adapter position or list size mismatch.");
                        }
                    }
                });
            } else {
                holder.buttonDelete.setOnClickListener(null);
            }
        }

        if (holder.clickableArea != null) {
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

    public void removeItem(int originalPositionInFullList) {
        if (originalPositionInFullList >= 0 && originalPositionInFullList < vocabularyListFull.size()) {
            VocabularyResponse removedItem = vocabularyListFull.get(originalPositionInFullList);
            vocabularyListFull.remove(originalPositionInFullList);

            int filteredPosition = findPositionInFilteredList(removedItem.getId());
            if (filteredPosition != -1) {
                vocabularyListFiltered.remove(filteredPosition);
                notifyItemRemoved(filteredPosition);
                notifyItemRangeChanged(filteredPosition, vocabularyListFiltered.size());
            }
        } else {
            Log.w(TAG, "removeItem: Invalid original position provided: " + originalPositionInFullList + ". Full list size: " + (vocabularyListFull != null ? vocabularyListFull.size() : "null"));
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
            VocabularyResponse vocab = vocabularyListFull.get(i);
            if (vocab != null && vocabId.equals(vocab.getId())) return i;
        }
        return -1;
    }

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
                    if (item != null) {
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
            notifyDataSetChanged();
        }
    };


    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord, textViewPhonetic, textViewDefinition, textViewExample;
        ImageButton buttonDelete, buttonEdit;
        LinearLayout clickableArea;

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