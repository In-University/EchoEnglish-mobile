package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.CategoryResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.model.Category;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter<T extends CategoryResponse> extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<T> categoryList;
    private OnCategoryClickListener<T> listener;

    public interface OnCategoryClickListener<T> {
        void onCategoryClick(T category);
    }

    public CategoryAdapter(Context context, List<T> categoryList, OnCategoryClickListener<T> listener) {
        this.context = context;
        this.categoryList = categoryList != null ? new ArrayList<>(categoryList) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position < 0 || position >= categoryList.size()) return;
        T category = categoryList.get(position);

        holder.textCategoryName.setText(category.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    public void updateData(List<T> newCategories) {
        this.categoryList.clear();
        if (newCategories != null) {
            this.categoryList.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCategoryIcon;
        TextView textCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCategoryIcon = itemView.findViewById(R.id.imageCategoryIcon);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
        }
    }
}
