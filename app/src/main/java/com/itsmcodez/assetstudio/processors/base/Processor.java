package com.itsmcodez.assetstudio.processors.base;
import com.itsmcodez.assetstudio.common.AssetExportCallback;
import com.itsmcodez.assetstudio.models.base.Model;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public interface Processor {
    void process(Model model, Path destPath, AssetExportCallback callback);
    void processAsync(Model model, Path destPath, AssetExportCallback callback, ExecutorService supplier);
}
