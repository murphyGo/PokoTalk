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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.murphy.pokotalk.data.ChatManager;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.Session;
import com.murphy.pokotalk.data.db.PokoDatabaseHelper;
import com.murphy.pokotalk.data.db.PokoDatabaseManager;
import com.murphy.pokotalk.data.db.PokoSessionDatabase;
import com.murphy.pokotalk.data.db.PokoUserDatabase;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.service.PokoNotificationManager;
import com.murphy.pokotalk.service.PokoTalkService;
import com.naver.maps.map.NaverMapSdk;

import java.util.ArrayList;
import java.util.List;

public class PokoTalkApp extends Application
        implements LifecycleObserver,
        ServiceConnection {
    private static PokoTalkApp instance;

    // Server and notification manager
    private PokoServer server;
    private PokoNotificationManager notificationManager;

    // Naver map cloud id
    private static final String NAVER_MAP_CLIENT_ID = "rlj06wv3wv";

    // Volley request queue
    public RequestQueue requestQueue;

    // Notification channels
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";

    // Service messenger
    protected Messenger serviceMessenger;

    // Foreground
    public boolean foreground;

    // App data loading variables
    private boolean appDataLoaded = false;
    private int appDataLoadState = 0;
    private List<Runnable> appDataLoadedCallback;

    // Constants for app data load result state
    public static final int LOAD_SUCCESS = 1;
    public static final int LOAD_NO_DATA = 2;
    public static final int LOAD_FAIL = 3;

    // Logout variable
    public boolean logout = false;

    public static PokoTalkApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Save application instance
        instance = this;

        // Get server
        server = PokoServer.getInstance();

        // Start connection
        server.connect(this);

        // Get notification manager
        notificationManager = new PokoNotificationManager(this);

        // Initialize app data loaded callback list
        appDataLoadedCallback = new ArrayList<>();

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

        // Start data load AsyncTask
        new DataLoadAsyncTask().execute(this);

        // Attach callbacks
        server.attachActivityCallback(Constants.newMessageName, newMessageCallback);

        Log.v("POKO", "APP START");
    }

    /* This AsyncTask loads session and user data,
     * and try to connect to server.
     */
    static class DataLoadAsyncTask
            extends AsyncTask<PokoTalkApp, Void, PokoTalkApp> {
        PokoTalkApp app;

        @Override
        protected PokoTalkApp doInBackground(PokoTalkApp[] objects) {
            // Get application
            app = objects[0];

            // Load application data
            loadApplicationData();

            // Get session
            Session session = Session.getInstance();

            // Try to login if session data is loaded
            if (session.sessionIdExists()) {
                PokoServer server = PokoServer.getInstance();
                server.sendSessionLogin(session.getSessionId());
            }

            return app;
        }

        @Override
        protected void onPostExecute(PokoTalkApp app) {
            // Notify data loaded
            if (app.appDataLoaded) {
                for (Runnable callback : app.appDataLoadedCallback) {
                    callback.run();
                }

                // Clear callbacks
                app.appDataLoadedCallback.clear();
            }
        }

        public void loadApplicationData() {
            // Get context
            Context context = app.getApplicationContext();

            /* Loads application data */
            try {
                DataLock.getInstance().acquireWriteLock();

                try {
                    // Load session data
                    PokoDatabaseManager.loadSessionData(context);

                    // Get session and user
                    Session session = Session.getInstance();
                    Contact user = session.getUser();

                    if (user != null) {
                        // Create SQLite database
                        PokoUserDatabase db = PokoUserDatabase.getInstance(context, user.getUserId());

                        // Make sure tables exists
                        db.makeSureTableExists();

                        // Load user application data
                        PokoDatabaseManager.loadUserData(context);
                        PokoDatabaseManager.loadGroupData(context);
                        PokoDatabaseManager.loadEventData(context);
                    }

                    // Check if session id exists
                    if (Session.getInstance().sessionIdExists()) {
                        // Set state success
                        app.appDataLoadState = app.LOAD_SUCCESS;
                    } else {
                        // Session id must exist, no data;
                        app.appDataLoadState = app.LOAD_NO_DATA;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Error loading app data
                    app.appDataLoadState = app.LOAD_FAIL;
                } finally {
                    DataLock.getInstance().releaseWriteLock();

                    // Set application data loaded true
                    app.appDataLoaded = true;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // App data loaded callback
    public void notifyWhenAppDataLoaded(Runnable runnable) {
        if (appDataLoaded) {
            runnable.run();
        } else {
            synchronized (this) {
                if (appDataLoaded) {
                    runnable.run();
                } else {
                    appDataLoadedCallback.add(runnable);
                }
            }
        }
    }

    // Do all the stuffs to logout user in application
    public void logoutUser() {
        // Set logout
        logout = true;

        // Send logout and disconnect
        server.sendLogout();
        server.disconnect();

        // Reset session
        Session session = Session.getInstance();
        session.setSessionId(null);
        session.setUser(null);

        // Remove session data
        PokoSessionDatabase database = PokoSessionDatabase.getInstance(this);
        SQLiteDatabase db = database.getWritableDatabase();
        PokoDatabaseHelper.deleteAllSessionData(db);
        db.close();

        // Disable database job
        PokoDatabaseManager.getInstance().disable();

        /** From here, socket will not receive an event anymore.
         *  Also, no database job is processing anymore. */

        // We simulate like app data is loaded but data not exists
        appDataLoaded = true;
        appDataLoadState = LOAD_NO_DATA;

        try {
            DataLock.getInstance().acquireWriteLock();

            // Reset application data
            DataCollection.reset();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            DataLock.getInstance().releaseWriteLock();
        }

        // Reconnect to server with new socket
        server.connect(this);
    }

    public void startLoadingApplicationData() {
        // Logout job done, set logout false
        logout = false;

        // Reset application data loaded state
        appDataLoaded = false;
        appDataLoadState = 0;

        // Start data load AsyncTask
        new DataLoadAsyncTask().execute(this);
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

    // This method create notification channels, it will work only for the first time
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

    /* Activity callbacks */
    private ActivityCallback newMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Group group = (Group) getData("group");
            PokoMessage message = (PokoMessage) getData("message");
            if (group != null && message != null) {

                // Notify only when it is not my message
                if (!message.isMyMessage(Session.getInstance())) {
                    if (foreground) {
                        Group chatGroup = ChatManager.getChattingGroup();
                        // Notify with sound in app but when the user is not on chat of new message.
                        if (chatGroup == null || chatGroup.getGroupId() != group.getGroupId()) {
                            notificationManager.notifySoundInApp();
                        }
                    } else {
                        // Notify the user with high importance notification.
                        Log.v("POKO", "NEW MESSAGE GROUP ID2 " + group.getGroupId());
                        notificationManager.notifyNewMessage(
                                PokoTalkApp.CHANNEL_1_ID, group, message);
                    }
                }
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    /* Getters and Setters */
    public RequestQueue getVolleyRequestQueue() {
        return requestQueue;
    }
    public boolean isAppDataLoaded() { return appDataLoaded; }
    public int getAppDataLoadState() { return appDataLoadState; }
    public boolean isLogout() {
        return logout;
    }

}
