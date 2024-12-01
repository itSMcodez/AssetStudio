package com.itsmcodez.assetstudio.exporters;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import androidx.documentfile.provider.DocumentFile;
import com.itsmcodez.assetstudio.common.AssetExportCallback;
import com.itsmcodez.assetstudio.common.Exporter;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.models.base.Model;
import com.itsmcodez.assetstudio.common.Message;
import com.itsmcodez.assetstudio.markers.ExportType;
import com.itsmcodez.assetstudio.processors.SVG2PNGProcessor;
import com.itsmcodez.assetstudio.processors.SVG2VectorProcessor;
import com.itsmcodez.assetstudio.processors.SVGProcessor;
import com.itsmcodez.assetstudio.utils.PathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class IconExporter extends Exporter {
    private Context context;

    @Override
    protected void createExport(ExportType type, Model model, DocumentFile destFolder) {
        if (type == null || model == null || destFolder == null) {
            showToast("Invalid export parameters.");
            return;
        }

        if (model instanceof IconModel iconModel) {
            processExport(type, iconModel, destFolder);
        }
    }

    @Override
    protected void shouldIntercept(Message msg) {
        // No interception logic implemented
    }

    public void export(ExportType type, Model model, DocumentFile destFolder, Context context) {
        this.context = context;
        createExport(type, model, destFolder);
    }

    private void processExport(ExportType type, IconModel icon, DocumentFile destFolder) {
        Path outputPath = createOutputPath(destFolder, icon.getName(), getFileExtension(type));

        if (outputPath == null) {
            showToast("Invalid destination folder.");
            return;
        }

        switch (type) {
            case PNG -> new SVG2PNGProcessor()
                    .processAsync(
                            icon,
                            outputPath,
                            createCallback(outputPath),
                            Executors.newSingleThreadExecutor());
            case SVG -> new SVGProcessor()
                    .processAsync(
                            icon,
                            outputPath,
                            createCallback(outputPath),
                            Executors.newSingleThreadExecutor());
            case VECTOR -> new SVG2VectorProcessor()
                    .processAsync(
                            icon,
                            outputPath,
                            createCallback(outputPath),
                            Executors.newSingleThreadExecutor());
            default -> showToast("Unsupported export type.");
        }
    }

    private Path createOutputPath(DocumentFile destFolder, String fileName, String extension) {
        try {
            return Paths.get(
                    PathUtils.getFullPathFromTreeUri(destFolder.getUri()),
                    fileName.replace('-', '_') + extension);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private AssetExportCallback createCallback(Path path) {
        return new AssetExportCallback() {
            @Override
            public void onExportSuccess() {
                runOnUiThread(() -> showToast("File saved: " + path));
            }

            @Override
            public void onExportFailed(Message msg) {
                runOnUiThread(() -> showToast(msg.getDescription()));
            }

            @Override
            public Context getExportContext() {
                return context;
            }
        };
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void runOnUiThread(Runnable action) {
        new Handler(context.getMainLooper()).post(action);
    }

    private String getFileExtension(ExportType type) {
        return switch (type) {
            case PNG -> ".png";
            case SVG -> ".svg";
            case VECTOR -> ".xml";
            default -> "";
        };
    }
}
