/*
 * MinTime.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    public static final int RQ_ALARM_ORANGE = 1;
    public static final int RQ_ALARM_RED = 2;
    public static final int RQ_ALARM_REPEATING = 3;
    public static final int RQ_NOTIFICATION = 4;

    private static final String TAG = MinTime.class.getSimpleName();
    private static final String FILENAME_DISTRIBUTIONS = TAG + ".dst";
    private static final String DST = "distributions";

    public static final String COUNTER1 = "counter1";
    public static final String COUNTER2 = "counter2";
    public static final String COUNTER3 = "counter3";
    public static final String RESUMED = "resumed";
    public static final String END = "end";

    public static final long ONE_MINUTE = 60000L;

    private MainBinding binding;

    private List<String> distributions;

    private SharedPreferences prefs;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        binding = MainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.startOrResume.setOnClickListener(v -> {
            updateData();
            Intent intent = new Intent(MinTime.this,
                    CountdownActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            startActivity(intent);
        });

        binding.counter1.setValueUpdator(this);
        binding.counter2.setValueUpdator(this);
        binding.counter3.setValueUpdator(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // die zuletzt eingegebenen Daten
        if (!prefs.contains(COUNTER1) || !prefs.contains(COUNTER2) || !prefs.contains(COUNTER3)) {
            updateData(ONE_MINUTE, ONE_MINUTE, ONE_MINUTE);
            prefs.edit().putLong(RESUMED, -1).apply();
        }
        boolean isResumed = prefs.getLong(RESUMED, -1) != -1;
        if (isResumed) {
            binding.startOrResume.setText(R.string.resume);
        } else {
            binding.startOrResume.setText(R.string.start);
        }
        updateViews(prefs.getLong(COUNTER1, 0),
                prefs.getLong(COUNTER2, 0),
                prefs.getLong(COUNTER3, 0));
        // die gespeicherten Verteilungen
        loadDistributions();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateData();
        menu.removeGroup(1);
        for (String d : distributions) {
            String[] vals = d.split("\\|");
            final long val1 = Long.parseLong(vals[0]);
            final long val2 = Long.parseLong(vals[1]);
            final long val3 = Long.parseLong(vals[2]);
            String dd = getString(R.string.template_two_dashes,
                    millisToPrettyString(this, val1),
                    millisToPrettyString(this, val2),
                    millisToPrettyString(this, val3));
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
        return super.onPrepareOptionsMenu(menu);
    }

    public static String millisToPrettyString(Context context, long millis) {
        long secs = millis / 1000;
        long m = secs / 60;
        long s = secs % 60;
        StringBuilder sb = new StringBuilder();
        if ((m > 0) || (s == 0)) {
            sb.append(context.getString(R.string.template, m,
                    context.getString(R.string.min)));
        }
        if (s > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(context.getString(R.string.template, s,
                    context.getString(R.string.sec)));
        }
        return sb.toString();
    }

    private void updateViews(long val1, long val2, long val3) {
        binding.counter1.setValueInMillis(val1);
        binding.counter2.setValueInMillis(val2);
        binding.counter3.setValueInMillis(val3);
        updateTotal(val1, val2, val3);
    }

    /**
     * Diese Methode schreibt die aktuellen Eingaben der Views in die
     * Datenstruktur.
     */
    private void updateData() {
        updateData(binding.counter1.getValueInMillis(),
                binding.counter2.getValueInMillis(),
                binding.counter3.getValueInMillis());
    }

    /**
     * Diese Methode schreibt die übergebenen Werte in die Datenstruktur.
     *
     * @param val1 Wert für {@code COUNTER1}
     * @param val2 Wert für {@code COUNTER2}
     * @param val3 Wert für {@code COUNTER3}
     */
    private void updateData(long val1, long val2, long val3) {
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
            JSONUtils.saveJSONObject(this, data, FILENAME_DISTRIBUTIONS);
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Schreiben der Zeitverteilungen", e);
        }
    }

    /**
     * Liest Zeitverteilungen ein. Hierzu wird der Instanzvariable
     * {@code distributions} eine neue Instanz einer {@code List<String>}
     * zugewiesen und mit den Verteilungen gefüllt.
     */
    private void loadDistributions() {
        distributions = new ArrayList<>();
        JSONObject data = JSONUtils
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

    @Override
    public void updateValue() {
        long val1 = binding.counter1.getValueInMillis();
        long val2 = binding.counter2.getValueInMillis();
        long val3 = binding.counter3.getValueInMillis();
        updateTotal(val1, val2, val3);
        invalidateOptionsMenu();
    }

    private void updateTotal(long val1, long val2, long val3) {
        binding.total.setText(millisToPrettyString(this, val1 + val2 + val3));
        binding.startOrResume.setEnabled(val1 + val2 + val3 > 0);
    }
}
