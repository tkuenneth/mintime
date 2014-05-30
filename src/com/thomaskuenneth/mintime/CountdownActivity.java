/*
 * CountdownActivity.java
 * 
 * TKWeek (c) Thomas Künneth 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Diese Activity realisiert die Zeitanzeige/Countdown.
 * 
 * @author Thomas
 * 
 */
public class CountdownActivity extends Activity {

	private JSONObject data;
	private BigTime timer;
	private AsyncTask<Void, Long, Void> task;
	private Animation anim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.countdown);

		timer = (BigTime) findViewById(R.id.timer);
		timer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MinTime.putLongInJSONObject(data, MinTime.RESUMED, -1);
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
		MinTime.saveJSONObject(this, data);
		data = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		data = MinTime.loadJSONObject(this);
		if (data == null) {
			throw new IllegalStateException("data == null");
		}

		if (MinTime.getLongFromJSONObject(data, MinTime.RESUMED) == -1) {
			MinTime.putLongInJSONObject(data, MinTime.RESUMED,
					System.currentTimeMillis());
		}

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
						// keine Log-Ausgabe nötig - der Threas darf ja
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
					if (elapsed <= MinTime.getLongFromJSONObject(data,
							MinTime.COUNTER1)) {
						color = R.color.green;
					} else if (elapsed <= (MinTime.getLongFromJSONObject(data,
							MinTime.COUNTER1) + MinTime.getLongFromJSONObject(
							data, MinTime.COUNTER2))) {
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

	private long getEnd() {
		long resumed = MinTime.getLongFromJSONObject(data, MinTime.RESUMED);
		long total = MinTime.getLongFromJSONObject(data, MinTime.COUNTER1)
				+ MinTime.getLongFromJSONObject(data, MinTime.COUNTER2)
				+ MinTime.getLongFromJSONObject(data, MinTime.COUNTER3);
		return resumed + total;
	}

	private long getElpased() {
		return System.currentTimeMillis()
				- MinTime.getLongFromJSONObject(data, MinTime.RESUMED);
	}

	private long getRemaining() {
		long end = getEnd();
		return end - System.currentTimeMillis();
	}
}
