package com.murphy.pokotalk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.PokoDatabase;
import com.murphy.pokotalk.data.file.PokoDatabaseManager;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;

import java.util.ArrayDeque;

/** PokoTalk service process running in background which manages
 * 1. Server connection
 * 2. Loading and Saving user data
 */
public class PokoTalkService extends Service {
    private PokoServer server;
    private boolean sessionLoaded = false;
    private boolean appStarted = false;
    private ArrayDeque<Messenger> waitingRequests = new ArrayDeque<>();
    private final Messenger requestMessenger = new Messenger(new ServiceRequestHandler());
    private PokoNotificationManager notificationManager;
    private PokoDatabase pokoDB;

    /* PokoMessage names */
    public static final int NOTIFY_WHEN_LOADED = 0;
    public static final int APP_FOREGROUND = 1;
    public static final int APP_BACKGROUND = 2;

    public PokoTalkService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server = PokoServer.getInstance(this);
        pokoDB = PokoDatabase.getInstance(this);

        Log.v("POKO", "POKO service made, process id " + Process.myPid());

        /* Settings for notification channels */
        notificationManager = new PokoNotificationManager(this);

        /* Start AsyncTask */
        new sessionLoadAsyncTask().execute(this);

        /* Attach callbacks */
        server.attachActivityCallback(Constants.newMessageName, newMessageCallback);
        Log.v("POKO", "ATTACH SERVICE NEW MESSAGE CALLBACK");
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
        Log.v("POKO", "DETACH SERVICE NEW MESSAGE CALLBACK");
        //server.detachActivityCallback(Constants.newMessageName, newMessageCallback);

        /* Restart service again */
        Intent broadcastIntent = new Intent("com.murphy.pokotalk.SERVICE_RESTART");
        sendBroadcast(broadcastIntent);
        pokoDB.close();
        pokoDB = null;

        super.onDestroy();
    }

    public void loadApplicationData() {
        /* Loads application data */
        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                Context context = getApplicationContext();
                PokoDatabaseManager.loadSessionData(context);
                PokoDatabaseManager.loadUserData(context);
                PokoDatabaseManager.loadGroupData(context);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* PokoMessage handler for service requests */
    class ServiceRequestHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch(msg.what) {
                    case NOTIFY_WHEN_LOADED: {
                        Messenger replyMessenger = msg.replyTo;
                        isSessionLoaded(replyMessenger);
                        return;
                    }
                    case APP_FOREGROUND: {
                        appStarted = true;
                        Log.v("POKO", "APP STARTED");
                        return;
                    }
                    case APP_BACKGROUND: {
                        Log.v("POKO", "APP CLOSED");
                        appStarted = false;
                        return;
                    }
                    default: {
                        return;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* Getter and Setter */
    public PokoServer getServer() {
        return server;
    }

    /* Session loaded things */

    /* This AsyncTask loads session and user data,
     * and try to connect to server.
     */
    class sessionLoadAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            PokoTalkService service = PokoTalkService.this;
            if (service.isSessionLoaded()) {
                return null;
            }

            service.loadApplicationData();
            Session session = Session.getInstance();
            /* Try to login if session data is loaded */
            if (session.sessionIdExists()) {
                session.login(service.getApplicationContext());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                PokoTalkService.this.setSessionLoaded(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isSessionLoaded() {
        return sessionLoaded;
    }

    public synchronized boolean isSessionLoaded(Messenger messenger) throws RemoteException {
        if (!sessionLoaded) {
            waitingRequests.addLast(messenger);
        } else {
            sendNotifyWhenLoaded(messenger);
        }

        return sessionLoaded;
    }

    protected synchronized void setSessionLoaded(boolean sessionLoaded) throws RemoteException {
        if (sessionLoaded) {
            for (Messenger messenger : waitingRequests) {
                sendNotifyWhenLoaded(messenger);
            }
        }
        this.sessionLoaded = sessionLoaded;
        waitingRequests.clear();
    }

    private void sendNotifyWhenLoaded(Messenger messenger) throws RemoteException {
        Message message = Message.obtain(null, NOTIFY_WHEN_LOADED);
        messenger.send(message);
    }

    /* Activity callbacks */
    private ActivityCallback newMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Group group = (Group) getData("group");
            PokoMessage message = (PokoMessage) getData("message");
            if (group != null && message != null) {
                if (appStarted) {
                    Group chatGroup = DataCollection.getInstance().getChattingGroup();
                    // Notify when the user is not on chat of new message.
                    if (chatGroup != group) {
                        notificationManager.notifyNewMessage(
                                PokoTalkApp.CHANNEL_2_ID, group, message);
                    }
                } else {
                    // Notify the user with high importance notification.
                    Log.v("POKO", "NEW MESSAGE GROUP ID2 " + group.getGroupId());
                    notificationManager.notifyNewMessage(
                            PokoTalkApp.CHANNEL_1_ID, group, message);
                }
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* Service management methods */

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
