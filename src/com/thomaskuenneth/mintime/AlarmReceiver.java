/*
 * AlarmReceiver.java
 * 
 * TKWeek (c) Thomas Künneth 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

/**
 * Dieser BroadcastReceiver wird verwendet, um das Gerät vibrieren zu lassen.
 * 
 * @author Thomas
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {

	public static final String PATTERN = "pattern";

	@Override
	public void onReceive(Context context, Intent intent) {
		final long[] pattern = intent.getLongArrayExtra(PATTERN);
		if (pattern != null) {
			Vibrator v = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			if (v.hasVibrator()) {
				v.vibrate(pattern, -1);
			}
		}
	}
}
