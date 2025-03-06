package com.itsmcodez.assetstudio.imageloader.fetcher;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    private Context context;
    
    public SvgDataFetcher(Context context, SVG model) {
        this.context = context;
    	this.model = model;
    }
    
    @Override
    public void loadData(Priority priority, DataCallback<? super Drawable> callback) {
        Picture svgPicture = model.renderToPicture();
        PictureDrawable svgDrawable = new PictureDrawable(svgPicture);
        Drawable drawable = getBitmapDrawable(context, svgDrawable);
        callback.onDataReady(drawable);
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

    /* We convert the PictureDrawable to Bitmap to be able to apply tint to the imageview  
    *  as PictureDrawable alone doesn't support tinting
    */
    private Bitmap convertPictureDrawableToBitmap(PictureDrawable pictureDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                        pictureDrawable.getIntrinsicWidth(),
                        pictureDrawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pictureDrawable.getPicture());
        return bitmap;
    }

    private Drawable getDrawableFromBitmap(Context context, Bitmap bitmap) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        return bitmapDrawable;
    }

    private Drawable getBitmapDrawable(Context context, PictureDrawable pictureDrawable) {
        // Convert PictureDrawable to Bitmap
        Bitmap bitmap = convertPictureDrawableToBitmap(pictureDrawable);
        // Bitmap to BitmapDrawable
        return getDrawableFromBitmap(context, bitmap);
    }
}
