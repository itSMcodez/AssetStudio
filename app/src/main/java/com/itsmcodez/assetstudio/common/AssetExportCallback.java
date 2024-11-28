package com.itsmcodez.assetstudio.common;
import android.content.Context;

public abstract class AssetExportCallback {
    public abstract void onExportSuccess();
    public abstract void onExportFailed(Message msg);
    public abstract Context getExportContext();
}
