/*
 * Counter.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Komponente, die einen Zähler repräsentiert. Der Wert kann mit Plus/Minus
 * eingestellt werden.
 *
 * @author Thomas
 */
public class Counter extends FrameLayout {

    private boolean useMinutes = true;
    private long value = 0;
    private int times = 0;

    private TextView text;
    private SeekBar seekbar;

    private ValueUpdater cb = null;

    public Counter(Context context) {
        this(context, null, 0);
    }

    public Counter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

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
                        + getClass().toString() + ": " + attr);
            }
        }
        a.recycle();
        View counter = inflate(context, R.layout.counter, this);
        TextView minus = counter.findViewById(R.id.minus);
        minus.setTextColor(color);
        minus.setOnClickListener(view -> changeValue(false));
        TextView plus = counter.findViewById(R.id.plus);
        plus.setTextColor(color);
        plus.setOnClickListener(view -> changeValue(true));
        text = counter.findViewById(R.id.text);
        text.setTextColor(color);
        seekbar = findViewById(R.id.seekbar);
        ColorStateList csl = ColorStateList.valueOf(color);
        seekbar.setThumbTintList(csl);
        seekbar.setProgressTintList(csl);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
                updateUIAndNotifyListener();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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
        updateUIAndNotifyListener();
    }

    public void setValueUpdator(ValueUpdater updater) {
        cb = updater;
    }

    private void changeValue(boolean increase) {
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
        if (value < 0) {
            if (useMinutes) {
                useMinutes = false;
                value = 59;
            } else {
                value = 0;
            }
        } else if (value > 59) {
            if (!useMinutes) {
                useMinutes = true;
                value = 0;
                times = 0;
            }
        }
        updateUIAndNotifyListener();
    }

    private void updateUIAndNotifyListener() {
        if (useMinutes) {
            seekbar.setProgress((int) value);
        } else {
            seekbar.setProgress(0);
        }
        text.setText(MinTime.millisToPrettyString(getContext(), getValueInMillis()));
        if (cb != null) {
            cb.updateValue();
        }
    }
}
