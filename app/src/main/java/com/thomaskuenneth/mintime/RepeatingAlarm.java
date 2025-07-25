/*
 * MIT License
 *
 * Copyright (c) 2014 - 2025 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.thomaskuenneth.mintime;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.thomaskuenneth.mintime.MinTime.RESUMED;
import static com.thomaskuenneth.mintime.NotificationStatus.DEFAULT;
import static com.thomaskuenneth.mintime.NotificationStatus.NOTIFICATIONS_OFF;
import static com.thomaskuenneth.mintime.NotificationStatus.NOTIFICATION_CHANNEL_OFF;
import static com.thomaskuenneth.mintime.NotificationStatus.SILENT;

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
            Intent cancelIntent = new Intent(context, MinTime.class);
            cancelIntent.setAction(MinTime.ACTION_CANCEL);
            PendingIntent cancelPendingIntent = PendingIntent.getActivity(
                    context,
                    MinTime.RQ_CANCEL,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            String str = remaining < 0 ? context.getString(R.string.time_is_up)
                    : context.getString(R.string.left, mins, context.getString(R.string.min));
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
                    .setContentIntent(notificationClickedIntent)
                   .addAction(R.drawable.outline_cancel_24,
                    context.getString(R.string.cancel),
                    cancelPendingIntent);
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

    public static NotificationStatus getNotificationStatus(NotificationManager nm) {
        if (!nm.areNotificationsEnabled())
            return NOTIFICATIONS_OFF;
        List<NotificationChannel> channels = nm.getNotificationChannels();
        for (NotificationChannel channel : channels) {
            int importance = channel.getImportance();
            if (importance == NotificationManager.IMPORTANCE_NONE) {
                return NOTIFICATION_CHANNEL_OFF;
            }
            if (importance <= NotificationManager.IMPORTANCE_LOW) {
                return SILENT;
            }
        }
        return DEFAULT;
    }
}
