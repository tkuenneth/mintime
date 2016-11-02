/*
 * MinTime.java
 * 
 * Min Time (c) Thomas Künneth 2014 - 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

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
public class MinTime extends Activity {

    public static final int RQ_ALARM_ORANGE = 1;
    public static final int RQ_ALARM_RED = 2;
    public static final int RQ_ALARM_REPEATING = 3;
    public static final int RQ_NOTIFICATION = 4;

    private static final String TAG = MinTime.class.getSimpleName();
    private static final String FILENAME = TAG + ".dat";
    private static final String FILENAME_DISTRIBUTIONS = TAG + ".dst";
    private static final String DST = "distributions";

    public static final String COUNTER1 = "counter1";
    public static final String COUNTER2 = "counter2";
    public static final String COUNTER3 = "counter3";
    public static final String RESUMED = "resumed";
    public static final String END = "end";

    public static final long ONE_MINUTE = 60000L;

    private Counter counter1, counter2, counter3;
    private SimpleButton total, start;

    private JSONObject data;
    private List<String> distributions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        total = (SimpleButton) findViewById(R.id.total);
        start = (SimpleButton) findViewById(R.id.start_or_resume);
        start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (updateAndSaveData()) {
                    Intent intent = new Intent(MinTime.this,
                            CountdownActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MinTime.this, R.string.error1,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        OnClickListener l = new OnClickListener() {

            @Override
            public void onClick(View v) {
                updateTotal();
            }
        };
        counter1 = (Counter) findViewById(R.id.counter1);
        counter1.setOnClickListener(l);
        counter2 = (Counter) findViewById(R.id.counter2);
        counter2.setOnClickListener(l);
        counter3 = (Counter) findViewById(R.id.counter3);
        counter3.setOnClickListener(l);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateAndSaveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // die zuletzt eingegebenen Daten
        data = loadData(this);
        if (data == null) {
            data = new JSONObject();
            updateData(ONE_MINUTE, ONE_MINUTE, ONE_MINUTE);
            JSONUtils.putLongInJSONObject(data, RESUMED, -1);
        }
        if (JSONUtils.getLongFromJSONObject(data, RESUMED) == -1) {
            start.setText(R.string.start);
        } else {
            start.setText(R.string.resume);
        }
        updateViews(JSONUtils.getLongFromJSONObject(data, COUNTER1),
                JSONUtils.getLongFromJSONObject(data, COUNTER2),
                JSONUtils.getLongFromJSONObject(data, COUNTER3));
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
            item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    updateViews(val1, val2, val3);
                    return true;
                }
            });
        }
        final String distribution = createDistribution();
        if (isSavedDistribution(distribution)) {
            MenuItem delete = menu
                    .add(1, Menu.NONE, Menu.NONE, R.string.delete);
            delete.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    distributions.remove(distribution);
                    saveDistributions();
                    return true;
                }
            });
        } else {
            MenuItem save = menu.add(1, Menu.NONE, Menu.NONE, R.string.save);
            save.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    distributions.add(distribution);
                    saveDistributions();
                    return true;
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public static String millisToPrettyString(Context context, long millis) {
        long secs = millis / 1000;
        long m = secs / 60;
        long s = secs % 60;
        StringBuilder sb = new StringBuilder();
        if (m > 0) {
            sb.append(context.getString(R.string.template, m,
                    context.getString(R.string.min)));
        }
        if ((s > 0) || (m == 0)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(context.getString(R.string.template, s,
                    context.getString(R.string.sec)));
        }
        return sb.toString();
    }

    public static boolean saveData(Context context, JSONObject data) {
        return JSONUtils.saveJSONObject(context, data, FILENAME);
    }

    public static JSONObject loadData(Context context) {
        return JSONUtils.loadJSONObject(context, FILENAME);
    }

    private void updateViews(long val1, long val2, long val3) {
        counter1.setValueInMillis(val1);
        counter2.setValueInMillis(val2);
        counter3.setValueInMillis(val3);
        updateTotal(val1, val2, val3);
    }

    /**
     * Diese Methode schreibt die aktuellen Eingaben der Views in die
     * Datenstruktur.
     */
    private void updateData() {
        updateData(counter1.getValueInMillis(), counter2.getValueInMillis(),
                counter3.getValueInMillis());
    }

    /**
     * Diese Methode schreibt die übergebenen Werte in die Datenstruktur.
     *
     * @param val1 Wert für {@code COUNTER1}
     * @param val2 Wert für {@code COUNTER2}
     * @param val3 Wert für {@code COUNTER3}
     */
    private void updateData(long val1, long val2, long val3) {
        JSONUtils.putLongInJSONObject(data, COUNTER1, val1);
        JSONUtils.putLongInJSONObject(data, COUNTER2, val2);
        JSONUtils.putLongInJSONObject(data, COUNTER3, val3);
    }

    private boolean updateAndSaveData() {
        updateData();
        return saveData(this, data);
    }

    private boolean saveDistributions() {
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        for (String s : distributions) {
            array.put(s);
        }
        try {
            data.put(DST, array);
            return JSONUtils.saveJSONObject(this, data, FILENAME_DISTRIBUTIONS);
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Schreiben der Zeitverteilungen", e);
        }
        return false;
    }

    /**
     * Liest Zeitverteilungen ein. Hierzu wird der Instanzvariable
     * {@code distributions} eine neue Instanz einer {@code List<String>}
     * zugewiesen und mit den Verteilungen gefüllt.
     *
     * @return liefert {@code true} wenn die Verteilungen fehlerfrei eingelesen
     * wurden, sonst {@code false}
     */
    private boolean loadDistributions() {
        distributions = new ArrayList<>();
        JSONObject data = JSONUtils
                .loadJSONObject(this, FILENAME_DISTRIBUTIONS);
        if (data != null) {
            try {
                JSONArray array = data.getJSONArray(DST);
                for (int i = 0; i < array.length(); i++) {
                    distributions.add(array.getString(i));
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "Fehler beim Lesen der Zeitverteilungen", e);
            }
        }
        return false;
    }

    private String createDistribution() {
        long c1 = JSONUtils.getLongFromJSONObject(data, COUNTER1);
        long c2 = JSONUtils.getLongFromJSONObject(data, COUNTER2);
        long c3 = JSONUtils.getLongFromJSONObject(data, COUNTER3);
        return Long.toString(c1) + "|" + Long.toString(c2) + "|"
                + Long.toString(c3);
    }

    private boolean isSavedDistribution(String distribution) {
        for (String d : distributions) {
            if (d.equals(distribution)) {
                return true;
            }
        }
        return false;
    }

    private void updateTotal() {
        long val1 = counter1.getValueInMillis();
        long val2 = counter2.getValueInMillis();
        long val3 = counter3.getValueInMillis();
        updateTotal(val1, val2, val3);
    }

    private void updateTotal(long val1, long val2, long val3) {
        total.setText(millisToPrettyString(this, val1 + val2 + val3));
    }
}
