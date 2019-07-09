/*
 * CanvasUtils.java
 *
 * TKWeek (c) Thomas Künneth 2014 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.mintime;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Diese Klasse enthält statische Methoden für die Arbeit mit Canvas-Objekten.
 *
 * @author Thomas
 */
class CanvasUtils {

    /**
     * Zeichnet einen Text, der in einer bounding box zentriert wird.
     *
     * @param canvas Canvas
     * @param posX   X-Position
     * @param posY   Y-Position
     * @param text   der auszugebende Text
     * @param paint  vorkonfigurierte Paint-Instanz
     */
    static void drawText(Canvas canvas, float posX, float posY,
                         String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float textY = posY - (float) bounds.top - (float) bounds.height() / 2f;
        canvas.drawText(text, posX, textY, paint);
    }

    /**
     * Berechnet die Schriftgröße, so dass der übergebene Text nicht breiter als
     * {@code width} ist
     *
     * @param paint Paint-Objekt
     * @param width maximale Breite
     * @param text  Text
     */
    static void calcTextHeight(Paint paint, int width, String text) {
        int size = 48;
        int last = size;
        boolean first = true;
        int offset = 0;
        while (last > 8) {
            paint.setTextSize(size);
            float currentWidth = paint.measureText(text);
            if (first) {
                offset = currentWidth < width ? 8 : -8;
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
