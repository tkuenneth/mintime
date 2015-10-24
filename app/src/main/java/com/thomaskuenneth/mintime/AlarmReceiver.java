/*
 * AlarmReceiver.java
 * 
 * Min Time (c) Thomas Künneth 2014 - 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;

/**
 * Dieser BroadcastReceiver wird verwendet, um das Gerät vibrieren zu lassen.
 *
 * @author Thomas
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String PATTERN = "pattern";

    @Override
    @TargetApi(23)
    public void onReceive(Context context, Intent intent) {
        if ((Build.VERSION.SDK_INT < 23) ||
                (context.checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED)) {
            final long[] pattern = intent.getLongArrayExtra(PATTERN);
            if (pattern != null) {
                Vibrator v = (Vibrator) context
                        .getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(pattern, -1);
                }
            }
        }
    }
}
