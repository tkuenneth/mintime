/*
 * CountdownActivity.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.thomaskuenneth.mintime.databinding.CountdownBinding;

/**
 * Diese Activity realisiert die Zeitanzeige/Countdown.
 *
 * @author Thomas
 */
public class CountdownActivity extends AppCompatActivity implements CountdownApi {

    public static final String KEY_FINISH = "finish";
    public static final int NOTIFICATION_ID = 29082311;
    public static final long NOTIFICATION_INTERVAL_IN_MILLIS = 60000L;

    private static final long[] PATTERN1 = new long[]{0, 800, 800, 800, 800,
            800};

    private static final long[] PATTERN2 = new long[]{0, 500, 500, 500, 500,
            500, 500, 500};
    private static final int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    private Animation anim;
    private boolean taskShouldBeRunning;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntentOrange, alarmIntentRed,
            alarmIntentRepeating;

    private SharedPreferences prefs;

    private CountdownBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        alarmMgr = getSystemService(AlarmManager.class);
        binding = CountdownBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.tapHere.setOnClickListener(v -> {
            cancelAlarms();
            prefs.edit().putLong(MinTime.RESUMED, -1).apply();
            NotificationManager m = getSystemService(NotificationManager.class);
            if (m != null) {
                m.cancel(NOTIFICATION_ID);
            }
            Intent intent = new Intent(CountdownActivity.this,
                    MinTime.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
    }

    @Override
    protected void onPause() {
        binding.timer.setRedAlert(false);
        super.onPause();
        anim.cancel();
        anim.reset();
        taskShouldBeRunning = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean shouldFinish = false;
        if (intent != null) {
            shouldFinish = intent.hasExtra(KEY_FINISH);
        }
        if (shouldFinish) {
            binding.tapHere.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView tv = new TextView(this);
        binding.tapHere.setTextColor(tv.getCurrentTextColor());
        long now = System.currentTimeMillis();
        if (prefs.getLong(MinTime.RESUMED, -1) == -1) {
            prefs.edit().putLong(MinTime.RESUMED, now).apply();
        }
        // wiederkehrender Alarm
        Intent intentRepeating = new Intent(this, RepeatingAlarm.class);
        intentRepeating.putExtra(MinTime.END, getEnd());
        intentRepeating.putExtra(MinTime.RESUMED,
                prefs.getLong(MinTime.RESUMED, now));
        alarmIntentRepeating = PendingIntent.getBroadcast(this,
                MinTime.RQ_ALARM_REPEATING, intentRepeating,
                INTENT_FLAGS);
        // Eintritt in die Phase orange
        Intent intentOrange = new Intent(this, AlarmReceiver.class);
        intentOrange.putExtra(AlarmReceiver.PATTERN, PATTERN1);
        alarmIntentOrange = PendingIntent.getBroadcast(this,
                MinTime.RQ_ALARM_ORANGE, intentOrange, INTENT_FLAGS);
        // Eintritt in die Phase rot
        Intent intentRed = new Intent(this, AlarmReceiver.class);
        intentRed.putExtra(AlarmReceiver.PATTERN, PATTERN2);
        alarmIntentRed = PendingIntent.getBroadcast(this, MinTime.RQ_ALARM_RED,
                intentRed, INTENT_FLAGS);
        cancelAlarms();


        long elapsedRealtime = SystemClock.elapsedRealtime();
        long offset = now
                - prefs.getLong(MinTime.RESUMED, now);
        elapsedRealtime -= offset;
        long phaseGreen = prefs.getLong(MinTime.COUNTER1, now);
        long phaseOrange = prefs.getLong(MinTime.COUNTER2, now);
        if (offset <= phaseGreen) {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                    + phaseGreen, alarmIntentOrange);
        }
        if (offset <= phaseGreen + phaseOrange) {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                    + phaseGreen + phaseOrange, alarmIntentRed);
        }
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                elapsedRealtime /* + phaseGreen */, NOTIFICATION_INTERVAL_IN_MILLIS,
                alarmIntentRepeating);
        taskShouldBeRunning = true;
        CountdownTask task = new CountdownTask(this);
        task.execute();
    }

    private void cancelAlarms() {
        alarmMgr.cancel(alarmIntentOrange);
        alarmMgr.cancel(alarmIntentRed);
        alarmMgr.cancel(alarmIntentRepeating);
    }

    private long getTotal() {
        long now = System.currentTimeMillis();
        return prefs.getLong(MinTime.COUNTER1, now)
                + prefs.getLong(MinTime.COUNTER2, now)
                + prefs.getLong(MinTime.COUNTER3, now);
    }

    private long getEnd() {
        long resumed = prefs.getLong(MinTime.RESUMED, System.currentTimeMillis());
        long total = getTotal();
        return resumed + total;
    }

    public long getElpased() {
        return System.currentTimeMillis()
                - prefs.getLong(MinTime.RESUMED, System.currentTimeMillis());
    }

    public long getRemaining() {
        long end = getEnd();
        return end - System.currentTimeMillis();
    }

    public BigTime getTimer() {
        return binding.timer;
    }

    public Animation prepareAnimation() {
        binding.tapHere.setTextColor(Color.WHITE);
        return anim;
    }

    public boolean taskShouldBeRunning() {
        return taskShouldBeRunning;
    }
}
