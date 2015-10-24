/*
 * CountdownActivity.java
 * 
 * Min Time (c) Thomas Künneth 2014 - 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import org.json.JSONObject;

/**
 * Diese Activity realisiert die Zeitanzeige/Countdown.
 *
 * @author Thomas
 */
public class CountdownActivity extends Activity {

    public static final int NOTIFICATION_ID = 29082311;
    public static final long NOTIFICATION_INTERVAL_IN_MILLIS = 60000l;

    private static final long[] PATTERN1 = new long[]{0, 800, 800, 800, 800,
            800};

    private static final long[] PATTERN2 = new long[]{0, 500, 500, 500, 500,
            500, 500, 500};

    private JSONObject data;
    private BigTime timer;
    private AsyncTask<Void, Long, Void> task;
    private Animation anim;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntentOrange, alarmIntentRed,
            alarmIntentRepeating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setContentView(R.layout.countdown);
        timer = (BigTime) findViewById(R.id.timer);
        timer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                cancelAlarms();
                JSONUtils.putLongInJSONObject(data, MinTime.RESUMED, -1);
                NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                m.cancel(NOTIFICATION_ID);
                Intent intent = new Intent(CountdownActivity.this,
                        MinTime.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
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
        task.cancel(true);
        task = null;
        MinTime.saveData(this, data);
        data = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                elapsedRealtime + phaseGreen, NOTIFICATION_INTERVAL_IN_MILLIS,
                alarmIntentRepeating);
        task = new AsyncTask<Void, Long, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                while (!isCancelled() && (data != null)) {
                    try {
                        long remaining = getRemaining();
                        publishProgress(remaining);
                        if (remaining < 0) {
                            remaining = -remaining;
                        }
                        Thread.sleep(remaining >= 150000 ? 60000 : 1000);
                    } catch (InterruptedException e) {
                        // keine Log-Ausgabe nötig - der Thread darf ja
                        // jederzeit unterbrochen werden
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                if (data != null) {
                    long remaining = values[0];
                    boolean startAnimation = false;
                    if (remaining < 0) {
                        startAnimation = true;
                        remaining = -remaining;
                    }
                    long elapsed = getElpased();
                    int color;
                    if (elapsed <= JSONUtils.getLongFromJSONObject(data,
                            MinTime.COUNTER1)) {
                        color = R.color.green;
                    } else if (elapsed <= (JSONUtils.getLongFromJSONObject(
                            data, MinTime.COUNTER1) + JSONUtils
                            .getLongFromJSONObject(data, MinTime.COUNTER2))) {
                        color = R.color.orange;
                    } else {
                        color = R.color.red;
                    }
                    timer.setColor(getResources().getColor(color));
                    long secs = remaining / 1000;
                    if (secs >= 60) {
                        timer.setText(getString(R.string.template, secs / 60,
                                getString(R.string.min)));
                    } else {
                        timer.setText(getString(R.string.template, secs,
                                getString(R.string.sec)));
                    }
                    if (startAnimation) {
                        timer.startAnimation(anim);
                        timer.setRedAlert(true);
                    }
                }
            }
        };
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

    private long getElpased() {
        return System.currentTimeMillis()
                - JSONUtils.getLongFromJSONObject(data, MinTime.RESUMED);
    }

    private long getRemaining() {
        long end = getEnd();
        return end - System.currentTimeMillis();
    }
}
