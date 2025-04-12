package com.example.echoenglish_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;

import java.util.List;

public class ShortcutAdapter extends RecyclerView.Adapter<ShortcutAdapter.ViewHolder> {

    private final List<Shortcut> shortcuts;
    private final OnShortcutClickListener listener;

    public interface OnShortcutClickListener {
        void onShortcutClick(Shortcut shortcut);
    }

    public ShortcutAdapter(List<Shortcut> shortcuts, OnShortcutClickListener listener) {
        this.shortcuts = shortcuts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shortcut, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shortcut shortcut = shortcuts.get(position);
        holder.nameTextView.setText(shortcut.getName());

        // TODO: Use Glide/Picasso for loading logos from URLs if needed
        if (shortcut.getIconResId() != 0) {
            holder.iconImageView.setImageResource(shortcut.getIconResId());
        } else {
            holder.iconImageView.setImageResource(R.drawable.background_button_blue); // Default icon
        }

        holder.itemView.setOnClickListener(v -> listener.onShortcutClick(shortcut));
    }

    @Override
    public int getItemCount() {
        return shortcuts.size();
    }

    // ViewHolder linking to item_shortcut.xml views
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iv_shortcut_icon);
            nameTextView = itemView.findViewById(R.id.tv_shortcut_name);
        }
    }

    // Shortcut Model (inner class or separate file)
    public static class Shortcut {
        String name; String url; int iconResId;
        public Shortcut(String name, String url, int iconResId) {
            this.name = name; this.url = url; this.iconResId = iconResId;
        }
        public String getName() { return name; }
        public String getUrl() { return url; }
        public int getIconResId() { return iconResId; }
    }
}