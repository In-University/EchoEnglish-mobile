package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.util.FileUtils;

import java.util.List;
import java.util.Locale;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {

    private List<Attachment> attachments;
    private Context context;
    private AttachmentActionListener actionListener;

    public interface AttachmentActionListener {
        void onRemoveClicked(int position);
        void onPreviewClicked(int position);
    }

    public AttachmentAdapter(List<Attachment> attachments, Context context, AttachmentActionListener listener) {
        this.attachments = attachments;
        this.context = context;
        this.actionListener = listener;
    }

    public AttachmentAdapter(List<Attachment> attachments, Context context) {
        this.attachments = attachments;
        this.context = context;
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new AttachmentViewHolder(view, actionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        Attachment attachment = attachments.get(position);
        holder.bind(attachment, context);
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    static class AttachmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFileType;
        TextView txtFileName;
        TextView txtFileSize;
        TextView txtFileType;
        ImageButton btnRemoveFile;
        FrameLayout iconContainer;

        public AttachmentViewHolder(@NonNull View itemView, AttachmentActionListener listener) {
            super(itemView);
            imgFileType = itemView.findViewById(R.id.imgFileType);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            txtFileSize = itemView.findViewById(R.id.txtFileSize);
            txtFileType = itemView.findViewById(R.id.txtFileType);
            btnRemoveFile = itemView.findViewById(R.id.btnRemoveFile);

            btnRemoveFile.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemoveClicked(position);
                    }
                }
            });
        }

        void bind(Attachment attachment, Context context) {
            txtFileName.setText(attachment.getFileName());

            if (attachment.getFileSize() > 0) {
                txtFileSize.setText(Formatter.formatFileSize(context, attachment.getFileSize()));
                txtFileSize.setVisibility(View.VISIBLE);
            } else {
                txtFileSize.setVisibility(View.GONE);
            }

            String mimeType = attachment.getMimeType();
            txtFileType.setText(getFileTypeDescription(mimeType));
            txtFileType.setVisibility(View.VISIBLE);

        }

        private String getFileTypeDescription(String mimeType) {
            if (mimeType == null) return "File";

            if (mimeType.startsWith("image/")) return "Image";
            if (mimeType.startsWith("video/")) return "Video";
            if (mimeType.startsWith("audio/")) return "Audio";
            if (mimeType.equals("application/pdf")) return "PDF Document";
            if (mimeType.contains("wordprocessingml") || mimeType.contains("msword")) return "Word Document";
            if (mimeType.contains("spreadsheetml") || mimeType.contains("ms-excel")) return "Spreadsheet";
            if (mimeType.contains("presentationml") || mimeType.contains("ms-powerpoint")) return "Presentation";
            if (mimeType.contains("zip") || mimeType.contains("rar")) return "Archive";
            if (mimeType.startsWith("text/")) return "Text Document";
            return "File";
        }

    }

    public void updateData(List<Attachment> newAttachments) {
        this.attachments.clear();
        this.attachments.addAll(newAttachments);
        notifyDataSetChanged();
    }

}