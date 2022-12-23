/*
 * MinTimeUtils.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2022
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class MinTimeUtils {

    private static final String TAG = MinTimeUtils.class.getSimpleName();

    static boolean saveJSONObject(Context context, JSONObject data,
                                  String filename) {
        try (FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            out.write(data.toString().getBytes());
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, String.format("Error saving file %s", filename), e);
        }
        return false;
    }

    static JSONObject loadJSONObject(Context context, String filename) {
        JSONObject data = null;
        FileInputStream in = null;
        try {
            File f = new File(context.getFilesDir(), filename);
            if (f.exists()) {
                in = context.openFileInput(filename);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(128);
                int i;
                while ((i = in.read()) != -1) {
                    bytes.write(i);
                }
                String json = bytes.toString();
                data = new JSONObject(json);
            }
        } catch (Throwable tr) { // IOException, JSONException
            Log.e(TAG, "Error while reading data", tr);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close file", e);
                }
            }
        }
        return data;
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

    public static int getBackgroundColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (android.R.attr.colorBackground, value, true);
        return value.data;
    }
}
