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

    private final Paint textPaint;

    private int color;
    private String text;

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
        a.recycle();
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        CanvasUtils.drawText(canvas, width / 2, height / 2, text, textPaint);
    }

    public void setColor(int color) {
        this.color = color;
        textPaint.setColor(color);
        postInvalidate();
    }

    public void setText(String text) {
        this.text = text;
        CanvasUtils.calcTextHeight(textPaint, getWidth(), getHeight(), text);
        postInvalidate();
    }
}
