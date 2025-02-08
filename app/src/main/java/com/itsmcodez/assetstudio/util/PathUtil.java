package com.itsmcodez.assetstudio.util;
import android.net.Uri;
import android.provider.DocumentsContract;

public class PathUtil {
    
    public static String getPathFromUri(Uri uri) {
        String docId = DocumentsContract.getTreeDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String path = split.length > 1 ? split[1] : "";

        if ("primary".equalsIgnoreCase(type)) {
            return "/storage/emulated/0/" + path;
        }
        // Handle non-primary volumes
        return "storage/" + type + "/" + path;
    }
    
    public static String getPathFromUri(final String URI) {
        Uri uri = Uri.parse(URI);
        String docId = DocumentsContract.getTreeDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        String path = split.length > 1 ? split[1] : "";

        if ("primary".equalsIgnoreCase(type)) {
            return "/storage/emulated/0/" + path;
        }
        // Handle non-primary volumes
        return "storage/" + type + "/" + path;
    }
    
}
