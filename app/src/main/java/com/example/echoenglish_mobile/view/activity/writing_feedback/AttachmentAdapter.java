package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.content.Context;
import android.text.format.Formatter; // For formatting file size
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout; // Or the specific type of the background container
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // For preview placeholder

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
//        ImageButton btnPreviewFile;
        ImageButton btnRemoveFile;
        FrameLayout iconContainer;

        public AttachmentViewHolder(@NonNull View itemView, AttachmentActionListener listener) {
            super(itemView);
            imgFileType = itemView.findViewById(R.id.imgFileType);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            txtFileSize = itemView.findViewById(R.id.txtFileSize);
            txtFileType = itemView.findViewById(R.id.txtFileType);
//            btnPreviewFile = itemView.findViewById(R.id.btnPreviewFile);
            btnRemoveFile = itemView.findViewById(R.id.btnRemoveFile);
            // iconContainer = itemView.findViewById(R.id.YOUR_ICON_CONTAINER_ID);

            // Set listeners using the interface
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

            // Format file size
            if (attachment.getFileSize() > 0) {
                txtFileSize.setText(Formatter.formatFileSize(context, attachment.getFileSize()));
                txtFileSize.setVisibility(View.VISIBLE);
            } else {
                // Hide size if unknown or zero
                txtFileSize.setVisibility(View.GONE);
                // Or display N/A:
                // txtFileSize.setText("N/A");
                // txtFileSize.setVisibility(View.VISIBLE);
            }

            // Set file type icon and description text
            String mimeType = attachment.getMimeType();
//            imgFileType.setImageResource(FileUtils.getIconForMimeType(mimeType));
            txtFileType.setText(getFileTypeDescription(mimeType)); // Use helper for description
            txtFileType.setVisibility(View.VISIBLE); // Ensure it's visible

            // Optional: Change icon background based on type
            // Drawable background = getBackgroundForMimeType(context, mimeType);
            // iconContainer.setBackground(background);

            // Optional: Change icon tint based on type if needed
            // int tintColor = getColorForMimeType(context, mimeType);
            // imgFileType.setColorFilter(ContextCompat.getColor(context, tintColor), PorterDuff.Mode.SRC_IN);


            // Determine if preview is possible (basic example: only for images)
//            boolean canPreview = mimeType != null && mimeType.startsWith("image/");
//            btnPreviewFile.setEnabled(canPreview);
//            btnPreviewFile.setAlpha(canPreview ? 1.0f : 0.4f); // Dim if cannot preview

        }

        // Helper to get a user-friendly description from MIME type
        private String getFileTypeDescription(String mimeType) {
            if (mimeType == null) return "File"; // Default

            if (mimeType.startsWith("image/")) return "Image";
            if (mimeType.startsWith("video/")) return "Video";
            if (mimeType.startsWith("audio/")) return "Audio";
            if (mimeType.equals("application/pdf")) return "PDF Document";
            if (mimeType.contains("wordprocessingml") || mimeType.contains("msword")) return "Word Document";
            if (mimeType.contains("spreadsheetml") || mimeType.contains("ms-excel")) return "Spreadsheet";
            if (mimeType.contains("presentationml") || mimeType.contains("ms-powerpoint")) return "Presentation";
            if (mimeType.contains("zip") || mimeType.contains("rar")) return "Archive";
            if (mimeType.startsWith("text/")) return "Text Document";
            // Add more specific descriptions
            return "File"; // Generic fallback
        }

        // Optional: Helper to get a background drawable based on type
        /*
        private Drawable getBackgroundForMimeType(Context context, String mimeType) {
            int drawableId = R.drawable.file_type_background_generic; // Default background
             if (mimeType == null) return ContextCompat.getDrawable(context, drawableId);

             if (mimeType.startsWith("image/")) drawableId = R.drawable.file_type_background_image;
             else if (mimeType.startsWith("video/")) drawableId = R.drawable.file_type_background_video;
             // ... add more cases ...

             return ContextCompat.getDrawable(context, drawableId);
             // You need to create these background drawables (e.g., different colored circles)
        }
        */

    }

    public void updateData(List<Attachment> newAttachments) {
        this.attachments.clear();
        this.attachments.addAll(newAttachments);
        notifyDataSetChanged();
    }

}