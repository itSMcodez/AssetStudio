package com.itsmcodez.assetstudio.listeners;
import android.content.Intent;
import android.net.Uri;

public interface PathChooserListener {
    boolean onPathChosen(Intent path);
    void onCancelled();
    Uri getDestPathUri();
}
