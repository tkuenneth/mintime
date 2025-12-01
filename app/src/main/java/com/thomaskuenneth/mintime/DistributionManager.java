/*
 * MIT License
 *
 * Copyright (c) 2025 Thomas Künneth
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DistributionManager {

    private static final String TAG = DistributionManager.class.getSimpleName();
    private static final String FILENAME_DISTRIBUTIONS = "MinTime.dst";
    private static final String DST = "distributions";

    private final Context context;
    private final SharedPreferences prefs;
    private List<String> distributions;

    public DistributionManager(Context context, SharedPreferences prefs) {
        this.context = context.getApplicationContext();
        this.prefs = prefs;
        loadDistributions();
    }

    public List<String> getDistributions() {
        return distributions;
    }

    public void saveDistributions() {
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        for (String s : distributions) {
            array.put(s);
        }
        try {
            data.put(DST, array);
            if (!MinTimeUtils.saveJSONObject(context, data, FILENAME_DISTRIBUTIONS)) {
                Log.w(TAG, "Saving file was not successful");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error while writing the file", e);
        }
    }

    private void loadDistributions() {
        distributions = new ArrayList<>();
        JSONObject data = MinTimeUtils.loadJSONObject(context, FILENAME_DISTRIBUTIONS);
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

    public String createDistribution() {
        long now = System.currentTimeMillis();
        long c1 = prefs.getLong(MinTime.COUNTER1, now);
        long c2 = prefs.getLong(MinTime.COUNTER2, now);
        long c3 = prefs.getLong(MinTime.COUNTER3, now);
        return c1 + "|" + c2 + "|" + c3;
    }

    public boolean isSavedDistribution(String distribution) {
        for (String d : distributions) {
            if (d.equals(distribution)) {
                return true;
            }
        }
        return false;
    }
}
