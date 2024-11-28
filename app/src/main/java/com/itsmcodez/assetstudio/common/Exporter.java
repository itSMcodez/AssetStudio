package com.itsmcodez.assetstudio.common;
import androidx.documentfile.provider.DocumentFile;
import com.itsmcodez.assetstudio.markers.ExportType;
import com.itsmcodez.assetstudio.models.base.Model;

public abstract class Exporter {
    protected abstract void createExport(ExportType type, Model model, DocumentFile destFolder);
    protected abstract void shouldIntercept(Message msg);
}
