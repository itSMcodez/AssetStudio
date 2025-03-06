package com.itsmcodez.assetstudio.repositories;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.MutableLiveData;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.itsmcodez.assetstudio.R;
import com.itsmcodez.assetstudio.callbacks.IconsLoadCallback;
import com.itsmcodez.assetstudio.common.IconPack;
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
        icons = new ArrayList<>();
        iconsLiveData = new MutableLiveData<>();
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
    
    public MutableLiveData<ArrayList<IconModel>> getIconsLiveData(@NonNull IconPack pack, IconsLoadCallback callback) {
        this.callback = callback;
        getIconsFromAsset(pack);
    	return this.iconsLiveData;
    }
    
    public void refreshIcons(IconPack pack) {
    	getIconsFromAsset(pack);
    }
    
    private void getIconsFromAsset(IconPack pack) {
        if(icons.size() > 0) {
        	icons.clear();
            iconsLiveData.setValue(icons);
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AssetManager manager = application.getAssets();
        String assetPath = getPackAssetFolder(pack);
        
        executor.execute(() -> {
                try {
                    if(callback != null) {
                    	callback.onLoadStart();
                    }
                    String[] svgFiles = manager.list(assetPath);
                    for(String svgFile : svgFiles) {
                    	String iconName = svgFile.substring(0, svgFile.indexOf("."));
                        String iconPath = assetPath + svgFile;
                        SVG svg = SVG.getFromAsset(manager, iconPath);
                        Drawable iconPreview = getBitmapDrawable(application, new PictureDrawable(svg.renderToPicture()));
                        icons.add(new IconModel(iconName, iconPath, svg, iconPreview));
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
    
    private String getPackAssetFolder(IconPack pack) {
        switch(pack) {
            case FEATHER : return "icons/feather/";
            case LUCIDE : return "icons/lucide/";
            case TABLER : return "icons/tabler/";
            default:
                return null;
        }
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
