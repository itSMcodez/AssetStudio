package com.itsmcodez.assetstudio.imageloader.fetcher;

import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher.DataCallback;
import com.bumptech.glide.load.data.DataFetcher;
import com.caverock.androidsvg.SVG;

public class SvgDataFetcher implements DataFetcher<Drawable> {
    private SVG model;
    
    public SvgDataFetcher(SVG model) {
    	this.model = model;
    }
    
    @Override
    public void loadData(Priority priority, DataCallback<? super Drawable> callback) {
        Picture svgPicture = model.renderToPicture();
        PictureDrawable svgDrawable = new PictureDrawable(svgPicture);
        callback.onDataReady(svgDrawable);
    }

    @Override
    public void cleanup() {
        // Intentionally empty
    }

    @Override
    public void cancel() {
        // No server operations
    }

    @Override
    public Class<Drawable> getDataClass() {
        return Drawable.class;
    }

    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
