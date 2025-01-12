package com.itsmcodez.assetstudio.preferences.base;

public interface IPreference {
    default void putPreference(String key, String value) {};
    default void putPreference(String key, int value) {};
    default void putPreference(String key, boolean value) {};
    Object getPreference(String key);
}
