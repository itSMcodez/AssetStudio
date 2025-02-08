package com.itsmcodez.assetstudio.preferences.fragment;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.itsmcodez.assetstudio.AboutActivity;
import com.itsmcodez.assetstudio.MainActivity;
import com.itsmcodez.assetstudio.R;
import com.itsmcodez.assetstudio.preferences.SAFPreference;
import com.itsmcodez.assetstudio.util.PathUtil;

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_ICON_PACK  = "KEY_ICON_PACK";
    public static final String ICON_PACK_FEATHER = "ICON_PACK_FEATHER";
    public static final String ICON_PACK_LUCIDE = "ICON_PACK_LUCIDE";
    public static final String ICON_PACK_TABLER  = "ICON_PACK_TABLER";
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Root layout
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());
        
        // icon pack preference
        ListPreference listOfIconPacks = new ListPreference(getContext());
        listOfIconPacks.setKey(KEY_ICON_PACK);
        listOfIconPacks.setTitle("Default Icon Pack");
        listOfIconPacks.setSummary("Choose default icon pack");
        listOfIconPacks.setEntries(new String[] {"Feather Icons", "Lucide Icons", "Tabler Icons"});
        listOfIconPacks.setEntryValues(new String[] {ICON_PACK_FEATHER, ICON_PACK_LUCIDE, ICON_PACK_TABLER});
        listOfIconPacks.setDefaultValue(ICON_PACK_FEATHER);
        listOfIconPacks.setIcon(R.drawable.ic_package);
        listOfIconPacks.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object val) {
                    MainActivity.getUpdateIntent().setAction(MainActivity.ACTION_UPDATE_ICONS);
                    return true;
                }
        });
        screen.addPreference(listOfIconPacks);
        
        // icons save location preference
        EditTextPreference location = new EditTextPreference(getContext());
        location.setKey(SAFPreference.KEY);
        location.setTitle("Save Location");
        if(location.getText() == null) {
            // Retrieve previously chosen file destination if exists
            SAFPreference SAFPref = SAFPreference.with(getContext());
            if(SAFPref != null) {
                if(!((String)SAFPref.getPreference(SAFPreference.KEY)).isEmpty()) {
                    location.setText(PathUtil.getPathFromUri(((String)SAFPref.getPreference(SAFPreference.KEY))));
                }
            }
        }
        location.setSummary(location.getText() != null ? location.getText() : "Set save location");
        location.setDialogTitle("Export Location");
        location.setDialogMessage("Set the save location for exported files");
        location.setIcon(R.drawable.ic_save_edit);
        screen.addPreference(location);
        
        // about
        Preference about = new Preference(getContext());
        about.setTitle("About");
        about.setSummary("About application");
        about.setIcon(R.drawable.ic_info);
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getContext(), AboutActivity.class));
                    return true;
                }
        });
        screen.addPreference(about);
        
        // apply preference views
        setPreferenceScreen(screen);
    }
    
}
