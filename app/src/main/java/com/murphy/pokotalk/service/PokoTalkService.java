package com.murphy.pokotalk.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.FileManager;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.User;
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
    private FileManager fileManager;
    private boolean sessionLoaded = false;
    private ArrayDeque<Messenger> waitingRequests = new ArrayDeque<>();
    private final Messenger requestMessenger = new Messenger(new ServiceRequestHandler());
    private NotificationManagerCompat notificationManagerCompat;

    /* PokoMessage names */
    public static final int NOTIFY_WHEN_LOADED = 0;

    public PokoTalkService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server = PokoServer.getInstance(this);
        fileManager = FileManager.getInstance();
        Log.v("POKO", "POKO service made, process id " + Process.myPid());

        /* Settings for notification channels */
        notificationManagerCompat = NotificationManagerCompat.from(this);

        /* Start AsyncTask */
        new sessionLoadAsyncTask().execute(this);

        /* Attach callbacks */
        server.attachActivityCallback(Constants.newMessageName, newMessageCallback);
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
        return requestMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        /* Detach callbacks */
        server.detachActivityCallback(Constants.newMessageName, newMessageCallback);

        super.onDestroy();
    }

    public void loadAppilcationData() {
        /* Temporarily thread by welcome activity does application data loading */
        /* Load application data */
        FileManager fileManager = FileManager.getInstance();
        fileManager.loadSession();
        fileManager.loadContactList();
        fileManager.loadPendingContactList();
        fileManager.loadStragerList();
        fileManager.loadGroupList();
        //TODO: Only load last messages first.
        fileManager.loadMessages();
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

    public FileManager getFileManager() {
        return fileManager;
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

            service.loadAppilcationData();
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
    }

    private void sendNotifyWhenLoaded(Messenger messenger) throws RemoteException {
        Message message = Message.obtain(null, NOTIFY_WHEN_LOADED);
        messenger.send(message);
    }

    /* New message notification */
    public void sendNotificationForMessage(PokoMessage message) {
        User writer = message.getWriter();
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.message_notification_layout);
        remoteViews.setImageViewResource(R.id.image, R.drawable.user);
        remoteViews.setTextViewText(R.id.title, writer.getNickname());
        remoteViews.setTextViewText(R.id.text, message.getContent());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, PokoTalkApp.CHANNEL_1_ID);

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.
                DecoratedMediaCustomViewStyle()).
                setSmallIcon(R.drawable.cockatiel_icon).
                setContentTitle(writer.getNickname()).
                setContentText(message.getContent()).
                setAutoCancel(true).
                setCustomContentView(remoteViews).
                setCustomBigContentView(remoteViews);

        //NotificationCompat.Builder builder =
         //       new NotificationCompat.Builder(this, PokoTalkApp.CHANNEL_2_ID);

        //notificationManagerCompat.notify(2, builder.build());
        notificationManagerCompat.notify(1, builder.build());
    }

    /* Activity callbacks */
    private ActivityCallback newMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            PokoMessage message = (PokoMessage) getData("message");
            if (message != null) {
                sendNotificationForMessage(message);
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };
}
