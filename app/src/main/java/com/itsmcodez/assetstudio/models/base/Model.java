package com.itsmcodez.assetstudio.models.base;
import androidx.annotation.Nullable;
import com.itsmcodez.assetstudio.markers.ModelType;

@FunctionalInterface
public interface Model {
    @Nullable ModelType getModelType();
}
