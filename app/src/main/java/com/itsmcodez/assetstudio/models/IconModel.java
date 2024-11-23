package com.itsmcodez.assetstudio.models;

import android.graphics.Picture;
import com.itsmcodez.assetstudio.markers.ModelType;
import com.itsmcodez.assetstudio.models.base.Model;

public class IconModel implements Model {
    private String name;
    private String path;
    private Picture preview;

    public IconModel(String name, String path, Picture preview) {
        this.name = name;
        this.path = path;
        this.preview = preview;
    }

    @Override
    public ModelType getModelType() {
        return ModelType.ICON_MODEL;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Picture getPreview() {
        return this.preview;
    }

    public void setPreview(Picture preview) {
        this.preview = preview;
    }

    @Override
    public String toString() {
        return "IconModel[name=" + name + ", path=" + path + "]";
    }
}
