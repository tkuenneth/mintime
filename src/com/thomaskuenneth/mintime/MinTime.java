/*
 * MinTime.java
 * 
 * TKWeek (c) Thomas Künneth 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Dies ist die Hauptactivity der App.
 * 
 * @author Thomas
 * 
 */
public class MinTime extends Activity {

	private static final String TAG = MinTime.class.getSimpleName();
	private static final String FILENAME = TAG + ".dat";

	public static final String COUNTER1 = "counter1";
	public static final String COUNTER2 = "counter2";
	public static final String COUNTER3 = "counter3";
	public static final String RESUMED = "resumed";

	private Counter counter1, counter2, counter3;
	private SimpleButton start;
	
	private JSONObject data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		counter1 = (Counter) findViewById(R.id.counter1);
		counter2 = (Counter) findViewById(R.id.counter2);
		counter3 = (Counter) findViewById(R.id.counter3);

		start = (SimpleButton) findViewById(R.id.start_or_resume);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MinTime.this,
						CountdownActivity.class);
				if (save()) {
					startActivity(intent);
				} else {
					// TODO: Meldung ausgeben?
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		save();
	}

	@Override
	protected void onResume() {
		super.onResume();
		data = loadJSONObject(this);
		if (data == null) {
			data = new JSONObject();
			putLongInJSONObject(data, COUNTER1, 0);
			putLongInJSONObject(data, COUNTER2, 0);
			putLongInJSONObject(data, COUNTER3, 0);
			putLongInJSONObject(data, RESUMED, -1);
		}
		if (getLongFromJSONObject(data, RESUMED) == -1) {
			start.setText(R.string.start);
		} else {
			start.setText(R.string.resume);
		}
		counter1.setValueInMillis(getLongFromJSONObject(data, COUNTER1));
		counter2.setValueInMillis(getLongFromJSONObject(data, COUNTER2));
		counter3.setValueInMillis(getLongFromJSONObject(data, COUNTER3));
	}
	
	public static boolean saveJSONObject(Context context, JSONObject data) {
		boolean result = false;
		FileOutputStream out = null;
		try {
			out = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			out.write(data.toString().getBytes());
			out.flush();
			result = true;
		} catch (IOException e) {
			Log.e(TAG, "Fehler beim Schreiben der Daten", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Log.e(TAG, "Fehler beim Schließen der Datei", e);
				}
			}
		}
		return result;
	}

	public static JSONObject loadJSONObject(Context context) {
		JSONObject data = null;
		FileInputStream in = null;
		try {
			in = context.openFileInput(FILENAME);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream(128);
			int i;
			while ((i = in.read()) != -1) {
				bytes.write(i);
			}
			String json = new String(bytes.toByteArray());
			data = new JSONObject(json);
		} catch (Throwable tr) { // IOException, JSONException
			Log.e(TAG, "Fehler beim Einlesen der Daten", tr);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, "Fehler beim Schließen der Datei", e);
				}
			}
		}
		return data;
	}

	public static long getLongFromJSONObject(JSONObject data, String name) {
		long result = -1;
		try {
			result = data.getLong(name);
		} catch (JSONException e) {
			Log.e(TAG, "Fehler beim Lesen von " + name, e);
		}
		return result;
	}

	public static void putLongInJSONObject(JSONObject data, String name,
			long value) {
		try {
			data.put(name, value);
		} catch (JSONException e) {
			Log.e(TAG, "Fehler beim Schreiben von " + name, e);
		}
	}

	private boolean save() {
		putLongInJSONObject(data, COUNTER1, counter1.getValueInMillis());
		putLongInJSONObject(data, COUNTER2, counter2.getValueInMillis());
		putLongInJSONObject(data, COUNTER3, counter3.getValueInMillis());
		return saveJSONObject(this, data);
	}

}
