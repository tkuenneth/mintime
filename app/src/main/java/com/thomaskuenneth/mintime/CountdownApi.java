/*
 * CountdownApi.java
 *
 * Min Time (c) Thomas Künneth 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.res.Resources;
import android.view.animation.Animation;

public interface CountdownApi {

    long getRemaining();

    BigTime getTimer();

    long getElpased();

    Animation prepareAnimation();

    boolean taskShouldBeRunning();

    String getString(int resId);

    Resources getResources();

    String getString(int resId, Object... formatArgs);

    public Context getBaseContext();

}
