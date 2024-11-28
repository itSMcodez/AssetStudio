package com.itsmcodez.assetstudio;
import android.app.Application;
import androidx.annotation.NonNull;
import com.itsmcodez.assetstudio.markers.IconPacks;

public class BaseApplication extends Application {
    
    @NonNull
    public IconPacks getDefaultIconPack() {
    	return IconPacks.LUCIDE_ICONS;
    }
}
