package com.itsmcodez.assetstudio.repositories;

import android.app.Application;
import android.graphics.Picture;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.itsmcodez.assetstudio.markers.IconPacks;
import com.itsmcodez.assetstudio.models.IconModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IconsRepository {
    private static IconsRepository INSTANCE;
    private Application application;
    private IconPacks iconPack;
    private List<IconModel> icons;
    private MutableLiveData<List<IconModel>> iconsLiveData;
    private String[] packs;

    private IconsRepository(Application application, IconPacks iconPack) {
        this.application = application;
        this.iconPack = iconPack;
        icons = Collections.synchronizedList(new ArrayList<>());
        iconsLiveData = new MutableLiveData<>();
        String folder = "icon-packs/";

        try {
            packs = application.getAssets().list(folder);
            if (packs == null) {
                throw new IOException("Asset folder is empty or not found: " + folder);
            }

            String folderName = getFolderNameForIconPack(iconPack);
            if (folderName == null) {
                throw new IOException("Unknown icon pack: " + iconPack);
            }

            String defPack = folder.concat(folderName);
            String[] svgFiles = application.getAssets().list(defPack);
            if (svgFiles == null) {
                throw new IOException("No SVG files found in: " + defPack);
            }

            for (String svgFile : svgFiles) {
                try {
                    String iconName = svgFile.substring(0, svgFile.indexOf("."));
                    String iconPath = defPack + "/" + svgFile;
                    Picture iconPreview = SVG.getFromAsset(application.getAssets(), iconPath).renderToPicture();

                    synchronized (icons) {
                        icons.add(new IconModel(iconName, iconPath, iconPreview));
                    }
                } catch (SVGParseException err) {
                    err.printStackTrace();
                    new Handler(Looper.getMainLooper())
                    .post(() -> Toast.makeText(application, "Error parsing SVG: " + svgFile, Toast.LENGTH_SHORT).show());
                }
            }
            
            synchronized(icons) {
                iconsLiveData.postValue(new ArrayList<>(icons));
            }
            
        } catch (IOException err) {
            err.printStackTrace();
            new Handler(Looper.getMainLooper())
            .post(() -> Toast.makeText(application, "An error occurred", Toast.LENGTH_SHORT).show());
        }
    }

    private String getFolderNameForIconPack(final IconPacks iconPack) {
        switch (iconPack) {
            case TABLER_ICONS:
                return "tabler-icons";
            case LUCIDE_ICONS:
                return "lucide-icons";
            case FEATHER_ICONS:
                return "feather-icons";
            default:
                return null;
        }
    }

    public static IconsRepository getInstance(Application application, IconPacks iconPack) {
        if (INSTANCE == null) {
            synchronized (IconsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IconsRepository(application, iconPack);
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    public List<IconModel> getIcons() {
        return this.icons;
    }

    @NonNull
    public MutableLiveData<List<IconModel>> getIconsLiveData() {
        return this.iconsLiveData;
    }

    public void setDefaultIconPack(@NonNull IconPacks iconPack) {
        this.iconPack = iconPack;
    }
}
