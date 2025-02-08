package com.itsmcodez.assetstudio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.itsmcodez.assetstudio.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    private ActivityAboutBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    public void openGitProject(View v) {
    	startActivity(
            new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(binding.githubDesc.getText().toString()))
        );
    }
    
    public void openTelegramGroup(View v) {
    	startActivity(
            new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(binding.teleDesc.getText().toString()))
        );
    }
}
