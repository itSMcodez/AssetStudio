package com.itsmcodez.assetstudio.processors;

import android.content.Context;
import com.itsmcodez.assetstudio.common.Message;
import com.itsmcodez.assetstudio.markers.MessageType;
import com.itsmcodez.assetstudio.models.IconModel;
import java.io.InputStream;
import com.itsmcodez.assetstudio.models.base.Model;
import java.nio.file.Path;
import com.itsmcodez.assetstudio.common.AssetExportCallback;
import java.util.concurrent.ExecutorService;
import com.itsmcodez.assetstudio.processors.base.Processor;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SVG2VectorProcessor implements Processor {

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
        
        // Svg to VectorDrawable xml
        try {
            parseSVGToVector(context.getAssets().open(icon.getPath()), destPath.toFile(), callback);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    @Override
    public void processAsync(Model model, Path destPath, AssetExportCallback callback, ExecutorService supplier) {
        supplier.execute(() -> {
                process(model, destPath, callback);
        });
    }
    
    private static void parseSVGToVector(InputStream svgFile, File vectorFile, AssetExportCallback callback) {
        try {
            // Parse the SVG file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(svgFile);
            Element root = document.getDocumentElement();

            // Initialize and build the Vector Drawable XML structure
            StringBuilder vectorContent = new StringBuilder();
            vectorContent
                    .append("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n")
                    .append("    android:width=\"24dp\"\n")
                    .append("    android:height=\"24dp\"\n")
                    .append("    android:viewportWidth=\"24\"\n")
                    .append("    android:viewportHeight=\"24\"\n")
                    .append("    android:tint=\"?attr/colorControlNormal\">\n\n");
            // Process supported SVG elements
            processPaths(root, vectorContent);
            processCircles(root, vectorContent);
            processRects(root, vectorContent);
            processLines(root, vectorContent);
            processEllipses(root, vectorContent);
            processPolygons(root, vectorContent);
            vectorContent.append("</vector>");

            // Write to the output file
            try (FileWriter writer = new FileWriter(vectorFile)) {
                writer.write(vectorContent.toString());
            }
            
            // Notify user
            if(callback != null) {
                callback.onExportSuccess();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if(callback != null) {
            	callback.onExportFailed(new Message() {
                        @Override
                        public String getDescription() {
                            return "An error occurred while parsing the svg file";
                        }
                        
                        @Override
                        public MessageType getType() {
                            return MessageType.SAVE_ERROR;
                        }
                });
            }
        }
    }

    // Process <path> elements
    private static void processPaths(Element root, StringBuilder vectorContent) {
        NodeList paths = root.getElementsByTagName("path");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <path> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < paths.getLength(); i++) {
            
            // <path> elements and their attributes
            Element path = (Element) paths.item(i);
            String pathData = path.getAttribute("d");

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }

    // Process <circle> elements
    private static void processCircles(Element root, StringBuilder vectorContent) {
        NodeList circles = root.getElementsByTagName("circle");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <circle> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < circles.getLength(); i++) {
            
            // <circle> elements and their attributes
            Element circle = (Element) circles.item(i);
            float cx = Float.parseFloat(circle.getAttribute("cx"));
            float cy = Float.parseFloat(circle.getAttribute("cy"));
            float r = Float.parseFloat(circle.getAttribute("r"));

            String pathData = String.format(
                            "M%.2f,%.2f A%.2f,%.2f 0 1,0 %.2f,%.2f A%.2f,%.2f 0 1,0 %.2f,%.2f Z",
                            cx - r, cy, r, r, cx + r, cy, r, r, cx - r, cy);

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }

    // Process <rect> elements
    private static void processRects(Element root, StringBuilder vectorContent) {
        NodeList rects = root.getElementsByTagName("rect");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <rect> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < rects.getLength(); i++) {
            
            // <rect> elements and their attributes
            Element rect = (Element) rects.item(i);
            float x = Float.parseFloat(rect.getAttribute("x"));
            float y = Float.parseFloat(rect.getAttribute("y"));
            float width = Float.parseFloat(rect.getAttribute("width"));
            float height = Float.parseFloat(rect.getAttribute("height"));

            String pathData = String.format(
                            "M%.2f,%.2f L%.2f,%.2f L%.2f,%.2f L%.2f,%.2f Z",
                            x, y, x + width, y, x + width, y + height, x, y + height);

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }

    // Process <line> elements
    private static void processLines(Element root, StringBuilder vectorContent) {
        NodeList lines = root.getElementsByTagName("line");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <line> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < lines.getLength(); i++) {
            
            // <line> elements and their attributes
            Element line = (Element) lines.item(i);
            float x1 = Float.parseFloat(line.getAttribute("x1"));
            float y1 = Float.parseFloat(line.getAttribute("y1"));
            float x2 = Float.parseFloat(line.getAttribute("x2"));
            float y2 = Float.parseFloat(line.getAttribute("y2"));

            String pathData = String.format("M%.2f,%.2f L%.2f,%.2f", x1, y1, x2, y2);

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }

    // Process <ellipse> elements
    private static void processEllipses(Element root, StringBuilder vectorContent) {
        NodeList ellipses = root.getElementsByTagName("ellipse");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <ellipse> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < ellipses.getLength(); i++) {
            
            // <ellipse> elements and their attributes
            Element ellipse = (Element) ellipses.item(i);
            float cx = Float.parseFloat(ellipse.getAttribute("cx"));
            float cy = Float.parseFloat(ellipse.getAttribute("cy"));
            float rx = Float.parseFloat(ellipse.getAttribute("rx"));
            float ry = Float.parseFloat(ellipse.getAttribute("ry"));

            String pathData = String.format(
                            "M%.2f,%.2f A%.2f,%.2f 0 1,0 %.2f,%.2f A%.2f,%.2f 0 1,0 %.2f,%.2f Z",
                            cx - rx, cy, rx, ry, cx + rx, cy, rx, ry, cx - rx, cy);

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }

    // Process <polygon> elements
    private static void processPolygons(Element root, StringBuilder vectorContent) {
        NodeList polygons = root.getElementsByTagName("polygon");
        
        // <svg> element and it's attribute
        // I will apply the attributes of <svg> to each <polygon> element
        Element svg = root;
        String fill = svg.getAttribute("fill");
        String stroke = svg.getAttribute("stroke");
        String strokeWidth = svg.getAttribute("stroke-width");
        String strokeLineCap = svg.getAttribute("stroke-linecap");
        String strokeLineJoin = svg.getAttribute("stroke-linejoin");
        
        for (int i = 0; i < polygons.getLength(); i++) {
            
            // <polygon> elements and their attributes
            Element polygon = (Element) polygons.item(i);
            String points = polygon.getAttribute("points");

            String pathData = "M" + points.trim().replace(" ", " L") + " Z";

            if(!pathData.isEmpty()) {
            	vectorContent
                        .append("    <path android:pathData=\"")
                        .append(pathData)
                        .append("\"\n");
            }
            
            if(!fill.isEmpty()) {
            	vectorContent
                        .append("        android:fillColor=\"")
                        .append(fill.equals("none") ? "#00000000" : fill)
                        .append("\"\n");
            }

            if (!stroke.isEmpty()) {
                vectorContent
                        .append("        android:strokeColor=\"")
                        .append(stroke.equals("currentColor") ? "#000000" : stroke)
                        .append("\"\n");
            }
            
            if (!strokeWidth.isEmpty()) {
                vectorContent
                        .append("        android:strokeWidth=\"")
                        .append(strokeWidth)
                        .append("\"\n");
            }
            
            if(!strokeLineCap.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineCap=\"")
                        .append(strokeLineCap.equals("butt") ? "butt" : strokeLineCap.equals("round") ? "round" : "square")
                        .append("\"\n");
            }
            
            if(!strokeLineJoin.isEmpty()) {
                vectorContent
                        .append("        android:strokeLineJoin=\"")
                        .append(strokeLineJoin.equals("miter") ? "miter" : strokeLineJoin.equals("round") ? "round" : "bevel")
                        .append("\"");
            }
            
            vectorContent.append(" />\n\n");
        }
    }
}
