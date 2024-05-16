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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

class CanvasUtils {

    static void drawText(Canvas canvas, float posX, float posY,
                         String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float textY = posY - (float) bounds.top - (float) bounds.height() / 2f;
        canvas.drawText(text, posX, textY, paint);
    }

    static void calcTextHeight(Paint paint, int width, int height, String text) {
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
                    if (currentWidth >= width || size > height) {
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
