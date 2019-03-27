/*
 * Counter.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;

/**
 * Komponente, die einen Zähler repräsentiert. Der Wert kann mit Plus/Minus
 * eingestellt werden.
 *
 * @author Thomas
 */
public class Counter extends View {

    private static final int INTERVAL_LONG = 500;
    private static final int SWIPE_MAX_MINUTES = 90;
    private static final int INTERVAL_SHORT = 100;
    private static final int DELAY = 200;

    private boolean useMinutes = true;
    private boolean increase;
    private long value = 0;
    private int times = 0;
    private Timer timer = null;
    private TimerTask task = null;
    private float lastX;

    private final Paint paint;
    private final int color;

    public Counter(Context context) {
        this(context, null, 0);
    }

    public Counter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Counter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int _color = Color.WHITE;
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.Counter);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.Counter_mintime_color:
                    _color = a.getColor(i, _color);
                    break;
                default:
                    throw new RuntimeException("Unknown attribute for "
                            + getClass().toString() + ": " + attr);
            }
        }
        a.recycle();
        color = _color;

        // Paint für die Textausgabe
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);

        setOnTouchListener((v, event) -> {
            int x = (int) event.getX();
            int segmentWidth = v.getWidth() / 6;
            int segment = x / segmentWidth;
            lastX = event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ((segment < 2) || (segment > 3)) {
                        increase = (segment > 3);
                        timer = new Timer();
                        task = new TimerTask() {

                            @Override
                            public void run() {
                                int increment = 1;
                                if (times < 5) {
                                    times += 1;
                                } else {
                                    if ((value % 5) == 0) {
                                        if (increase || (value >= 10)) {
                                            increment = 5;
                                        }
                                    }
                                }
                                if (increase) {
                                    value += increment;
                                } else {
                                    value -= increment;
                                }
                                if (value < 1) {
                                    if (useMinutes) {
                                        useMinutes = false;
                                        value = 59;
                                    } else {
                                        value = 0;
                                    }
                                } else if (value > 59) {
                                    if (!useMinutes) {
                                        useMinutes = true;
                                        value = 1;
                                        times = 0;
                                    }
                                }
                                postInvalidate();
                            }

                        };
                        times = 0;
                        timer.schedule(task, 0, INTERVAL_LONG);
                    } else {
                        lastX = x;
                        timer = new Timer();
                        useMinutes = true;
                        task = new TimerTask() {

                            @Override
                            public void run() {
                                int segmentSize = v.getWidth() / SWIPE_MAX_MINUTES;
                                value = (long) lastX / segmentSize;
                                if (value < 1) {
                                    value = 1;
                                } else if (value > SWIPE_MAX_MINUTES) {
                                    value = SWIPE_MAX_MINUTES;
                                }
                                postInvalidate();
                            }

                        };
                        timer.schedule(task, DELAY, INTERVAL_SHORT);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    cancelTimer();
                    postInvalidate();
                    performClick();
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Point outSize = CanvasUtils.getScreenSize(getContext());
        paint.setTextSize(outSize.y / 12f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float widthSmall = width * .15f;
        float widthLarge = width * .7f;
        String info = MinTime.millisToPrettyString(getContext(), getValueInMillis());
        paint.setColor(color);
        CanvasUtils.drawText(canvas, widthSmall + widthLarge / 2, height / 2,
                info, paint);
        CanvasUtils.drawText(canvas, widthSmall / 2, height / 2, getContext()
                .getString(R.string.minus), paint);
        CanvasUtils.drawText(canvas, widthSmall + widthLarge + widthSmall / 2,
                height / 2, getContext().getString(R.string.plus), paint);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            cancelTimer();
        }
    }

    public long getValueInMillis() {
        return useMinutes ? value * MinTime.ONE_MINUTE : value * 1000;
    }

    public void setValueInMillis(long value) {
        value /= 1000;
        if (value >= 60) {
            this.value = value / 60;
            useMinutes = true;
        } else {
            this.value = value;
            useMinutes = false;
        }
        postInvalidate();
    }

    private void cancelTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
