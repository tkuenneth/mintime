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
import static com.thomaskuenneth.mintime.MinTime.ACTION_COUNTDOWN;
import static com.thomaskuenneth.mintime.MinTime.RESUMED;
import static com.thomaskuenneth.mintime.NotificationStatus.DEFAULT;
import static com.thomaskuenneth.mintime.NotificationStatus.NOTIFICATIONS_OFF;
import static com.thomaskuenneth.mintime.NotificationStatus.NOTIFICATION_CHANNEL_OFF;
import static com.thomaskuenneth.mintime.NotificationStatus.SILENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.List;

public class RepeatingAlarm extends BroadcastReceiver {

    public static final String CHANNEL_ID = "RepeatingAlarm_20251228";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getLong(RESUMED, -1) == -1) {
            return;
        }
        long resumed = intent.getLongExtra(RESUMED, -1);
        long end = intent.getLongExtra(MinTime.END, -1);
        long durationGreen = intent.getLongExtra(MinTime.COUNTER1, 0);
        long durationOrange = intent.getLongExtra(MinTime.COUNTER2, 0);
        long durationRed = intent.getLongExtra(MinTime.COUNTER3, 0);
        if (resumed != -1) {
            long now = System.currentTimeMillis();
            long remaining = end - now;
            long elapsed = now - resumed;
            long duration = end - resumed;
            Intent countdownIntent = new Intent(context,
                    MinTime.class);
            countdownIntent.setAction(ACTION_COUNTDOWN);
            PendingIntent countdownPendingIntent = PendingIntent
                    .getActivity(context, MinTime.RQ_COUNTDOWN,
                            countdownIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Intent cancelIntent = new Intent(context, MinTime.class);
            cancelIntent.setAction(MinTime.ACTION_CANCEL);
            PendingIntent cancelPendingIntent = PendingIntent.getActivity(
                    context,
                    MinTime.RQ_CANCEL,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            String contentTitle = remaining < 0 ? context.getString(R.string.time_is_up)
                    : context.getString(R.string.left, ((remaining / 1000L) / 60 == 0 ? (MinTime.NOTIFICATION_INTERVAL_IN_MILLIS / 1000 / 60) : (remaining / 1000L) / 60), context.getString(R.string.min));
            String contentText = context.getString(R.string.running, (elapsed + 59999) / 60000);

            int progress = 0;
            int segmentLengthGreen = 0;
            int segmentLengthOrange = 0;
            int segmentLengthRed = 0;
            if (duration > 0) {
                progress = (int) Math.min(1000, (elapsed * 1000L) / duration);
                segmentLengthGreen = (int) ((durationGreen * 1000) / duration);
                segmentLengthOrange = (int) ((durationOrange * 1000) / duration);
                segmentLengthRed = 1000 - segmentLengthGreen - segmentLengthOrange;
            }

            String phaseName;
            if (elapsed < durationGreen) {
                phaseName = context.getString(R.string.info2_short);
            } else if (elapsed < (durationGreen + durationOrange)) {
                phaseName = context.getString(R.string.info3_short);
            } else if (elapsed < (durationGreen + durationOrange + durationRed)) {
                phaseName = context.getString(R.string.info4_short);
            } else {
                phaseName = context.getString(R.string.time_is_up);
            }

            NotificationCompat.ProgressStyle.Segment segmentGreen = new NotificationCompat.ProgressStyle.Segment(segmentLengthGreen)
                    .setColor(context.getColor(R.color.green));
            NotificationCompat.ProgressStyle.Segment segmentOrange = new NotificationCompat.ProgressStyle.Segment(segmentLengthOrange)
                    .setColor(context.getColor(R.color.orange));
            NotificationCompat.ProgressStyle.Segment segmentRed = new NotificationCompat.ProgressStyle.Segment(segmentLengthRed)
                    .setColor(context.getColor(R.color.red));
            NotificationCompat.ProgressStyle progressStyle = new NotificationCompat.ProgressStyle()
                    .setProgressSegments(Arrays.asList(segmentGreen, segmentOrange, segmentRed))
                    .setProgress(progress)
                    .setProgressTrackerIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_mintime));

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(contentTitle)
                    .setWhen(end)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_mintime_monochrome)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentText(contentText)
                    .setSubText(phaseName)
                    .setStyle(progressStyle)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setRequestPromotedOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(countdownPendingIntent)
                    .addAction(R.drawable.outline_cancel_24,
                            context.getString(R.string.cancel),
                            cancelPendingIntent)
                    .addAction(R.drawable.ic_mintime_monochrome,
                            context.getString(R.string.open_app),
                            countdownPendingIntent);
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
            // an older, no longer used one
            nm.deleteNotificationChannel(RepeatingAlarm.class.getName());
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getString(R.string.notification_channel_description));
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
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