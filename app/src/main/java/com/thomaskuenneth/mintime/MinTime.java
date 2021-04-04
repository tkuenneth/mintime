/*
 * MinTime.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.thomaskuenneth.mintime.databinding.MainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Dies ist die Hauptactivity der App.
 *
 * @author Thomas
 */
public class MinTime extends AppCompatActivity
        implements ValueUpdater {

    public static final int NOTIFICATION_ID = 29082311;
    public static final int RQ_ALARM_ORANGE = 1;
    public static final int RQ_ALARM_RED = 2;
    public static final int RQ_ALARM_REPEATING = 3;
    public static final int RQ_NOTIFICATION = 4;
    public static final String COUNTER1 = "counter1";
    public static final String COUNTER2 = "counter2";
    public static final String COUNTER3 = "counter3";
    public static final String RESUMED = "resumed";
    public static final String END = "end";
    public static final long ONE_MINUTE = 60000L;
    public static final long NOTIFICATION_INTERVAL_IN_MILLIS = ONE_MINUTE;

    private static final int INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    private static final String TAG = MinTime.class.getSimpleName();
    private static final String FILENAME_DISTRIBUTIONS = TAG + ".dst";
    private static final String DST = "distributions";
    private static final long[] PATTERN1 = new long[]{0, 800, 800, 800, 800,
            800};
    private static final long[] PATTERN2 = new long[]{0, 500, 500, 500, 500,
            500, 500, 500};

    private static PendingIntent alarmIntentOrange = null;
    private static PendingIntent alarmIntentRed = null;
    private static PendingIntent alarmIntentRepeating = null;

    private boolean taskShouldBeRunning;
    private Animation anim;
    private AlarmManager alarmMgr;
    private MainBinding binding;
    private List<String> distributions;
    private SharedPreferences prefs;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains(COUNTER1) || !prefs.contains(COUNTER2) || !prefs.contains(COUNTER3)) {
            updatePrefs(ONE_MINUTE, ONE_MINUTE, ONE_MINUTE);
        }
        loadDistributions();
        alarmMgr = getSystemService(AlarmManager.class);
        binding = MainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        // setup
        binding.start.setOnClickListener(v -> {
            prefs.edit().putLong(MinTime.RESUMED, System.currentTimeMillis()).apply();
            configureAlarms();
            updateUI();
        });
        binding.counter1.setValueUpdator(this);
        binding.counter2.setValueUpdator(this);
        binding.counter3.setValueUpdator(this);
        // countdown
        binding.tapHere.setOnClickListener(v -> {
            NotificationManager m = getSystemService(NotificationManager.class);
            if (m != null) {
                m.cancel(NOTIFICATION_ID);
            }
            prefs.edit().putLong(MinTime.RESUMED, -1).apply();
            cancelAlarms();
            stopAnimationAndTask();
            updateUI();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAnimationAndTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeGroup(1);
        if (!isResumed()) {
            for (String d : distributions) {
                String[] vals = d.split("\\|");
                final long val1 = Long.parseLong(vals[0]);
                final long val2 = Long.parseLong(vals[1]);
                final long val3 = Long.parseLong(vals[2]);
                String dd = getString(R.string.template_two_dashes,
                        MinTimeUtils.millisToPrettyString(this, val1),
                        MinTimeUtils.millisToPrettyString(this, val2),
                        MinTimeUtils.millisToPrettyString(this, val3));
                MenuItem item = menu.add(1, Menu.NONE, Menu.NONE, dd);
                item.setOnMenuItemClickListener(item1 -> {
                    updateViews(val1, val2, val3);
                    invalidateOptionsMenu();
                    return true;
                });
            }
            final String distribution = createDistribution();
            if (isSavedDistribution(distribution)) {
                MenuItem delete = menu
                        .add(1, Menu.NONE, Menu.NONE, R.string.delete);
                delete.setIcon(R.drawable.ic_delete);
                delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                delete.setOnMenuItemClickListener(item -> {
                    distributions.remove(distribution);
                    saveDistributions();
                    invalidateOptionsMenu();
                    return true;
                });
            } else if ((binding.counter1.getValueInMillis() > 0) ||
                    (binding.counter2.getValueInMillis() > 0) ||
                    (binding.counter3.getValueInMillis() > 0)) {
                MenuItem save = menu.add(1, Menu.NONE, Menu.NONE, R.string.save);
                save.setIcon(R.drawable.ic_save);
                save.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                save.setOnMenuItemClickListener(item -> {
                    distributions.add(distribution);
                    saveDistributions();
                    invalidateOptionsMenu();
                    return true;
                });
            }
            if ((binding.counter1.getValueInMillis() > 0) ||
                    (binding.counter2.getValueInMillis() > 0) ||
                    (binding.counter3.getValueInMillis() > 0)) {
                MenuItem clear = menu
                        .add(1, Menu.NONE, Menu.NONE, R.string.clear);
                clear.setIcon(R.drawable.ic_clear);
                clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                clear.setOnMenuItemClickListener(item -> {
                    updateViews(0, 0, 0);
                    invalidateOptionsMenu();
                    return true;
                });
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void updateValue() {
        long val1 = binding.counter1.getValueInMillis();
        long val2 = binding.counter2.getValueInMillis();
        long val3 = binding.counter3.getValueInMillis();
        updateTotal(val1, val2, val3);
        updatePrefs(val1, val2, val3);
        invalidateOptionsMenu();
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

    private void updateViews(long val1, long val2, long val3) {
        binding.counter1.setValueInMillis(val1);
        binding.counter2.setValueInMillis(val2);
        binding.counter3.setValueInMillis(val3);
        updateTotal(val1, val2, val3);
    }

    private void updatePrefs(long val1, long val2, long val3) {
        prefs.edit().putLong(COUNTER1, val1)
                .putLong(COUNTER2, val2)
                .putLong(COUNTER3, val3)
                .apply();
    }

    private void saveDistributions() {
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        for (String s : distributions) {
            array.put(s);
        }
        try {
            data.put(DST, array);
            MinTimeUtils.saveJSONObject(this, data, FILENAME_DISTRIBUTIONS);
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Schreiben der Zeitverteilungen", e);
        }
    }

    private void loadDistributions() {
        distributions = new ArrayList<>();
        JSONObject data = MinTimeUtils
                .loadJSONObject(this, FILENAME_DISTRIBUTIONS);
        if (data != null) {
            try {
                JSONArray array = data.getJSONArray(DST);
                for (int i = 0; i < array.length(); i++) {
                    distributions.add(array.getString(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Fehler beim Lesen der Zeitverteilungen", e);
            }
        }
    }

    private String createDistribution() {
        long now = System.currentTimeMillis();
        long c1 = prefs.getLong(COUNTER1, now);
        long c2 = prefs.getLong(COUNTER2, now);
        long c3 = prefs.getLong(COUNTER3, now);
        return c1 + "|" + c2 + "|" + c3;
    }

    private boolean isSavedDistribution(String distribution) {
        for (String d : distributions) {
            if (d.equals(distribution)) {
                return true;
            }
        }
        return false;
    }

    private void updateTotal(long val1, long val2, long val3) {
        binding.total.setText(MinTimeUtils.millisToPrettyString(this, val1 + val2 + val3));
        binding.start.setEnabled(val1 + val2 + val3 > 0);
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

    private void configureAlarms() {
        Intent intentRepeating = new Intent(this, RepeatingAlarm.class);
        intentRepeating.putExtra(END, getEnd());
        intentRepeating.putExtra(RESUMED,
                prefs.getLong(RESUMED, System.currentTimeMillis()));
        alarmIntentRepeating = PendingIntent.getBroadcast(this,
                RQ_ALARM_REPEATING, intentRepeating,
                INTENT_FLAGS);

        Intent intentOrange = new Intent(this, AlarmReceiver.class);
        intentOrange.putExtra(AlarmReceiver.PATTERN, PATTERN1);
        alarmIntentOrange = PendingIntent.getBroadcast(this,
                RQ_ALARM_ORANGE, intentOrange, INTENT_FLAGS);
        // Eintritt in die Phase rot
        Intent intentRed = new Intent(this, AlarmReceiver.class);
        intentRed.putExtra(AlarmReceiver.PATTERN, PATTERN2);
        alarmIntentRed = PendingIntent.getBroadcast(this, RQ_ALARM_RED,
                intentRed, INTENT_FLAGS);
    }

    private void cancelAlarms() {
        if (alarmIntentOrange != null) {
            alarmMgr.cancel(alarmIntentOrange);
            alarmIntentOrange = null;
        }
        if (alarmIntentRed != null) {
            alarmMgr.cancel(alarmIntentRed);
            alarmIntentRed = null;
        }
        if (alarmIntentRepeating != null) {
            alarmMgr.cancel(alarmIntentRepeating);
            alarmIntentRepeating = null;
        }
    }

    private boolean isResumed() {
        return prefs.getLong(MinTime.RESUMED, -1) != -1;
    }

    private void updateUI() {
        ActionBar ab = getSupportActionBar();
        if (isResumed()) {
            getTimer().setRedAlert(false);
            TextView tv = new TextView(this);
            binding.tapHere.setTextColor(tv.getCurrentTextColor());
            long now = System.currentTimeMillis();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long offset = now
                    - prefs.getLong(MinTime.RESUMED, now);
            elapsedRealtime -= offset;
            long phaseGreen = prefs.getLong(MinTime.COUNTER1, now);
            long phaseOrange = prefs.getLong(MinTime.COUNTER2, now);
            if (offset <= phaseGreen) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                        + phaseGreen, alarmIntentOrange);
            }
            if (offset <= phaseGreen + phaseOrange) {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                        + phaseGreen + phaseOrange, alarmIntentRed);
            }
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    elapsedRealtime, NOTIFICATION_INTERVAL_IN_MILLIS,
                    alarmIntentRepeating);
            anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            taskShouldBeRunning = true;
            CountdownTask task = new CountdownTask(this);
            task.execute();
            binding.countdown.setVisibility(View.VISIBLE);
            binding.setup.setVisibility(View.INVISIBLE);
            if (ab != null) ab.hide();
        } else {
            updateViews(prefs.getLong(COUNTER1, 0),
                    prefs.getLong(COUNTER2, 0),
                    prefs.getLong(COUNTER3, 0));
            binding.setup.setVisibility(View.VISIBLE);
            binding.countdown.setVisibility(View.INVISIBLE);
            if (ab != null) ab.show();
        }
    }

    private void stopAnimationAndTask() {
        anim.cancel();
        anim.reset();
        taskShouldBeRunning = false;
    }
}
