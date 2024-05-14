/*
 * MIT License
 *
 * Copyright (c) 2014 - 2024 Thomas Künneth
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

import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

class CountdownTask extends AsyncTask<Void, Long, Void> {

    private final MinTime i;

    private final SharedPreferences prefs;

    CountdownTask(MinTime i) {
        this.i = i;
        prefs = PreferenceManager.getDefaultSharedPreferences(i);
        onProgressUpdate(i.getRemaining());
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (!isCancelled()) {
            try {
                long remaining = i.getRemaining();
                publishProgress(remaining);
                if (remaining < 0) {
                    remaining = -remaining;
                }
                Thread.sleep(remaining >= 150000 ? 60000 : 1000);
            } catch (InterruptedException ignored) {
            }
            if (!i.taskShouldBeRunning()) {
                cancel(true);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        BigTime timer = i.getTimer();
        long remaining = values[0];
        boolean timeIsUp = remaining < 1;
        long elapsed = i.getElapsed();
        long now = System.currentTimeMillis();
        int color;
        if (elapsed <= prefs.getLong(MinTime.COUNTER1, now)) {
            color = R.color.green;
        } else if (elapsed <= (prefs.getLong(MinTime.COUNTER1, now) + prefs
                .getLong(MinTime.COUNTER2, now))) {
            color = R.color.orange;
        } else {
            color = R.color.red;
        }
        int intColor = i.getResources().getColor(color, null);
        timer.setColor(intColor);
        if (timeIsUp) {
            timer.setText(i.getString(R.string.time_is_up));
        } else {
            long secs = remaining / 1000;
            if (secs >= 60) {
                long rounded = secs >= 100 ? (secs + 59) : secs;
                timer.setText(i.getString(R.string.template, rounded / 60,
                        i.getString(R.string.min)));
            } else {
                timer.setText(i.getString(R.string.template, secs,
                        i.getString(R.string.sec)));
            }
        }
    }
}
