package com.itsmcodez.assetstudio.callbacks;
import com.itsmcodez.assetstudio.models.IconModel;
import java.util.ArrayList;

public interface IconsLoadCallback {
    void onLoadSuccess(ArrayList<IconModel> icons);
    void onLoadFailed();
}