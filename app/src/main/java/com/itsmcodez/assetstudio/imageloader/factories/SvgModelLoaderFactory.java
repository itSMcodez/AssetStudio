package com.itsmcodez.assetstudio.imageloader.factories;

import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.caverock.androidsvg.SVG;
import com.itsmcodez.assetstudio.imageloader.loaders.SvgModelLoader;

public class SvgModelLoaderFactory implements ModelLoaderFactory<SVG, Drawable> {

    @Override
    public ModelLoader<SVG, Drawable> build(MultiModelLoaderFactory unused) {
        return new SvgModelLoader();
    }

    @Override
    public void teardown() {
        // Do nothing
    }
}
