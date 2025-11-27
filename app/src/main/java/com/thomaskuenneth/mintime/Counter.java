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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.slider.Slider;

public class Counter extends FrameLayout {

    private final TextView text;
    private final Slider seekbar;

    private boolean useMinutes;
    private long value;
    private ValueUpdater cb;

    public Counter(Context context) {
        this(context, null, 0);
    }

    public Counter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @noinspection resource close() just calls recycle() but isn't available on all supported API levels
     */
    public Counter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int color = Color.WHITE;
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.Counter);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.Counter_mintime_color) {
                color = a.getColor(i, color);
            } else {
                throw new RuntimeException("Unknown attribute for "
                        + getClass() + ": " + attr);
            }
        }
        a.recycle();
        View counter = inflate(context, R.layout.counter, this);
        MaterialButton minus = counter.findViewById(R.id.minus);
        minus.setOnClickListener(view -> changeValue(false));
        MaterialButton plus = counter.findViewById(R.id.plus);
        plus.setOnClickListener(view -> changeValue(true));
        text = counter.findViewById(R.id.text);
        text.setTextColor(color);
        seekbar = findViewById(R.id.seekbar);

        ColorStateList csl = ColorStateList.valueOf(color);
        minus.setBackgroundTintList(csl);
        plus.setBackgroundTintList(csl);

        int iconColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE);
        minus.setIconTint(ColorStateList.valueOf(iconColor));
        plus.setIconTint(ColorStateList.valueOf(iconColor));

        seekbar.setThumbTintList(csl);
        seekbar.setTrackActiveTintList(csl);
        seekbar.setTrackInactiveTintList(csl);
        seekbar.setLabelFormatter(value -> context.getString(R.string.template, (int) value, context.getString(R.string.min)));
        seekbar.addOnChangeListener((slider, v, fromUser) -> {
            if (fromUser) {
                value = (long) v;
                useMinutes = true;
            }
            updateUIAndNotifyListener(false);
        });
    }

    public long getValueInMillis() {
        return useMinutes ? value * MinTime.ONE_MINUTE : value * 1000;
    }

    public void setValueInMillis(long value) {
        value /= 1000;
        if ((value >= 60) || (value == 0)) {
            useMinutes = true;
            long minutes = value / 60;
            // Round to the nearest multiple of 10
            this.value = Math.round(minutes / 10.0) * 10;
        } else {
            this.value = value;
            useMinutes = false;
        }
        updateUIAndNotifyListener(true);
    }

    public void setValueUpdater(ValueUpdater updater) {
        cb = updater;
    }

    private void changeValue(boolean increase) {
        if (useMinutes) {
            value += increase ? 10 : -10;
            if (value > 90) {
                value = 0;
            } else if (value < 0) {
                useMinutes = false;
                value = 59;
            }
        } else {
            value += increase ? 1 : -1;
            if (value > 59) {
                useMinutes = true;
                value = 0;
            } else if (value < 0) {
                value = 59;
            }
        }
        updateUIAndNotifyListener(true);
    }

    private void updateUIAndNotifyListener(boolean updateSeekBar) {
        if (updateSeekBar) {
            if (useMinutes) {
                seekbar.setValue(value);
            } else {
                seekbar.setValue(0);
            }
        }
        text.setText(MinTimeUtils.millisToPrettyString(getContext(), getValueInMillis()));
        if (cb != null) {
            cb.updateValue();
        }
    }
}
