package com.itsmcodez.assetstudio.repositories;

import android.app.Application;
import android.content.res.Configuration;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class IconsRepository {
    private static IconsRepository INSTANCE;
    private Application application;
    private IconPacks iconPack;
    private List<IconModel> icons;
    private MutableLiveData<List<IconModel>> iconsLiveData;
    private String[] packs;
    private final ExecutorService EXECUTOR;

    private IconsRepository(Application application, IconPacks iconPack) {
        this.application = application;
        this.iconPack = iconPack;
        EXECUTOR = Executors.newFixedThreadPool(4);
        icons = Collections.synchronizedList(new ArrayList<>());
        iconsLiveData = new MutableLiveData<>();
        String rootFolder = "icon-packs/";

        try {
            // 1. retrieve folders of icon packs from root folder
            packs = application.getAssets().list(rootFolder);
            if (packs == null) {
                throw new IOException("Asset folder is empty or not found: " + rootFolder);
            }

            // 2. retrieve default/selected icon pack folder
            String iconPackFolder = getFolderNameForIconPack(iconPack);
            if (iconPackFolder == null) {
                throw new IOException("Unknown icon pack: " + iconPack);
            }

            // 3. form folder path and retrieve all svg files
            String defPackFolder = rootFolder.concat(iconPackFolder);
            String[] svgFiles = application.getAssets().list(defPackFolder);
            if (svgFiles == null) {
                throw new IOException("No SVG files found in: " + defPackFolder);
            }

            // 4. construct IconModel for each svg file and add it to icons on a background thread
            EXECUTOR.execute(() -> {
                    
                    for (String svgFile : svgFiles) {
                        try {
                            String iconName = svgFile.substring(0, svgFile.indexOf("."));
                            String iconPath = defPackFolder + "/" + svgFile;
                            Picture iconPreview = extractSvg(iconPath, getThemeBasedColor()).renderToPicture();
                            synchronized (icons) {
                                icons.add(new IconModel(iconName, iconPath, iconPreview));
                            }
                        } catch(SVGParseException err) {
                            err.printStackTrace();
                            new Handler(Looper.getMainLooper())
                            .post(() -> Toast.makeText(application, "Error parsing SVG: " + svgFile, Toast.LENGTH_SHORT).show());
                        } catch(IOException err) {
                            err.printStackTrace();
                            new Handler(Looper.getMainLooper())
                            .post(() -> Toast.makeText(application, "An error occurred", Toast.LENGTH_SHORT).show());
                        }
                    }
                    
                    // 5. post icons to iconsLiveData
                    synchronized(icons) {
                        iconsLiveData.postValue(icons);
                    }
            });
            
        } catch(IOException err) {
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

    private SVG extractSvg(String svgPath, String strokeColor) throws IOException, SVGParseException {
        InputStream inputStream = application.getAssets().open(svgPath);
        String svgContent = new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

        // Replace color placeholder
        svgContent = svgContent.replaceAll("currentColor", strokeColor);

        // Load updated SVG
        SVG svg = SVG.getFromString(svgContent);
        return svg;
    }

    private String getThemeBasedColor() {
        int nightModeFlags = application.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode - Use white tint
            return "#ffffff";
        }
        // Light mode - Use black tint
        return "#000000";
    }
}
