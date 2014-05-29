package com.thomaskuenneth.mintime;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CanvasUtils {

	public static void drawText(Canvas canvas, float posX, float posY,
			String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		float textY = posY - (float) bounds.top - (float) bounds.height() / 2f;
		canvas.drawText(text, posX, textY, paint);
	}

}
