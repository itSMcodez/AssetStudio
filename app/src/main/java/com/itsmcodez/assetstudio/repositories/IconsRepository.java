package com.itsmcodez.assetstudio.repositories;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.MutableLiveData;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.itsmcodez.assetstudio.R;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.models.IconModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IconsRepository {
    private static IconsRepository instance;
    private ArrayList<IconModel> icons;
    private MutableLiveData<ArrayList<IconModel>> iconsLiveData;
    private Application application;
    private IconsLoadCallback callback;
    
    private IconsRepository(Application application) {
        this.application = application;
    	getIconsFromAsset();
    }
    
    public static synchronized IconsRepository getInstance(Application application) {
    	if(instance == null) {
    		instance = new IconsRepository(application);
    	}
        return instance;
    }
    
    public ArrayList<IconModel> getIcons() {
    	return this.icons;
    }
    
    public MutableLiveData<ArrayList<IconModel>> getIconsLiveData(IconsLoadCallback callback) {
        this.callback = callback;
    	return this.iconsLiveData;
    }
    
    private void getIconsFromAsset() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
    	icons = new ArrayList<>();
        iconsLiveData = new MutableLiveData<>();
        AssetManager manager = application.getAssets();
        String assetPath = "icons/feather/";
        
        executor.execute(() -> {
                try {
                    if(callback != null) {
                    	callback.onLoadStart();
                    }
                    String[] svgFiles = manager.list(assetPath);
                    for(String svgFile : svgFiles) {
                    	String iconName = svgFile.substring(0, svgFile.indexOf("."));
                        String iconPath = assetPath + svgFile;
                        Drawable iconPreview = getBitmapDrawable(application, new PictureDrawable(SVG.getFromAsset(manager, iconPath).renderToPicture()));
                        icons.add(new IconModel(iconName, iconPath, iconPreview));
                    }
                    iconsLiveData.postValue(icons);
                    if(callback != null) {
                    	callback.onLoadSuccess(icons);
                    }
                } catch(IOException err) {
                	err.printStackTrace();
                    if(callback != null) {
                    	callback.onLoadFailed();
                    }
                } catch(SVGParseException err) {
                    err.printStackTrace();
                    if(callback != null) {
                    	callback.onLoadFailed();
                    }
                }
        });
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
        DrawableCompat.wrap(bitmapDrawable);
        DrawableCompat.setTint(bitmapDrawable, context.getColor(R.color.default_theme_onBackground));
        return bitmapDrawable;
    }

    private Drawable getBitmapDrawable(Context context, PictureDrawable pictureDrawable) {
        // Convert PictureDrawable to Bitmap
        Bitmap bitmap = convertPictureDrawableToBitmap(pictureDrawable);
        // Bitmap to BitmapDrawable
        return getDrawableFromBitmap(context, bitmap);
    }
}
