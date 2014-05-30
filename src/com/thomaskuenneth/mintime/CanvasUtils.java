/*
 * CanvasUtils.java
 * 
 * TKWeek (c) Thomas K�nneth 2014
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;

/**
 * Diese Klasse enth�lt statische Methoden f�r die Arbeit mit Canvas-Objekten.
 * 
 * @author Thomas
 * 
 */
public class CanvasUtils {

	/**
	 * Zeichnet einen Text, der in einer bounding box hentriert wird.
	 * 
	 * @param canvas
	 *            Canvas
	 * @param posX
	 *            X-Position
	 * @param posY
	 *            Y-Position
	 * @param text
	 *            der auszugebende Text
	 * @param paint
	 *            vorkonfigurierte Paint-Instanz
	 */
	public static void drawText(Canvas canvas, float posX, float posY,
			String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		float textY = posY - (float) bounds.top - (float) bounds.height() / 2f;
		canvas.drawText(text, posX, textY, paint);
	}

	/**
	 * Liefert die Bildschirmgr��e in Pixel.
	 * 
	 * @param context
	 *            Context
	 * @return Bildschirmgr��e in Pixel
	 */
	public static Point getScreenSize(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point outSize = new Point();
		display.getSize(outSize);
		return outSize;
	}

	/**
	 * Berechnet die Schriftgr��e, so dass der �bergebene Text nicht breiter als
	 * {@code width} ist
	 * 
	 * @param paint
	 *            Paint-Objekt
	 * @param width
	 *            maximale Breite
	 * @param text
	 *            Text
	 */
	public static void calcTextHeight(Paint paint, int width, String text) {
		int size = 48;
		int last = size;
		boolean first = true;
		int offset = 0;
		while (last > 4) {
			paint.setTextSize(size);
			float currentWidth = paint.measureText(text);
			if (first) {
				offset = currentWidth < width ? 4 : -4;
				first = false;
			} else {
				if (offset > 0) {
					if (currentWidth >= width) {
						break;
					}
				} else {
					if (currentWidth <= width) {
						break;
					}
				}
			}
			last = size;
			size = size + offset;
		}
		paint.setTextSize(last);
	}
}
