/*
 * MinTime.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;
import androidx.window.core.layout.WindowHeightSizeClass;
import androidx.window.core.layout.WindowSizeClass;
import androidx.window.core.layout.WindowWidthSizeClass;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;
import androidx.window.layout.WindowMetricsCalculator;

import com.thomaskuenneth.mintime.databinding.MainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    private WindowInfoTrackerCallbackAdapter adapter;

    private boolean hideDescriptions;

    private final Consumer<WindowLayoutInfo> callback = (windowLayoutInfo -> {
        final var windowMetrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(this);
        final var windowWidth = windowMetrics.getBounds().width();
        final var windowHeight = windowMetrics.getBounds().height();
        final var mainUiLayoutParams = binding.mainUi.getLayoutParams();
        final var infoPanelLayoutParams = binding.infoPanel.getRoot().getLayoutParams();
        final var hingeLayoutParams = binding.hinge.getLayoutParams();
        float screenPixelDensity = getResources().getDisplayMetrics().density;
        WindowSizeClass windowSizeClass = WindowSizeClass.compute(windowWidth / screenPixelDensity, windowHeight / screenPixelDensity);
        boolean hasFoldingFeature = false;
        hideDescriptions = true;
        List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
        for (DisplayFeature displayFeature : displayFeatures) {
            FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
            if (foldingFeature != null) {
                hasFoldingFeature = windowSizeClass.getWindowWidthSizeClass() != WindowWidthSizeClass.COMPACT &&
                        windowSizeClass.getWindowHeightSizeClass() != WindowHeightSizeClass.COMPACT;
                final var foldingFeatureBounds = foldingFeature.getBounds();
                hingeLayoutParams.width = foldingFeatureBounds.width();
                hingeLayoutParams.height = foldingFeatureBounds.height();
                if (foldingFeature.getOrientation() == FoldingFeature.Orientation.VERTICAL) {
                    binding.parent.setOrientation(LinearLayout.HORIZONTAL);
                    mainUiLayoutParams.width = foldingFeatureBounds.left;
                    mainUiLayoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    infoPanelLayoutParams.width = windowWidth - foldingFeatureBounds.right;
                    infoPanelLayoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                } else {
                    int[] intArray = new int[2];
                    binding.mainUi.getLocationOnScreen(intArray);
                    binding.parent.setOrientation(LinearLayout.VERTICAL);
                    mainUiLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    mainUiLayoutParams.height = foldingFeatureBounds.top - intArray[1];
                    infoPanelLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    infoPanelLayoutParams.height = windowHeight - foldingFeatureBounds.bottom;
                }
            }
        }
        if (!hasFoldingFeature) {
            final float density = getResources().getDisplayMetrics().density;
            final float dp = windowMetrics.getBounds().width() / density;
            binding.parent.setOrientation(LinearLayout.HORIZONTAL);
            hingeLayoutParams.width = 0;
            hingeLayoutParams.height = 0;
            if (dp >= 600) {
                mainUiLayoutParams.width = windowWidth / 2;
                mainUiLayoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                infoPanelLayoutParams.width = windowWidth / 2 - 32;
                infoPanelLayoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            } else {
                mainUiLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                mainUiLayoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                infoPanelLayoutParams.width = 0;
                infoPanelLayoutParams.height = 0;
                hideDescriptions = false;
            }
        }
        updateUI();
    });

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new WindowInfoTrackerCallbackAdapter(
                WindowInfoTracker.Companion.getOrCreate(
                        this
                )
        );
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        loadDistributions();
        alarmMgr = getSystemService(AlarmManager.class);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // setup
        binding.start.setOnClickListener(v -> {
            prefs.edit().putLong(RESUMED, System.currentTimeMillis()).apply();
            configureAlarms();
            updateUI();
        });
        binding.counter1.setValueUpdator(this);
        binding.counter2.setValueUpdator(this);
        binding.counter3.setValueUpdator(this);
        // countdown
        binding.tapHere.setOnClickListener(v -> {
            prefs.edit().putLong(RESUMED, -1).apply();
            cancelAlarms();
            NotificationManager m = getSystemService(NotificationManager.class);
            if (m != null) {
                m.cancel(NOTIFICATION_ID);
            }
            stopAnimationAndTask();
            updateUI();
        });
        anim = null;
        RepeatingAlarm.initNotificationChannels(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAnimationAndTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.addWindowLayoutInfoListener(this,
                ContextCompat.getMainExecutor(this),
                callback);
        updateUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.removeWindowLayoutInfoListener(callback);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeGroup(1);
        if (!isResumed()) {
            for (String d : distributions) {
                String[] values = d.split("\\|");
                final long val1 = Long.parseLong(values[0]);
                final long val2 = Long.parseLong(values[1]);
                final long val3 = Long.parseLong(values[2]);
                String dd = getString(R.string.template_two_dashes,
                        MinTimeUtils.millisToPrettyString(this, val1),
                        MinTimeUtils.millisToPrettyString(this, val2),
                        MinTimeUtils.millisToPrettyString(this, val3));
                MenuItem item = menu.add(1, Menu.NONE, Menu.NONE, dd);
                item.setOnMenuItemClickListener(item1 -> {
                    updateViews(val1, val2, val3);
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
            MenuItem notificationSettingsMenuItem = menu.add(1, Menu.NONE, Menu.NONE, R.string.notification_settings);
            notificationSettingsMenuItem.setOnMenuItemClickListener(menuItem -> {
                launchNotificationSettings();
                return true;
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MenuItem alarmsAndRemindersMenuItem = menu.add(1, Menu.NONE, Menu.NONE, R.string.alarms_and_reminders);
                alarmsAndRemindersMenuItem.setOnMenuItemClickListener(menuItem -> {
                    launchAlarmsAndRemindersSettings();
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

    public long getElapsed() {
        return System.currentTimeMillis()
                - prefs.getLong(RESUMED, System.currentTimeMillis());
    }

    public long getRemaining() {
        long end = getEnd();
        return end - System.currentTimeMillis();
    }

    public BigTime getTimer() {
        return binding.timer;
    }

    public TextView getTapHere() {
        return binding.tapHere;
    }

    public Animation prepareAnimation() {
        return anim;
    }

    public boolean taskShouldBeRunning() {
        return taskShouldBeRunning;
    }

    private void updateViews(long val1, long val2, long val3) {
        binding.counter1.setValueInMillis(val1);
        binding.counter2.setValueInMillis(val2);
        binding.counter3.setValueInMillis(val3);
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
            if (!MinTimeUtils.saveJSONObject(this, data, FILENAME_DISTRIBUTIONS)) {
                Log.w(TAG, "Saving file was not successful");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error while writing the file", e);
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
                Log.e(TAG, "Error while reading data", e);
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
        return prefs.getLong(COUNTER1, now)
                + prefs.getLong(COUNTER2, now)
                + prefs.getLong(COUNTER3, now);
    }

    private long getEnd() {
        long resumed = prefs.getLong(RESUMED, System.currentTimeMillis());
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
        return prefs.getLong(RESUMED, -1) != -1;
    }

    private void updateUI() {
        binding.info1.setVisibility(hideDescriptions ? View.INVISIBLE : View.VISIBLE);
        binding.info2.setVisibility(hideDescriptions ? View.INVISIBLE : View.VISIBLE);
        binding.info3.setVisibility(hideDescriptions ? View.INVISIBLE : View.VISIBLE);
        binding.info4.setVisibility(hideDescriptions ? View.INVISIBLE : View.VISIBLE);
        updateViews(prefs.getLong(COUNTER1, 0),
                prefs.getLong(COUNTER2, 0),
                prefs.getLong(COUNTER3, 0));
        ActionBar ab = getSupportActionBar();
        if (isResumed()) {
            long now = System.currentTimeMillis();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long offset = now
                    - prefs.getLong(RESUMED, now);
            elapsedRealtime -= offset;
            long phaseGreen = prefs.getLong(COUNTER1, now);
            long phaseOrange = prefs.getLong(COUNTER2, now);
            if (canScheduleExactAlarms()) {
                if (offset <= phaseGreen) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                            + phaseGreen, alarmIntentOrange);
                }
                if (offset <= phaseGreen + phaseOrange) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime
                            + phaseGreen + phaseOrange, alarmIntentRed);
                }
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
            binding.parent.setVisibility(View.INVISIBLE);
            if (ab != null) ab.hide();
        } else {
            binding.parent.setVisibility(View.VISIBLE);
            binding.countdown.setVisibility(View.INVISIBLE);
            if (ab != null) ab.show();
            if (RepeatingAlarm.shouldCheckNotificationSettings(getSystemService(NotificationManager.class))) {
                linkToSettings(
                        binding.info,
                        R.string.check_notification_settings,
                        R.string.notification_settings,
                        this::launchNotificationSettings);
                binding.info.setVisibility(View.VISIBLE);
            } else {
                binding.info.setVisibility(View.GONE);
            }
            if (canScheduleExactAlarms()) {
                binding.infoScheduleExactAlarms.setVisibility(View.GONE);
            } else {
                linkToSettings(
                        binding.infoScheduleExactAlarms,
                        R.string.exact_alarms_are_off,
                        R.string.alarms_and_reminders,
                        this::launchAlarmsAndRemindersSettings);
                binding.infoScheduleExactAlarms.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean canScheduleExactAlarms() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmMgr.canScheduleExactAlarms();
    }

    private void stopAnimationAndTask() {
        binding.timer.clearAnimation();
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
        taskShouldBeRunning = false;
    }

    private void linkToSettings(TextView textView, @StringRes int messageResId, @StringRes int linkResId, Runnable code) {
        String message = getString(messageResId);
        String clickableString = getString(linkResId);
        Spannable spannable = new SpannableString(message);
        int pos = message.toLowerCase().indexOf(clickableString.toLowerCase());
        if (pos >= 0) {
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    code.run();
                }
            }, pos, pos + clickableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        textView.setText(spannable);
    }

    private void launchNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void launchAlarmsAndRemindersSettings() {
        startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
    }
}
