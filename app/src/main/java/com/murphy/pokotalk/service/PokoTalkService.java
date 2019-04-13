package com.murphy.pokotalk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.util.Log;

/** PokoTalk service process running in background which manages
 * 1. Server connection
 * 2. Loading and Saving user data
 */
public class PokoTalkService extends Service {
    private boolean appStarted = false;
    private final Messenger requestMessenger = new Messenger(new ServiceRequestHandler(this));

    /* PokoMessage names */
    public static final int APP_FOREGROUND = 1;
    public static final int APP_BACKGROUND = 2;

    public PokoTalkService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("POKO", "CREATE SERVICE " + this.toString());
        Log.v("POKO", "POKO service made, process id " + Process.myPid());
    }

    /* Service must start with startService method */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* PokoTalk service must persist so it can receive data in real time */
        return START_STICKY;
    }

    /* Service can get bound to clients */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("POKO", "ON BIND");

        return requestMessenger.getBinder();
    }

    /* When all bound client unbinds */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.v("POKO", "ON UNBIND");

        return false;
    }

    @Override
    public void onDestroy() {
        /* Detach callbacks */
        Log.v("POKO", "DESTROY SERVICE");

        /* Restart service again */
        Intent broadcastIntent = new Intent("com.murphy.pokotalk.SERVICE_RESTART");
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }

    /* PokoMessage handler for service requests */
    static class ServiceRequestHandler extends Handler {
        PokoTalkService service;

        public ServiceRequestHandler(PokoTalkService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case APP_FOREGROUND: {
                    service.appStarted = true;
                    Log.v("POKO", "APP STARTED");
                    return;
                }
                case APP_BACKGROUND: {
                    Log.v("POKO", "APP CLOSED");
                    service.appStarted = false;
                    return;
                }
                default: {
                    return;
                }
            }
        }
    }

    /* Starts PokoTalk service */
    public static void startPokoTalkService(Context context) {
        Intent intent = new Intent(context, PokoTalkService.class);
        context.startService(intent);
        Log.v("POKO", "START SERVICE");
    }

    /* Bind PokoTalk service */
    public static void bindPokoTalkService(Context context, ServiceConnection conn) {
        /* Bind to PokoTalk service */
        Intent intent = new Intent(context, PokoTalkService.class);
        context.bindService(intent, conn, BIND_AUTO_CREATE);
        Log.v("POKO", "BIND SERVICE");
    }

    /* Unbind PokoTalk service */
    public static void unbindPokoTalkService(Context context, ServiceConnection conn) {
        /* Bind to PokoTalk service */
        context.unbindService(conn);
        Log.v("POKO", "UNBIND SERVICE");
    }
}
