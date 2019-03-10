/*
 * RepeatingAlarm.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Diese Klasse stellt eine Benachrichtigung dar.
 *
 * @author Thomas
 */
public class RepeatingAlarm extends BroadcastReceiver {

    private static final String CHANNEL_ID = RepeatingAlarm.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        long end = intent.getLongExtra(MinTime.END, -1);
        long now = System.currentTimeMillis();
        long resumed = intent.getLongExtra(MinTime.RESUMED, now);
        if (end != -1) {
            long notification_mins = CountdownActivity.NOTIFICATION_INTERVAL_IN_MILLIS / 1000 / 60;
            long remaining = end - now;
            int resId;
            if (remaining < 0) {
                remaining = -remaining;
                resId = R.string.overrun;
            } else {
                resId = R.string.left;
            }
            long secs = (remaining / 1000L);
            long mins = secs / 60;
            if (mins == 0) {
                mins = notification_mins;
            }
            Intent intentCountDownActivity = new Intent(context,
                    CountdownActivity.class);
            PendingIntent notificationClickedIntent = PendingIntent
                    .getActivity(context, MinTime.RQ_NOTIFICATION,
                            intentCountDownActivity,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            String str = context.getString(resId, mins,
                    context.getString(R.string.min));
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    context, CHANNEL_ID).setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(str)
                    .setWhen(resumed)
                    .setSmallIcon(R.drawable.ic_launcher_mintime)
                    .setContentIntent(notificationClickedIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat
                    .from(context);
            notificationManager.notify(CountdownActivity.NOTIFICATION_ID,
                    notificationBuilder.build());
        }
    }
}
