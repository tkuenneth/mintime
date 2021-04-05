/*
 * CountdownTask.java
 *
 * Min Time (c) Thomas Künneth 2015 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.animation.Animation;

import androidx.preference.PreferenceManager;

class CountdownTask extends AsyncTask<Void, Long, Void> {

    private final MinTime i;

    private final SharedPreferences prefs;

    CountdownTask(MinTime i) {
        this.i = i;
        prefs = PreferenceManager.getDefaultSharedPreferences(i);
        i.getTimer().setRedAlert(false);
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
            } catch (InterruptedException e) {
                // keine Log-Ausgabe nötig - der Thread darf ja
                // jederzeit unterbrochen werden
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
        boolean startAnimation = false;
        if (remaining < 0) {
            startAnimation = true;
            remaining = -remaining;
        }
        long elapsed = i.getElpased();
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
        i.getTapHere().setTextColor(intColor);
        timer.setColor(intColor);
        long secs = remaining / 1000;
        if (secs >= 60) {
            long rounded = secs >= 100 ? (secs + 59) : secs;
            timer.setText(i.getString(R.string.template, rounded / 60,
                    i.getString(R.string.min)));
        } else {
            timer.setText(i.getString(R.string.template, secs,
                    i.getString(R.string.sec)));
        }
        if ((startAnimation) && !timer.isRedAlert()) {
            timer.setRedAlert(true);
            Animation anim = i.prepareAnimation();
            if (anim != null) {
                timer.startAnimation(anim);
            }
        }
    }
}
