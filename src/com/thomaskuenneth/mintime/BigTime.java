package com.thomaskuenneth.mintime;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class BigTime extends View {

	private final Paint paint;
	private int color;
	private String text = "";

	public BigTime(Context context) {
		this(context, null, 0);
	}

	public BigTime(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BigTime(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		color = Color.WHITE;
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.Counter);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.Counter_color:
				color = a.getColor(i, color);
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
		paint.setTextSize(48);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float width = getWidth();
		float height = getHeight();
		paint.setColor(color);
		CanvasUtils.drawText(canvas, width / 2, height / 2, text, paint);
	}

	public void setColor(int color) {
		this.color = color;
		postInvalidate();
	}

	public void setText(String text) {
		this.text = text;
		postInvalidate();
	}
}
