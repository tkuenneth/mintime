/*
 * AlarmReceiver.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String PATTERN = "pattern";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.checkSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            final long[] pattern = intent.getLongArrayExtra(PATTERN);
            if (pattern != null) {
                PowerManager pm = context.getSystemService(PowerManager.class);
                if (pm != null) {
                    Vibrator v = context.getSystemService(Vibrator.class);
                    if (v != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createWaveform(pattern, -1),
                                    new AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                            .setUsage(AudioAttributes.USAGE_ALARM)
                                            .build());
                        } else {
                            v.vibrate(pattern, -1);
                        }
                    }
                }
            }
        }
    }
}
