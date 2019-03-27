/*
 * BigTime.java
 *
 * Min Time (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Diese Klasse realisiert eine Zeitanzeige mit großer Schrift.
 *
 * @author Thomas
 */
public class BigTime extends View {

    private static final String TAG = BigTime.class.getSimpleName();

    private final Paint paint1;
    private int color;
    private String text = "";
    private boolean redAlert;

    public BigTime(Context context) {
        this(context, null, 0);
    }

    public BigTime(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigTime(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        color = Color.WHITE;
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.Counter, 0, 0);
        try {
            color = a.getColor(R.styleable.Counter_mintime_color, color);
        } catch (Exception e) {
            Log.e(TAG, "getColor()", e);
        }
        redAlert = false;
        a.recycle();
        // Paint für die Zeitanzeige
        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setTypeface(Typeface.DEFAULT);
        paint1.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        String text = MinTime.millisToPrettyString(getContext(),
                MinTime.ONE_MINUTE * 999);
        CanvasUtils.calcTextHeight(paint1, w, text);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (redAlert) {
            setBackgroundColor(color);
            paint1.setColor(Color.WHITE);
        } else {
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
}
