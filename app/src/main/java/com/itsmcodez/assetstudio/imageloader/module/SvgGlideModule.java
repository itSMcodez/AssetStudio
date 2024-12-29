package com.itsmcodez.assetstudio.imageloader.module;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;
import com.itsmcodez.assetstudio.imageloader.factories.SvgModelLoaderFactory;

@GlideModule
public class SvgGlideModule extends AppGlideModule {
    
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.prepend(SVG.class, Drawable.class, new SvgModelLoaderFactory());
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
