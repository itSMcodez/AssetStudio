package com.itsmcodez.assetstudio.utils;

import android.net.Uri;
import android.provider.DocumentsContract;

public class PathUtils {
    
    public static String getFullPathFromTreeUri(Uri treeUri) {
        if (treeUri == null) return null;

        String docId = DocumentsContract.getTreeDocumentId(treeUri);
        String[] split = docId.split(":");
        String type = split[0];

        String fullPath;
        if ("primary".equalsIgnoreCase(type)) {
            // Path for primary storage
            fullPath = "/storage/emulated/0/" + (split.length > 1 ? split[1] : "");
        } else {
            // Handle non-primary volumes (e.g., SD cards or USB drives)
            fullPath = "/storage/" + type + "/" + (split.length > 1 ? split[1] : "");
        }

        return fullPath;
    }
}
