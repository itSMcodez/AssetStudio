package com.itsmcodez.assetstudio.models;

import android.graphics.drawable.Drawable;
import com.itsmcodez.assetstudio.adapters.IconsAdapter;

public class IconModel implements IconsAdapter.MultiSelection.Selectable {
    private String name, assetPath;
    private Drawable preview;
    private boolean selected;

    public IconModel(String name, String assetPath, Drawable preview) {
        this.name = name;
        this.assetPath = assetPath;
        this.preview = preview;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetPath() {
        return this.assetPath;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

    public Drawable getPreview() {
        return this.preview;
    }

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

    @Override
    public String toString() {
        return "IconModel[name=" + name + ", assetPath=" + assetPath + "]";
    }

    @Override
    public boolean getSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
