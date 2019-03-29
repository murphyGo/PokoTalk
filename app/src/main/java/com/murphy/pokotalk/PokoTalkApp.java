package com.murphy.pokotalk;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.murphy.pokotalk.service.PokoTalkService;
import com.naver.maps.map.NaverMapSdk;

public class PokoTalkApp extends Application
        implements LifecycleObserver,
        ServiceConnection {
    private static PokoTalkApp instance;
    private static final String NAVER_MAP_CLIENT_ID = "rlj06wv3wv";
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    protected Messenger serviceMessenger;
    public boolean foreground;
    public RequestQueue requestQueue;

    public static PokoTalkApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Save application instance
        instance = this;

        // Create notification channels
        createNotificationChannels();

        // Initialize foreground variable
        foreground = false;

        // Add application lifecycle observer
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // Register Naver map client ID
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_MAP_CLIENT_ID));

        // Setup simple Volley request queue
        requestQueue = Volley.newRequestQueue(this);
    }

    // Called when application is in foreground
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground(){
        Log.d("LifecycleObserver ","Foreground");
        foreground = true;
        sendAppForeground();

        // We should start service and bind to service when app is in foreground.
        // If the app is in background, starting service will cause an error.
        // Bind to service
        if (serviceMessenger == null) {
            Context context = getApplicationContext();
            PokoTalkService.startPokoTalkService(context);
            PokoTalkService.bindPokoTalkService(context, this);
        }
    }

    // Called when application is in background
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d("LifecycleObserver ","Background");
        foreground = false;
        sendAppBackground();
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("PokoTalk message notification.");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("In app message notification.");
            channel2.setShowBadge(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }

    /* Service callbacks */
    // Called when bound to service.
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceMessenger = new Messenger(service);
        if (foreground) {
            sendAppForeground();
        }
    }

    // Called when service crashes for some reason.
    @Override
    public void onServiceDisconnected(ComponentName name) {
        // Try to reconnect to server
        serviceMessenger = null;
        Context context = getApplicationContext();
        PokoTalkService.startPokoTalkService(context);
        PokoTalkService.bindPokoTalkService(context, this);
    }

    /* Service call methods */
    // Notify service that application is in foreground
    public void sendAppForeground() {
        try {
            if (serviceMessenger != null) {
                Message message = Message.obtain(null, PokoTalkService.APP_FOREGROUND);
                serviceMessenger.send(message);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Notify service that application is in background
    public void sendAppBackground() {
        try {
            if (serviceMessenger != null) {
                Message message = Message.obtain(null, PokoTalkService.APP_BACKGROUND);
                serviceMessenger.send(message);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* Getters and Setters */
    public RequestQueue getVolleyRequestQueue() {
        return requestQueue;
    }
}
