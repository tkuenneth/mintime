/*
 * CountdownTask.java
 *
 * Min Time (c) Thomas Künneth 2015
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONObject;

class CountdownTask extends AsyncTask<Void, Long, Void> {

    private final CountdownApi i;

    CountdownTask(CountdownApi i) {
        this.i = i;
        onProgressUpdate(i.getRemaining());
    }

    @Override
    protected Void doInBackground(Void... params) {
        JSONObject data = i.getData();
        while (!isCancelled() && (data != null)) {
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
        JSONObject data = i.getData();
        if (data != null) {
            BigTime timer = i.getTimer();
            long remaining = values[0];
            boolean startAnimation = false;
            if (remaining < 0) {
                startAnimation = true;
                remaining = -remaining;
            }
            long elapsed = i.getElpased();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timer.setColor(i.getResources().getColor(color, null));
            } else {
                timer.setColor(i.getResources().getColor(color));
            }
            long secs = remaining / 1000;
            if (secs >= 60) {
                timer.setText(i.getString(R.string.template, secs / 60,
                        i.getString(R.string.min)));
            } else {
                timer.setText(i.getString(R.string.template, secs,
                        i.getString(R.string.sec)));
            }
            if ((startAnimation) && !timer.isRedAlert()) {
                timer.startAnimation(i.prepareAnimation());
                timer.setRedAlert(true);
            }
        }
    }
}
