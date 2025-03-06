package com.itsmcodez.assetstudio.options;
import android.app.Application;
import android.content.Context;
import com.blankj.utilcode.util.ConvertUtils;
import com.itsmcodez.assetstudio.common.Options;
import com.itsmcodez.assetstudio.util.DomXmlModifier;
import com.itsmcodez.assetstudio.viewmodels.IconOptionsViewModel;
import java.io.InputStream;

public class IconOptions extends IconOptionsViewModel implements Options {
    private Context mContext;
    private InputStream mInputStream;
    private InputStream modifiedStream;
    private String CONTENTS = "";
    private final String SVG_TAG = "svg";
    
    private final class SVG_ATTRS {
        public static final String FILL  = "fill";
        public static final String STROKE = "stroke";
        public static final String STROKE_WIDTH = "stroke-width";
        public static final String STROKE_LINECAP  = "stroke-linecap";
        public static final String STROKE_LINEJOIN  = "stroke-linejoin";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
    }
    
    public static enum StrokeLineCap {
        BUTT,
        ROUND,
        SQUARE;
    }
    
    public static enum StrokeLineJoin {
        ARCS,
        BEVEL,
        MITER,
        MITER_CLIP,
        ROUND;
    }
    
    public IconOptions(Application application) {
        super(application);
        mContext = application;
    }
    
    public void setIconInputStream(InputStream is) {
    	mInputStream = is;
        
        if(is != null) {
        	CONTENTS = ConvertUtils.inputStream2String(is, "utf-8");
        }
    }
    
    private void modifyAttribute(String attribute, String attributeValue) {
        try {
        	CONTENTS = DomXmlModifier.modifyXml(CONTENTS, SVG_TAG, null, attribute, attributeValue);
            setXml_contentLiveData(CONTENTS);
        } catch(Exception err) {
        	err.printStackTrace();
        }
    }
    
    public void setFillColor(String hex_color) {
        modifyAttribute(SVG_ATTRS.FILL, hex_color);
    }
    
    public void setStrokeColor(String hex_color) {
    	modifyAttribute(SVG_ATTRS.STROKE, hex_color);
    }
    
    public void setStrokeWidth(int width) {
    	modifyAttribute(SVG_ATTRS.STROKE_WIDTH, String.valueOf(width));
    }
    
    public void setWidth(String width) {
    	modifyAttribute(SVG_ATTRS.WIDTH, width);
    }
    
    public void setHeight(String height) {
    	modifyAttribute(SVG_ATTRS.HEIGHT, height);
    }
    
    public void setStrokeLineCap(final StrokeLineCap type) {
    	switch(type) {
            case BUTT : modifyAttribute(SVG_ATTRS.STROKE_LINECAP, "butt");
            break;
            case SQUARE : modifyAttribute(SVG_ATTRS.STROKE_LINECAP, "square");
            break;
            default : modifyAttribute(SVG_ATTRS.STROKE_LINECAP, "round");
        }
    }
    
    public void setStrokeLineJoin(final StrokeLineJoin type) {
    	switch(type) {
            case ARCS : modifyAttribute(SVG_ATTRS.STROKE_LINEJOIN, "arcs");
            break;
            case BEVEL : modifyAttribute(SVG_ATTRS.STROKE_LINEJOIN, "bevel");
            break;
            case MITER : modifyAttribute(SVG_ATTRS.STROKE_LINEJOIN, "miter");
            break;
            case MITER_CLIP : modifyAttribute(SVG_ATTRS.STROKE_LINEJOIN, "miter-clip");
            break;
            default : modifyAttribute(SVG_ATTRS.STROKE_LINEJOIN, "round");
        }
    }
    
    @Override
    public Object getModifiedVersion() {
        byte[] data = CONTENTS.getBytes();
        modifiedStream = ConvertUtils.bytes2InputStream(data);
        return modifiedStream;
    }
    
}
