package com.itsmcodez.assetstudio.utils;
import android.content.Context;
import androidx.documentfile.provider.DocumentFile;
import com.itsmcodez.assetstudio.exporters.IconExporter;
import com.itsmcodez.assetstudio.markers.ExportType;
import com.itsmcodez.assetstudio.models.IconModel;

public class IconUtils {
    private static final IconExporter EXPORTER = new IconExporter();
    
    public static void exportIcon2SVG(IconModel icon, DocumentFile destFolder, Context context) {
    	EXPORTER.export(ExportType.SVG, icon, destFolder, context);
    }
    
    public static void exportIcon2PNG(IconModel icon, DocumentFile destFolder, Context context) {
    	EXPORTER.export(ExportType.PNG, icon, destFolder, context);
    }
    
    public static void exportIcon2Vector(IconModel icon, DocumentFile destFolder, Context context) {
    	EXPORTER.export(ExportType.VECTOR, icon, destFolder, context);
    }
}
