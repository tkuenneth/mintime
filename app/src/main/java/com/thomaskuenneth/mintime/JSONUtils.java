/*
 * JSONUtils.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Diese Klasse enthält statische Methoden zum Bearbeiten, Laden und Speichern
 * von JSON-Objekten.
 *
 * @author Thomas
 */
class JSONUtils {

    private static final String TAG = JSONUtils.class.getSimpleName();

    static long getLongFromJSONObject(JSONObject data, String name) {
        try {
            return data.getLong(name);
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Lesen von " + name, e);
        }
        return -1;
    }

    static void putLongInJSONObject(JSONObject data, String name,
                                    long value) {
        try {
            data.put(name, value);
        } catch (JSONException e) {
            Log.e(TAG, "Fehler beim Schreiben von " + name, e);
        }
    }

    static boolean saveJSONObject(Context context, JSONObject data,
                                  String filename) {
        try (FileOutputStream out = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            out.write(data.toString().getBytes());
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Schreiben der Daten", e);
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
}
