package com.itsmcodez.assetstudio;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            intent.putExtra("DEBUG_INFO", Log.getStackTraceString(throwable));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Kill the app after displaying the error
            System.exit(1);
        });
    }
}
