package com.itsmcodez.assetstudio.processors;

import com.itsmcodez.assetstudio.common.Message;
import com.itsmcodez.assetstudio.markers.MessageType;
import com.itsmcodez.assetstudio.models.IconModel;
import android.content.Context;
import com.itsmcodez.assetstudio.models.base.Model;
import com.itsmcodez.assetstudio.common.AssetExportCallback;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.nio.file.Path;
import com.itsmcodez.assetstudio.processors.base.Processor;

public class SVGProcessor implements Processor {

    @Override
    public void process(Model model, Path destPath, AssetExportCallback callback) {
        
        IconModel icon = null;
        if(model instanceof IconModel iconModel) {
            icon = iconModel;
        }
        
        Context context = null;
        if(callback != null) {
        	context = callback.getExportContext();
        }
        
        try {
            // 1. Get input stream from the assets
            InputStream in = context.getAssets().open(icon.getPath());
            
            // 2. Copy the file using java.nio
            Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 3. Notify the user
            if(callback != null) {
            	callback.onExportSuccess();
            }
            
        } catch(IOException err) {
            err.printStackTrace();
            if(callback != null) {
            	callback.onExportFailed(new Message() {
                        @Override
                        public String getDescription() {
                            return "An error occurred while saving the file";
                        }
                        
                        @Override
                        public MessageType getType() {
                            return MessageType.SAVE_ERROR;
                        }
                });
            }
        }
    }

    @Override
    public void processAsync(Model model, Path destPath, AssetExportCallback callback, ExecutorService supplier) {
        supplier.execute(() -> {
                process(model, destPath, callback);
        });
    }
}
