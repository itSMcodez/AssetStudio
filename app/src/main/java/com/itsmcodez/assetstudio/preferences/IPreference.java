package com.itsmcodez.assetstudio.preferences;

public interface IPreference {
    int MODE_PRIVATE = 0;
    void setValue(String key, String value);
    String getValue(String key);
    boolean onChanged(String key);
}
