/*
 * CountdownApi.java
 *
 * Min Time (c) Thomas Künneth 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.res.Resources;
import android.view.animation.Animation;

import org.json.JSONObject;

public interface CountdownApi {

    long getRemaining();

    BigTime getTimer();

    long getElpased();

    JSONObject getData();

    Animation prepareAnimation();

    boolean taskShouldBeRunning();

    String getString(int resId);

    Resources getResources();

    String getString(int resId, Object... formatArgs);

}
