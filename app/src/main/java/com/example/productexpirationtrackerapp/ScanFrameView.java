package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

/**
 * Draws the 4-corner scan bracket frame (like a camera viewfinder).
 * Corners are animated with a subtle pulse.
 */
public class ScanFrameView extends View {

    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float cornerLen;
    private float cornerRadius;
    private float strokeWidth;
    private float pulseAlpha = 1f;

    public ScanFrameView(Context context) {
        super(context); init();
    }
    public ScanFrameView(Context context, AttributeSet attrs) {
        super(context, attrs); init();
    }
    public ScanFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        float dp = getResources().getDisplayMetrics().density;
        cornerLen    = 24f * dp;
        cornerRadius = 4f  * dp;
        strokeWidth  = 3f  * dp;

        cornerPaint.setColor(0xFF4DA6FF); // bright blue corners
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(strokeWidth);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        borderPaint.setColor(0x33FFFFFF); // faint white border inside frame
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f * dp);

        // Pulse animation
        ValueAnimator pulse = ValueAnimator.ofFloat(0.5f, 1f, 0.5f);
        pulse.setDuration(2000);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setInterpolator(new LinearInterpolator());
        pulse.addUpdateListener(a -> {
            pulseAlpha = (float) a.getAnimatedValue();
            cornerPaint.setAlpha((int)(pulseAlpha * 255));
            invalidate();
        });
        pulse.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        float cl = cornerLen, cr = cornerRadius;

        // Faint border around the whole frame
        RectF border = new RectF(strokeWidth, strokeWidth, w - strokeWidth, h - strokeWidth);
        canvas.drawRoundRect(border, 8f, 8f, borderPaint);

        // ── TOP-LEFT corner ──────────────────────────────────────────────
        canvas.drawLine(strokeWidth, cr + strokeWidth, strokeWidth, cl, cornerPaint);         // vertical
        canvas.drawLine(cr + strokeWidth, strokeWidth, cl, strokeWidth, cornerPaint);         // horizontal
        // inner radius arc
        canvas.drawArc(strokeWidth, strokeWidth, strokeWidth + cr * 2, strokeWidth + cr * 2,
                180f, 90f, false, cornerPaint);

        // ── TOP-RIGHT corner ─────────────────────────────────────────────
        canvas.drawLine(w - strokeWidth, cr + strokeWidth, w - strokeWidth, cl, cornerPaint);
        canvas.drawLine(w - cr - strokeWidth, strokeWidth, w - cl, strokeWidth, cornerPaint);
        canvas.drawArc(w - strokeWidth - cr * 2, strokeWidth, w - strokeWidth, strokeWidth + cr * 2,
                270f, 90f, false, cornerPaint);

        // ── BOTTOM-LEFT corner ───────────────────────────────────────────
        canvas.drawLine(strokeWidth, h - cr - strokeWidth, strokeWidth, h - cl, cornerPaint);
        canvas.drawLine(cr + strokeWidth, h - strokeWidth, cl, h - strokeWidth, cornerPaint);
        canvas.drawArc(strokeWidth, h - strokeWidth - cr * 2, strokeWidth + cr * 2, h - strokeWidth,
                90f, 90f, false, cornerPaint);

        // ── BOTTOM-RIGHT corner ──────────────────────────────────────────
        canvas.drawLine(w - strokeWidth, h - cr - strokeWidth, w - strokeWidth, h - cl, cornerPaint);
        canvas.drawLine(w - cr - strokeWidth, h - strokeWidth, w - cl, h - strokeWidth, cornerPaint);
        canvas.drawArc(w - strokeWidth - cr * 2, h - strokeWidth - cr * 2, w - strokeWidth, h - strokeWidth,
                0f, 90f, false, cornerPaint);
    }
}