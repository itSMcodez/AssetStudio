package com.itsmcodez.assetstudio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.itsmcodez.assetstudio.databinding.ActivitySettingsBinding;
import com.itsmcodez.assetstudio.preferences.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content, new SettingsFragment())
        .commit();
    }
}
