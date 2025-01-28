package com.itsmcodez.assetstudio;

import android.content.DialogInterface;
import android.widget.TextView;
import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display logs or errors
        Intent intent = getIntent();
        String debugInfo = intent.getStringExtra("DEBUG_INFO");
        
        AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("An error occurred!")
        .setMessage(debugInfo != null ? debugInfo : "No debug information available")
        .setNeutralButton("Exit Application", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    finish();
                }
        })
        .create();
        dialog.show();
    }
}