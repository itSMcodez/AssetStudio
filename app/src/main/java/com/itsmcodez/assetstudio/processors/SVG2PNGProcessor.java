package com.itsmcodez.assetstudio.processors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import com.blankj.utilcode.util.ConvertUtils;
import com.itsmcodez.assetstudio.common.Message;
import com.itsmcodez.assetstudio.markers.MessageType;
import com.itsmcodez.assetstudio.models.IconModel;
import com.itsmcodez.assetstudio.models.base.Model;
import com.itsmcodez.assetstudio.common.AssetExportCallback;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.nio.file.Path;
import com.itsmcodez.assetstudio.processors.base.Processor;

public class SVG2PNGProcessor implements Processor {

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

            /* 1. Convert icon from Picture to bitmap and write to file as PNG using a ByteBuffer */
            Bitmap bitmap = ConvertUtils.drawable2Bitmap(new PictureDrawable(icon.getPreview()));
            
            // 2. Resize the bitmap
            final float densityScale = context.getResources().getDisplayMetrics().density;
            final int height = (int)(ConvertUtils.dp2px(24.0f)/densityScale);
            final int width = (int)(ConvertUtils.dp2px(24.0f)/densityScale);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            
            // 3. Create image
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // 4. Write to the file
            Files.write(
                    destPath,
                    imageData,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // 5. Notify the user
            if(callback != null) {
            	callback.onExportSuccess();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
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
