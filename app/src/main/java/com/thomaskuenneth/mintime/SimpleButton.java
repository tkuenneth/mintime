/*
 * SimpleButton.java
 * 
 * Min Time (c) Thomas Künneth 2014 - 2015
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
import android.view.View;

/**
 * Diese Klasse repräsentiert einen einfachen Button ohne Effekte.
 *
 * @author Thomas
 */
public class SimpleButton extends View {

    private final Paint paint;
    private String text = "";

    public SimpleButton(Context context) {
        this(context, null, 0);
    }

    public SimpleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int color = Color.BLACK;
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.SimpleButton);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SimpleButton_text:
                    text = a.getString(i);
                    setText(text);
                    break;
                default:
                    throw new RuntimeException("Unknown attribute for "
                            + getClass().toString() + ": " + attr);
            }
        }
        a.recycle();
        // Paint für die Textausgabe
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        CanvasUtils.drawText(canvas, width / 2, height / 2, text, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Point outSize = CanvasUtils.getScreenSize(getContext());
        paint.setTextSize(outSize.y / 12);
    }

    public void setColor(int color) {
        paint.setColor(color);
        postInvalidate();
    }

    public void setText(String text) {
        this.text = text;
        postInvalidate();
    }

    public void setText(int resId) {
        setText(getContext().getString(resId));
    }
}
