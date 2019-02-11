package com.murphy.pokotalk.activity.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.FileManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity {
    private CircleImageView pokoImage;
    private TextView appNameText;
    private final int splash_time = 1200;
    private boolean writePermission;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pokoImage = (CircleImageView) findViewById(R.id.pokoImage);
        appNameText = (TextView) findViewById(R.id.appNameText);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.welcome_action);
        pokoImage.startAnimation(anim);
        appNameText.startAnimation(anim);

        /* Check permissions for this app */
        /* Now PokoTalk only works when read/write permissions are granted */
        writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        ArrayList<String> permissions = new ArrayList<>();

        if (!writePermission) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
            }
        }

        final Intent intent = new Intent(this, MainActivity.class);

        thread = new Thread() {
            @Override
            public void run() {
                /* Temporarily thread by welcome activity does application data loading */
                /* Load application data */
                FileManager fileManager = FileManager.getInstance();
                fileManager.loadSession();
                /* Login session */
                Session session = Session.getInstance();
                if (session.sessionIdExists()) {
                    session.login(getApplicationContext());
                }

                /* Sleep sometimes and show MainActivity */
                try{
                    sleep(splash_time);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                finish();
            }
        };

        if (writePermission) {
            thread.start();
        } else {
            /* Request for permissions */
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    Constants.ALL_PERMISSION);
        }
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
        }

        if (writePermission) {
            thread.start();
        } else {
            finish();
        }
    }
}
