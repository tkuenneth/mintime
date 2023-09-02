/*
 * AlarmReceiver.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.mintime;

import static android.Manifest.permission.VIBRATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String PATTERN = "pattern";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.checkSelfPermission(VIBRATE) == PERMISSION_GRANTED) {
            final long[] pattern = intent.getLongArrayExtra(PATTERN);
            if (pattern != null) {
                PowerManager pm = context.getSystemService(PowerManager.class);
                if (pm != null) {
                    Vibrator v = context.getSystemService(Vibrator.class);
                    if (v != null) {
                        v.vibrate(VibrationEffect.createWaveform(pattern, -1),
                                new AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .build());
                    }
                }
            }
        }
    }
}
