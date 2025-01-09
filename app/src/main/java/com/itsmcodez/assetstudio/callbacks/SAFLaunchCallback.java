package com.itsmcodez.assetstudio.callbacks;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;

public interface SAFLaunchCallback {
    void onLaunchResult(Uri uri);
    DocumentFile getDocumentFile();
}
