package com.itsmcodez.assetstudio.common;
import com.itsmcodez.assetstudio.models.SearchModel;

public interface IconSearchCallback {
    void onPublishResult(SearchModel.SearchResult result);
    void onSearch(CharSequence constraint, boolean isRunning);
}
