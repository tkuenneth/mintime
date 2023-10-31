/*
 * BigTime.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.mintime;

import static com.thomaskuenneth.mintime.MinTimeUtils.getBackgroundColor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class BigTime extends View {

    private static final String TAG = BigTime.class.getSimpleName();

    private final Paint paint1;
    private final int backgroundColor;

    private int color;
    private String text;
    private boolean redAlert;

    public BigTime(Context context) {
        this(context, null, 0);
    }

    public BigTime(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @noinspection resource close() just calls recycle() but isn't available on all supported API levels
     */
    public BigTime(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        backgroundColor = getBackgroundColor(context);
        text = "";
        TextView tv = new TextView(context);
        color = tv.getCurrentTextColor();
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.Counter, 0, 0);
        try {
            color = a.getColor(R.styleable.Counter_mintime_color, color);
        } catch (Exception e) {
            Log.e(TAG, "getColor()", e);
        }
        redAlert = false;
        a.recycle();
        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setTypeface(Typeface.DEFAULT);
        paint1.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        String text = MinTimeUtils.millisToPrettyString(getContext(),
                MinTime.ONE_MINUTE * 999);
        CanvasUtils.calcTextHeight(paint1, w, text);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (redAlert) {
            setBackgroundColor(color);
            paint1.setColor(backgroundColor);
        } else {
            setBackgroundColor(backgroundColor);
            paint1.setColor(color);
        }
        CanvasUtils.drawText(canvas, width / 2, height / 2, text, paint1);
    }

    public void setColor(int color) {
        this.color = color;
        postInvalidate();
    }

    public void setText(String text) {
        this.text = text;
        postInvalidate();
    }

    public void setRedAlert(boolean redAlert) {
        this.redAlert = redAlert;
        postInvalidate();
    }

    public boolean isRedAlert() {
        return redAlert;
    }
}
