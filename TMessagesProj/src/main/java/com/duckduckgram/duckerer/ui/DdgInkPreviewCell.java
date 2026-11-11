package com.duckduckgram.duckerer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeColors;

/**
 * mvs Ink: предпросмотр палитры.
 *
 * Не картинка и не скриншот: ячейка рисует текущие цвета темы и выбранный
 * акцент, поэтому перекрашивается вместе с ними. Ничего не нажимается —
 * это витрина, а не настройка.
 */
public class DdgInkPreviewCell extends View {

    /** Тип строки для {@link DdgCategoryActivity.Row#custom(int)}. */
    public static final int TYPE = 464;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public DdgInkPreviewCell(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(172), MeasureSpec.EXACTLY));
    }

    private void box(Canvas canvas, float left, float top, float right, float bottom, float radius, int color) {
        rect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final boolean dark = Theme.isCurrentThemeNight();
        final int paper = dark ? 0xFF131312 : 0xFFF5F1E8;
        final int surface = dark ? 0xFF1B1B19 : 0xFFFFFDF7;
        final int surfaceAlt = dark ? 0xFF242320 : 0xFFECE6DA;
        final int ink = dark ? 0xFFF0EBE1 : 0xFF151412;
        final int muted = dark ? 0xFF7A7469 : 0xFF8A8478;
        final int hair = dark ? 0xFF2C2B28 : 0xFFDCD5C7;
        final int accent = ThemeColors.inkAccentColor();

        final float outer = AndroidUtilities.dp(16);
        final float radius = AndroidUtilities.dp(18);
        final float top = AndroidUtilities.dp(6);
        final float bottom = getHeight() - AndroidUtilities.dp(6);

        box(canvas, outer, top, getWidth() - outer, bottom, radius, paper);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, AndroidUtilities.dp(0.7f)));
        paint.setColor(hair);
        rect.set(outer, top, getWidth() - outer, bottom);
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);

        final float left = outer + AndroidUtilities.dp(16);
        final float sw = AndroidUtilities.dp(14);
        final float gap = AndroidUtilities.dp(6);
        float y = top + AndroidUtilities.dp(16);
        box(canvas, left, y, left + sw, y + sw, sw / 2f, surfaceAlt);
        box(canvas, left + sw + gap, y, left + 2 * sw + gap, y + sw, sw / 2f, ink);
        box(canvas, left + 2 * (sw + gap), y, left + 3 * sw + 2 * gap, y + sw, sw / 2f, accent);

        final float bubbleTop = y + sw + AndroidUtilities.dp(16);
        final float bubbleW = (getWidth() - 2 * outer) * 0.5f;
        box(canvas, left, bubbleTop, left + bubbleW, bubbleTop + AndroidUtilities.dp(40),
                AndroidUtilities.dp(12), surfaceAlt);
        box(canvas, left + AndroidUtilities.dp(12), bubbleTop + AndroidUtilities.dp(12),
                left + bubbleW - AndroidUtilities.dp(18), bubbleTop + AndroidUtilities.dp(16),
                AndroidUtilities.dp(2), muted);
        box(canvas, left + AndroidUtilities.dp(12), bubbleTop + AndroidUtilities.dp(24),
                left + bubbleW * 0.62f, bubbleTop + AndroidUtilities.dp(28), AndroidUtilities.dp(2), muted);

        final float outRight = getWidth() - outer - AndroidUtilities.dp(16);
        final float outW = (getWidth() - 2 * outer) * 0.44f;
        final float outTop = bubbleTop - AndroidUtilities.dp(10);
        box(canvas, outRight - outW, outTop, outRight, outTop + AndroidUtilities.dp(48),
                AndroidUtilities.dp(14), ink);
        box(canvas, outRight - outW + AndroidUtilities.dp(12), outTop + AndroidUtilities.dp(13),
                outRight - AndroidUtilities.dp(26), outTop + AndroidUtilities.dp(17),
                AndroidUtilities.dp(2), 0x99F5F1E8);
        box(canvas, outRight - outW + AndroidUtilities.dp(12), outTop + AndroidUtilities.dp(23),
                outRight - AndroidUtilities.dp(20), outTop + AndroidUtilities.dp(27),
                AndroidUtilities.dp(2), 0x99F5F1E8);
        box(canvas, outRight - outW + AndroidUtilities.dp(12), outTop + AndroidUtilities.dp(33),
                outRight - AndroidUtilities.dp(44), outTop + AndroidUtilities.dp(37),
                AndroidUtilities.dp(2), 0x66F5F1E8);
        box(canvas, outRight - AndroidUtilities.dp(16), outTop + AndroidUtilities.dp(34),
                outRight - AndroidUtilities.dp(10), outTop + AndroidUtilities.dp(40),
                AndroidUtilities.dp(3), accent);
    }
}
