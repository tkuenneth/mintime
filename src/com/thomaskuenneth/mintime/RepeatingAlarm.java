/*
 * RepeatingAlarm.java
 * 
 * Min Time (c) Thomas Künneth 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Diese Klasse stellt eine Benachrichtigung dar.
 * 
 * @author Thomas
 *
 */
public class RepeatingAlarm extends BroadcastReceiver {

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		long end = intent.getLongExtra(MinTime.END, -1);
		if (end != -1) {
			long notification_mins = CountdownActivity.NOTIFICATION_INTERVAL_IN_MILLIS / 1000 / 60;
			long remaining = end - System.currentTimeMillis();
			int resId;
			if (remaining < 0) {
				remaining = -remaining;
				resId = R.string.overrun;
			} else {
				resId = R.string.left;
			}
			long secs = (remaining / 1000l);
			long mins = secs / 60;
			if (mins == 0) {
				mins = notification_mins;
			}
			Intent intentCountDownActivity = new Intent(context,
					CountdownActivity.class);
			PendingIntent notificationClickedIntent = PendingIntent
					.getActivity(context, MinTime.RQ_NOTIFICATION,
							intentCountDownActivity,
							PendingIntent.FLAG_CANCEL_CURRENT);
			String str = context.getString(resId, mins,
					context.getString(R.string.min));
			Notification n = new Notification.Builder(context).setOngoing(true)
					.setContentTitle(context.getString(R.string.app_name))
					.setContentText(str)
					.setSmallIcon(R.drawable.ic_launcher_mintime)
					.setContentIntent(notificationClickedIntent)
					.setOngoing(true).build();
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(CountdownActivity.NOTIFICATION_ID, n);
		}
	}
}
