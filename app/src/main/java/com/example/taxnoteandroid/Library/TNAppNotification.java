package com.example.taxnoteandroid.Library;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.NotificationCompat;

import com.example.taxnoteandroid.MainActivity;
import com.example.taxnoteandroid.R;

/**
 * Created by b0ne on 2017/03/15.
 */

public class TNAppNotification {

    private Context mContext;
    private String mTitle;
    private String mMessage;
    private int mNotificationId = 0;

    public static final int DAILY_ALERT_INPUT_FORGET_ID = 10;

    public TNAppNotification(Context context) {
        this.mContext = context;
    }

    public TNAppNotification(Context context, String title, String message) {
        this.mContext = context;
        this.mTitle = title;
        this.mMessage = message;
        this.mNotificationId = DAILY_ALERT_INPUT_FORGET_ID;
    }

    public TNAppNotification(Context context, String title, String message, int notificationId) {
        this.mContext = context;
        this.mTitle = title;
        this.mMessage = message;
        this.mNotificationId = notificationId;
    }

    public static TNAppNotification newInstanceForAlertInputForget(Context context) {
        String title = context.getString(R.string.app_name);
        String message = context.getString(R.string.alert_input_forget_notify_message);
        TNAppNotification appNotification = new TNAppNotification(context, title, message);
        return appNotification;
    }

    public void show() {
        long when = System.currentTimeMillis();
        Intent toIntent = new Intent(mContext, MainActivity.class);
        toIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        toIntent.setAction(Long.toString(when));

        // Notification pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, DAILY_ALERT_INPUT_FORGET_ID,
                toIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Notification builder
        NotificationCompat.Builder ntBuilder = new NotificationCompat.Builder(mContext);
        ntBuilder.setWhen(when)
                .setContentTitle(mTitle)
                .setContentText(mMessage)
                .setContentIntent(pendingIntent);

        int defsVal = Notification.DEFAULT_VIBRATE;
        defsVal |= Notification.DEFAULT_SOUND;
        defsVal |= Notification.DEFAULT_LIGHTS;
        ntBuilder.setDefaults(defsVal);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ntBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            ntBuilder.setColor(ContextCompat.getColor(mContext, R.color.primary));
            ntBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        ntBuilder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
        ntBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.ic_launcher));

        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle(ntBuilder);
        bigTextStyle.setBigContentTitle(mTitle)
                .bigText(mMessage);

        Notification notification = bigTextStyle.build();
        notification.flags = Notification.FLAG_SHOW_LIGHTS;

        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, notification);
    }

    /**
     * Notificationの削除
     * @param context
     * @param notificationId
     */
    public static void cancel(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
