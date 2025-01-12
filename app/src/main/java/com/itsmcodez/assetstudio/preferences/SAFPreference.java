package com.itsmcodez.assetstudio.preferences;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.itsmcodez.assetstudio.preferences.base.IPreference;

public class SAFPreference implements IPreference {
    public static final String KEY = "KEY";
    private static SAFPreference instance;
    private final String SP_NAME = "SAF_PREF";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;
    
    private SAFPreference(Context context) {
    	this.context = context;
        sp = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
        editor = sp.edit();
    }
    
    public static SAFPreference with(Context context) {
    	if(instance == null) {
    		instance = new SAFPreference(context);
    	}
        return instance;
    }
    
    @Override
    public void putPreference(String key, String value) {
        if(key.equalsIgnoreCase(KEY)) {
        	editor.putString(KEY, value);
            editor.commit();
            editor.apply();
        }
    }
    
    @Override
    public Object getPreference(String key) {
        if(key.equalsIgnoreCase(KEY)) return sp.getString(KEY, "");
        return "";
    }
    
}
