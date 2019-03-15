package com.murphy.pokotalk.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.murphy.pokotalk.PokoTalkApp;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.main.MainActivity;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.PokoMessage;
import com.murphy.pokotalk.data.user.User;

public class PokoNotificationManager {
    protected Context context;
    protected NotificationManagerCompat notificationManagerCompat;

    public static final int NOTIFICATION_ID_CHANNEL1 = 7777;
    public static final int NOTIFICATION_ID_CHANNEL2 = 7779;

    public PokoNotificationManager(Context context) {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
    }

    public void notifyNewMessage(String channel, Group group, PokoMessage message) {
        User writer = message.getWriter();

        // Create a remoteView for customizing notification.
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.message_notification_layout);
        remoteViews.setImageViewResource(R.id.image, R.drawable.user);
        remoteViews.setTextViewText(R.id.title, writer.getNickname());
        remoteViews.setTextViewText(R.id.text, message.getContent());

        // Create an Intent for the activity you want to start
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("opcode", MainActivity.START_GROUP_CHAT);
        intent.putExtra("groupId", group.getGroupId());
        //Log.v("POKO", "NEW MESSAGE GROUP ID2.5 " + group.getGroupId());

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);

        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a bitmap for picture of message writer.
        String imageSource = message.getWriter().getPicture();
        Bitmap bitmap;
        if (imageSource == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);
        }

        // Find total message number that will be displayed at notification badge.
        DataLock.getInstance().releaseReadLock();
        int newMessageNum = DataCollection.getInstance().getTotalNewMessgaeNumber();
        DataLock.getInstance().releaseReadLock();

        int notificationId, prioirty;
        // Find priority and channel
        if (channel == PokoTalkApp.CHANNEL_1_ID) {
            prioirty = NotificationCompat.PRIORITY_HIGH;
            notificationId = NOTIFICATION_ID_CHANNEL1;
        } else {
            prioirty = NotificationCompat.PRIORITY_HIGH;
            notificationId = NOTIFICATION_ID_CHANNEL2;
        }


        // Create a notification builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel);

        if (channel == PokoTalkApp.CHANNEL_1_ID) {
            // Configure notification
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getRealContent())).
                    setSmallIcon(R.drawable.pokotalk_icon_small).
                    setContentTitle(writer.getNickname()).
                    setContentText(message.getRealContent()).
                    setContentIntent(pendingIntent).
                    setLargeIcon(bitmap).
                    // Add priority for compatibility with Android 7.1 or lower
                            setPriority(prioirty).
                    setAutoCancel(true);
        } else {
            // Configure notification
            builder.setDefaults(Notification.DEFAULT_SOUND).
                    setSmallIcon(R.drawable.pokotalk_icon_small).
                    // Add priority for compatibility with Android 7.1 or lower
                    setPriority(prioirty).
                    setAutoCancel(true);
        }

        if (newMessageNum > 0) {
            builder.setNumber(newMessageNum);
        }
        //setCustomContentView(remoteViews).
        //setCustomBigContentView(remoteViews);

        // Give notification to manager and notify.
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
