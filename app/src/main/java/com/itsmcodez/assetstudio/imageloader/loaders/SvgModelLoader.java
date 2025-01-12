package com.itsmcodez.assetstudio.imageloader.loaders;

import android.content.Context;
import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.caverock.androidsvg.SVG;
import com.itsmcodez.assetstudio.imageloader.fetcher.SvgDataFetcher;

public class SvgModelLoader implements ModelLoader<SVG, Drawable> {
    private Context context;

    public SvgModelLoader(Context context) {
    	this.context = context;
    }
    
    @Override
    public LoadData<Drawable> buildLoadData(SVG model, int width, int height, Options options) {
        if(model != null) {
            if (width != SIZE_ORIGINAL) {
                model.setDocumentWidth(width);
            }
            if (height != SIZE_ORIGINAL) {
                model.setDocumentHeight(height);
            }
            return new LoadData<>(new ObjectKey(model.hashCode()), new SvgDataFetcher(context, model));
        }
        throw new IllegalArgumentException("SVG cannot be null!");
    }

    @Override
    public boolean handles(SVG model) {
        // handle svg
        return true;
    }
}
