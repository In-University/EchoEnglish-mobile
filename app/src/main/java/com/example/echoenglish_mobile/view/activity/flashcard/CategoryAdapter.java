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
// *** Import DTO Response thay vì Model ***

import java.util.ArrayList; // Import ArrayList
import java.util.List;

// *** Thêm tham số kiểu Generic <T> và sử dụng nó ***
// Hoặc bỏ Generic nếu chỉ dùng cho CategoryResponse: public class CategoryAdapter ...
public class CategoryAdapter<T extends CategoryResponse> extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
// Nếu chỉ dùng CategoryResponse, không cần Generic: public class CategoryAdapter ...

    private Context context;
    private List<T> categoryList; // *** Sử dụng kiểu Generic T ***
    // Hoặc: private List<CategoryResponse> categoryList;
    private OnCategoryClickListener<T> listener; // *** Sử dụng kiểu Generic T ***
    // Hoặc: private OnCategoryClickListener<CategoryResponse> listener;


    // *** Interface Listener cũng dùng Generic T ***
    public interface OnCategoryClickListener<T> {
        void onCategoryClick(T category); // *** Tham số là T ***
    }
    // Hoặc nếu không dùng Generic:
    // public interface OnCategoryClickListener {
    //    void onCategoryClick(CategoryResponse category);
    // }

    // *** Constructor nhận List<T> và Listener<T> ***
    public CategoryAdapter(Context context, List<T> categoryList, OnCategoryClickListener<T> listener) {
        // Hoặc: public CategoryAdapter(Context context, List<CategoryResponse> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        // Khởi tạo list an toàn
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
        if (position < 0 || position >= categoryList.size()) return; // Kiểm tra bounds
        T category = categoryList.get(position); // *** Lấy đối tượng kiểu T ***
        // Hoặc: CategoryResponse category = categoryList.get(position);

        holder.textCategoryName.setText(category.getName()); // Gọi getName() từ CategoryResponse
        // holder.imageCategoryIcon.setImageResource(...); // Set icon nếu CategoryResponse có trường icon

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category); // *** Truyền đối tượng kiểu T ***
                // Hoặc: listener.onCategoryClick(category); // Nếu category là CategoryResponse
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    // *** updateData nhận List<T> ***
    public void updateData(List<T> newCategories) {
        // Hoặc: public void updateData(List<CategoryResponse> newCategories) {
        this.categoryList.clear();
        if (newCategories != null) {
            this.categoryList.addAll(newCategories);
        }
        notifyDataSetChanged(); // Nên dùng DiffUtil
    }

    // ViewHolder không cần thay đổi
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