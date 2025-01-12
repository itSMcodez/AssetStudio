package com.itsmcodez.assetstudio.imageloader.factories;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.caverock.androidsvg.SVG;
import com.itsmcodez.assetstudio.imageloader.loaders.SvgModelLoader;

public class SvgModelLoaderFactory implements ModelLoaderFactory<SVG, Drawable> {
    private Context context;

    public SvgModelLoaderFactory(Context context) {
    	this.context = context;
    }
    
    @Override
    public ModelLoader<SVG, Drawable> build(MultiModelLoaderFactory unused) {
        return new SvgModelLoader(context);
    }

    @Override
    public void teardown() {
        // Do nothing
    }
}
