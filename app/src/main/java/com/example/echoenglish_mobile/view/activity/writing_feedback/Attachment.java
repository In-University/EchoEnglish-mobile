package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.net.Uri;

public class Attachment {
    private Uri uri;
    private String fileName;
    private long fileSize; // Optional
    private String mimeType; // Optional

    // Constructor, Getters, Setters...
    public Attachment(Uri uri, String fileName, String mimeType, long fileSize) {
        this.uri = uri;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    public Uri getUri() { return uri; }
    public String getFileName() { return fileName; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }

    // Optional: equals() and hashCode() based on Uri might be useful
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
