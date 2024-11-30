package com.itsmcodez.assetstudio.preferences;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ExportPathPreference implements IPreference {
    public static final String EXPORT_PATH_PREF  = "EXPORT_PATH_PREF";
    public static final String KEY_ICON_EXPORT_PATH = "KEY_ICON_EXPORT_PATH";
    private static ExportPathPreference INSTANCE;
    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor PREFERENCE_EDITOR;
    
    private ExportPathPreference(Context context) {
    	this.context = context;
        sp = context.getSharedPreferences(EXPORT_PATH_PREF, MODE_PRIVATE);
        PREFERENCE_EDITOR = sp.edit();
    }

    public static ExportPathPreference with(Context context) {
    	if(INSTANCE == null) {
    		INSTANCE = new ExportPathPreference(context);
    	}
        return INSTANCE;
    }
    
    @Override
    public void setValue(String key, String value) {
        if(key.equals(KEY_ICON_EXPORT_PATH)) {
        	PREFERENCE_EDITOR.putString(KEY_ICON_EXPORT_PATH, value);
            PREFERENCE_EDITOR.commit();
            PREFERENCE_EDITOR.apply();
            if(!(sp.getString(KEY_ICON_EXPORT_PATH, "").equals(""))) {
            	onChanged(key);
            }
        }
    }

    @Override
    public String getValue(String key) {
        if(key.equals(KEY_ICON_EXPORT_PATH)) {
            return sp.getString(key, "");
        }
        return null;
    }

    @Override
    public boolean onChanged(String key) {
        Toast.makeText(context, "Updated default directory", Toast.LENGTH_LONG).show();
        return true;
    }
}
