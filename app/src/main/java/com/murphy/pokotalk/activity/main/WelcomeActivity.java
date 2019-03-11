package com.murphy.pokotalk.activity.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.service.PokoTalkService;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeActivity extends AppCompatActivity implements ServiceConnection {
    private CircleImageView pokoImage;
    private TextView appNameText;
    private final int splash_time = 600;
    private boolean writePermission;
    private Thread thread;
    private Messenger serviceMessenger;
    private final Messenger myMessenger = new Messenger(new ServiceMessageHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        /* Find views */
        pokoImage = (CircleImageView) findViewById(R.id.pokoImage);
        appNameText = (TextView) findViewById(R.id.appNameText);

        /* Show animation */
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.welcome_action);
        pokoImage.startAnimation(anim);
        appNameText.startAnimation(anim);

        /* Check permissions for this app */
        /* Now PokoTalk only works when read/write permissions are granted */
        writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        ArrayList<String> permissions = new ArrayList<>();

        // TODO: Explain user for permission if the user denied the permission
        if (!writePermission) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
            }
        }

        thread = new Thread() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                /* Sleep sometimes and show MainActivity */
                try{
                    sleep(splash_time);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                startActivityForResult(intent, 0);
            }
        };

        if (writePermission) {
            PokoTalkService.startPokoTalkService(this);
            PokoTalkService.bindPokoTalkService(this, this);
        } else {
            /* Request for permissions */
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    Constants.ALL_PERMISSION);
        }

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        /* Unbind service */
        PokoTalkService.unbindPokoTalkService(this, this);

        super.onDestroy();
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
            PokoTalkService.startPokoTalkService(this);
            PokoTalkService.bindPokoTalkService(this, this);
        } else {
            finish();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceMessenger = new Messenger(service);

        try {
            /* Send message to service */
            Message message = Message.obtain(null, PokoTalkService.NOTIFY_WHEN_LOADED);
            message.replyTo = myMessenger;
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceMessenger = null;
        PokoTalkService.startPokoTalkService(this);
    }

    class ServiceMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PokoTalkService.NOTIFY_WHEN_LOADED: {
                    thread.start();
                    return;
                }
            }
        }
    }
}
