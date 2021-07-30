package com.murphy.pokotalk.activity.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity {
    private CircleImageView pokoImage;
    private TextView appNameText;
    private final int splashTime = 200;
    private boolean writePermission;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find views
        pokoImage = findViewById(R.id.pokoImage);
        appNameText = findViewById(R.id.appNameText);

        // Show animation
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.welcome_action);
        pokoImage.startAnimation(anim);
        appNameText.startAnimation(anim);

        // Check permissions for this app
        // Now PokoTalk only requires write permission as minimum
        writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        // Permission string lists
        ArrayList<String> permissions = new ArrayList<>();

        if (!writePermission) {
            // Add to permission list
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO: Explain user for permission if the user denied the permission
            } else {

            }
        }

        // Thread that sleeps for splash time and start MainActivity
        thread = new Thread() {
            @Override
            public void run() {
                // Intent to start MainActivity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                // Sleep sometimes and start MainActivity
                try{
                    sleep(splashTime);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                // Start MainActivity
                startActivityForResult(intent, 0);
            }
        };

        // Check permissions
        if (writePermission) {
            // Wait for application data to be loaded
            waitForAppDataLoaded();
        } else {
            // Request for permissions
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    Constants.ALL_PERMISSION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // MainActivity closed, finish this activity
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.ALL_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (String permission : permissions) {
                        switch (permission) {
                            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                                writePermission = true;
                                break;
                        }
                    }
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }

        // Check if the user granted the permission
        if (writePermission) {
            // Granted permission, wait for app data loaded
            waitForAppDataLoaded();
        } else {
            // User must grant write permission to use PokoTalk
            finish();
        }
    }

    // Wait for application data to be loaded and start thread
    private void waitForAppDataLoaded() {
        PokoTalkApp app = PokoTalkApp.getInstance();
        if (app.isAppDataLoaded()) {
            thread.start();
        } else {
            app.notifyWhenAppDataLoaded(appDataLoadedCallback);
        }
    }

    // Callback starts when application data is loaded
    private Runnable appDataLoadedCallback = new Runnable() {
        @Override
        public void run() {
            // App data loaded, start thread
            if (thread != null) {
                thread.start();
            }
        }
    };
}
