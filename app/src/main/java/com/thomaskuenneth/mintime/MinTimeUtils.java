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

import android.content.Context;
import android.util.Log;

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
}
