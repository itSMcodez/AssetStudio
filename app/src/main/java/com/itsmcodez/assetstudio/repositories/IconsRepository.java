package com.itsmcodez.assetstudio.repositories;
import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import androidx.lifecycle.MutableLiveData;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
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
                        Drawable iconPreview = new PictureDrawable(SVG.getFromAsset(manager, iconPath).renderToPicture());
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
}
