/*
 * RepeatingAlarm.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.mintime;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.thomaskuenneth.mintime.MinTime.RESUMED;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.List;

public class RepeatingAlarm extends BroadcastReceiver {

    public static final String CHANNEL_ID = RepeatingAlarm.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getLong(RESUMED, -1) != -1) {
            initNotificationChannels(context);
            long end = intent.getLongExtra(MinTime.END, -1);
            long resumed = intent.getLongExtra(RESUMED, -1);
            long now = System.currentTimeMillis();
            long notificationMinutes = MinTime.NOTIFICATION_INTERVAL_IN_MILLIS / 1000 / 60;
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
                mins = notificationMinutes;
            }
            Intent intentCountDownActivity = new Intent(context,
                    MinTime.class);
            PendingIntent notificationClickedIntent = PendingIntent
                    .getActivity(context, MinTime.RQ_NOTIFICATION,
                            intentCountDownActivity,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            String str = context.getString(resId, mins,
                    context.getString(R.string.min));
            StringBuilder sb = new StringBuilder();
            if (resumed != -1) {
                long elapsed = now - resumed;
                long running = (elapsed + 59999) / 60000;
                sb.append(context.getString(R.string.running, running));
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentTitle(str)
                    .setShowWhen(false)
                    .setSmallIcon(R.drawable.ic_mintime_monochrome)
                    .setOngoing(true)
                    .setContentText(sb.toString())
                    .setContentIntent(notificationClickedIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat
                    .from(context);
            if (context.checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                notificationManager.notify(MinTime.NOTIFICATION_ID,
                        notificationBuilder.build());
            }
        }
    }

    public static void initNotificationChannels(Context context) {
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_descr));
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }
    }

    public static boolean shouldCheckNotificationSettings(NotificationManager nm) {
        if (!nm.areNotificationsEnabled())
            return true;
        List<NotificationChannel> channels = nm.getNotificationChannels();
        for (NotificationChannel channel : channels) {
            if (channel.getImportance() <= NotificationManager.IMPORTANCE_LOW) {
                return true;
            }
        }
        return false;
    }
}
