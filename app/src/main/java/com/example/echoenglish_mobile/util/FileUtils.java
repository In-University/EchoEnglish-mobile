package com.example.echoenglish_mobile.util;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;


public class FileUtils {

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("FileUtils", "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "unknown_file";
    }

    public static long getFileSize(Context context, Uri uri) {
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        return cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("FileUtils", "Error getting file size", e);
            }
        }
        return 0; // Or -1 to indicate unknown
    }

    // Get Mime Type
    public static String getMimeType(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            return cR.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
    }


//    // Get placeholder icon based on MimeType
//    public static int getIconForMimeType(String mimeType) {
//        if (mimeType == null) return R.drawable.ic_file_generic; // Default generic icon
//
//        if (mimeType.startsWith("image/")) return R.drawable.ic_file_image;
//        if (mimeType.startsWith("video/")) return R.drawable.ic_file_video;
//        if (mimeType.startsWith("audio/")) return R.drawable.ic_file_audio;
//        if (mimeType.equals("application/pdf")) return R.drawable.ic_file_pdf;
//        if (mimeType.equals("application/msword") || mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return R.drawable.ic_file_doc;
//        if (mimeType.equals("application/vnd.ms-excel") || mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return R.drawable.ic_file_xls;
//        if (mimeType.equals("application/vnd.ms-powerpoint") || mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) return R.drawable.ic_file_ppt;
//        if (mimeType.equals("application/zip") || mimeType.equals("application/x-rar-compressed")) return R.drawable.ic_file_zip;
//        if (mimeType.startsWith("text/")) return R.drawable.ic_file_text;
//
//        return R.drawable.ic_file_generic; // Default
//        // Add more specific icons as needed
//        // You need to create these drawable resources (ic_file_generic, ic_file_image, etc.)
//    }
}
