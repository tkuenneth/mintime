/*
 * CountdownActivity.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Diese Activity realisiert die Zeitanzeige/Countdown.
 *
 * @author Thomas
 */
public class CountdownActivity extends AppCompatActivity implements CountdownApi {

    public static final int NOTIFICATION_ID = 29082311;
    public static final long NOTIFICATION_INTERVAL_IN_MILLIS = 60000L;

    private static final long[] PATTERN1 = new long[]{0, 800, 800, 800, 800,
            800};

    private static final long[] PATTERN2 = new long[]{0, 500, 500, 500, 500,
            500, 500, 500};

    private JSONObject data;
    private BigTime timer;
    private Animation anim;
    private boolean taskShouldBeRunning;
    private TextView tapHere;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntentOrange, alarmIntentRed,
            alarmIntentRepeating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setContentView(R.layout.countdown);
        timer = findViewById(R.id.timer);
        tapHere = findViewById(R.id.tap_here);
        tapHere.setOnClickListener(v -> {
            cancelAlarms();
            JSONUtils.putLongInJSONObject(data, MinTime.RESUMED, -1);
            NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        timer.setRedAlert(false);
        super.onPause();
        anim.cancel();
        anim.reset();
        taskShouldBeRunning = false;
        MinTime.saveData(this, data);
        data = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        tapHere.setTextColor(Color.BLACK);
        data = MinTime.loadData(this);
        if (data == null) {
            throw new IllegalStateException("data == null");
        }

        long now = System.currentTimeMillis();
        if (JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED) == -1) {
            JSONUtils.putLongInJSONObject(data, MinTime.RESUMED, now);
        }

        long elapsedRealtime = SystemClock.elapsedRealtime();
        long offset = now
                - JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED);
        elapsedRealtime -= offset;

        // wiederkehrender Alarm
        Intent intentRepeating = new Intent(this, RepeatingAlarm.class);
        intentRepeating.putExtra(MinTime.END, getEnd());
        intentRepeating.putExtra(MinTime.RESUMED,
                JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED));
        alarmIntentRepeating = PendingIntent.getBroadcast(this,
                MinTime.RQ_ALARM_REPEATING, intentRepeating,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Eintritt in die Phase orange
        Intent intentOrange = new Intent(this, AlarmReceiver.class);
        intentOrange.putExtra(AlarmReceiver.PATTERN, PATTERN1);
        alarmIntentOrange = PendingIntent.getBroadcast(this,
                MinTime.RQ_ALARM_ORANGE, intentOrange, 0);
        // Eintritt in die Phase rot
        Intent intentRed = new Intent(this, AlarmReceiver.class);
        intentRed.putExtra(AlarmReceiver.PATTERN, PATTERN2);
        alarmIntentRed = PendingIntent.getBroadcast(this, MinTime.RQ_ALARM_RED,
                intentRed, 0);
        cancelAlarms();
        long phaseGreen = JSONUtils.getLongFromJSONObject(data,
                MinTime.COUNTER1);
        long phaseOrange = JSONUtils.getLongFromJSONObject(data,
                MinTime.COUNTER2);
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
        return JSONUtils.getLongFromJSONObject(data, MinTime.COUNTER1)
                + JSONUtils.getLongFromJSONObject(data, MinTime.COUNTER2)
                + JSONUtils.getLongFromJSONObject(data, MinTime.COUNTER3);
    }

    private long getEnd() {
        long resumed = JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED);
        long total = getTotal();
        return resumed + total;
    }

    public long getElpased() {
        return System.currentTimeMillis()
                - JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED);
    }

    public long getRemaining() {
        long end = getEnd();
        return end - System.currentTimeMillis();
    }

    public BigTime getTimer() {
        return timer;
    }

    public JSONObject getData() {
        return data;
    }

    public Animation prepareAnimation() {
        tapHere.setTextColor(Color.WHITE);
        return anim;
    }

    public boolean taskShouldBeRunning() {
        return taskShouldBeRunning;
    }
}
