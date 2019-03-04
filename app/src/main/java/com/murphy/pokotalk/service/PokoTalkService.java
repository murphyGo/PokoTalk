package com.murphy.pokotalk.service;

import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.main.MainActivity;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.file.FileManager;
import com.murphy.pokotalk.data.group.Group;
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
    private boolean appStarted = false;
    private ArrayDeque<Messenger> waitingRequests = new ArrayDeque<>();
    private final Messenger requestMessenger = new Messenger(new ServiceRequestHandler());
    private NotificationManagerCompat notificationManagerCompat;

    /* PokoMessage names */
    public static final int NOTIFY_WHEN_LOADED = 0;
    public static final int APP_STARTED = 1;
    public static final int APP_CLOSED = 2;

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

        super.onDestroy();
    }

    public void loadAppilcationData() {
        /* Temporarily thread by welcome activity does application data loading */
        /* Load application data */
        try {
            DataLock.getInstance().acquireWriteLock();

            fileManager.loadSession();
            fileManager.loadContactList();
            fileManager.loadPendingContactList();
            fileManager.loadStragerList();
            fileManager.loadContactGroupRelations();
            fileManager.loadGroupList();
            fileManager.loadLastMessages();

            DataLock.getInstance().releaseWriteLock();
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
                    case APP_STARTED: {
                        appStarted = true;
                        return;
                    }
                    case APP_CLOSED: {
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
        waitingRequests.clear();
    }

    private void sendNotifyWhenLoaded(Messenger messenger) throws RemoteException {
        Message message = Message.obtain(null, NOTIFY_WHEN_LOADED);
        messenger.send(message);
    }

    /* New message notification */
    public void sendNotificationForMessage(Group group, PokoMessage message) {
        User writer = message.getWriter();
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.message_notification_layout);
        remoteViews.setImageViewResource(R.id.image, R.drawable.user);
        remoteViews.setTextViewText(R.id.title, writer.getNickname());
        remoteViews.setTextViewText(R.id.text, message.getContent());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, PokoTalkApp.CHANNEL_1_ID);

        // Create an Intent for the activity you want to start
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("opcode", MainActivity.START_GROUP_CHAT);
        intent.putExtra("groupId", group.getGroupId());
        Log.v("POKO", "NEW MESSAGE GROUP ID2.5 " + group.getGroupId());

// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
// Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setStyle(new android.support.v4.media.app.NotificationCompat.
                DecoratedMediaCustomViewStyle()).
                setSmallIcon(R.drawable.cockatiel_icon).
                setContentTitle(writer.getNickname()).
                setContentText(message.getContent()).
                setContentIntent(pendingIntent).
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
            Group group = (Group) getData("group");
            PokoMessage message = (PokoMessage) getData("message");
            if (group != null && message != null && !appStarted) {
                Log.v("POKO", "NEW MESSAGE GROUP ID2 " + group.getGroupId()); 
                sendNotificationForMessage(group, message);
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
