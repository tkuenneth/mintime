/*
 * AlarmReceiver.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.Vibrator;

/**
 * Dieser BroadcastReceiver wird verwendet, um das Gerät vibrieren zu lassen.
 *
 * @author Thomas
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String PATTERN = "pattern";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            final long[] pattern = intent.getLongArrayExtra(PATTERN);
            if (pattern != null) {
                PowerManager pm = context.getSystemService(PowerManager.class);
                if (pm != null) {
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mintime:AlarmReceiver");
                    wl.acquire(MinTime.ONE_MINUTE);
                    Vibrator v = context.getSystemService(Vibrator.class);
                    if (v != null) {
                        v.vibrate(pattern, -1);
                    }
                    wl.release();
                }
            }
        }
    }
}
