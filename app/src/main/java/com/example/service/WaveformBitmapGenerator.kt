package com.example.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sin

object WaveformBitmapGenerator {

    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F04438")
        style = Paint.Style.FILL
    }

    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FFFFFF")
        style = Paint.Style.FILL
    }

    /**
     * Generates a dynamic visualizer bitmap responding to captured sound RMS level.
     *
     * @param rmsDb Current sound amplitude in dB (typically -2.0 to +10.0 from recognizer)
     * @param isCapturingSound True when microphone is actively capturing voice/speech
     * @param isPaused True when session is paused
     * @param animFrame Monotonically increasing frame counter for fluid oscillation
     */
    fun createWaveformBitmap(
        rmsDb: Float,
        isCapturingSound: Boolean,
        isPaused: Boolean,
        animFrame: Int = 0
    ): Bitmap {
        val width = 140
        val height = 64
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerY = height / 2f

        // If paused or not capturing sound, draw resting baseline waveform
        if (isPaused || !isCapturingSound || rmsDb < 0.5f) {
            // Resting state: 1 dot, 5 low bars (with center red), 1 dot
            // Left dot
            canvas.drawCircle(12f, centerY, 3.5f, dimPaint)

            // Bar 1
            drawCenteredBar(canvas, 32f, centerY, 5f, 10f, dimPaint)
            // Bar 2
            drawCenteredBar(canvas, 48f, centerY, 5f, 14f, dimPaint)
            // Bar 3 (Center Red)
            drawCenteredBar(canvas, 64f, centerY, 5f, 18f, redPaint)
            // Bar 4
            drawCenteredBar(canvas, 80f, centerY, 5f, 14f, dimPaint)
            // Bar 5
            drawCenteredBar(canvas, 96f, centerY, 5f, 10f, dimPaint)

            // Right dot
            canvas.drawCircle(116f, centerY, 3.5f, dimPaint)
            return bitmap
        }

        // Active sound capturing: Calculate normalized volume
        // rmsDb is usually 0.5 to 10.0+
        val normRms = ((rmsDb - 0.5f) / 7.5f).coerceIn(0.15f, 1.0f)

        // Time oscillation based on frame counter for dynamic wave feel
        val t = animFrame * 0.45f
        val osc1 = 0.7f + 0.3f * sin(t + 0.5).toFloat()
        val osc2 = 0.7f + 0.3f * sin(t + 1.2).toFloat()
        val osc3 = 0.8f + 0.2f * sin(t + 2.0).toFloat()
        val osc4 = 0.7f + 0.3f * sin(t + 2.8).toFloat()
        val osc5 = 0.7f + 0.3f * sin(t + 3.6).toFloat()

        val maxBarHeight = height * 0.85f // ~54px

        val h1 = (10f + (maxBarHeight - 10f) * normRms * 0.45f * osc1).coerceIn(8f, maxBarHeight)
        val h2 = (14f + (maxBarHeight - 14f) * normRms * 0.75f * osc2).coerceIn(10f, maxBarHeight)
        val h3 = (18f + (maxBarHeight - 18f) * normRms * 1.00f * osc3).coerceIn(14f, maxBarHeight)
        val h4 = (14f + (maxBarHeight - 14f) * normRms * 0.80f * osc4).coerceIn(10f, maxBarHeight)
        val h5 = (10f + (maxBarHeight - 10f) * normRms * 0.50f * osc5).coerceIn(8f, maxBarHeight)

        // Pulsing dots
        val dotRadius = (3.5f + 1.5f * normRms).coerceIn(3.5f, 5f)
        canvas.drawCircle(12f, centerY, dotRadius, whitePaint)

        // Dancing dynamic equalizer bars
        drawCenteredBar(canvas, 32f, centerY, 5.5f, h1, whitePaint)
        drawCenteredBar(canvas, 48f, centerY, 5.5f, h2, whitePaint)
        drawCenteredBar(canvas, 64f, centerY, 5.5f, h3, redPaint)
        drawCenteredBar(canvas, 80f, centerY, 5.5f, h4, whitePaint)
        drawCenteredBar(canvas, 96f, centerY, 5.5f, h5, whitePaint)

        // Right dot
        canvas.drawCircle(116f, centerY, dotRadius, whitePaint)

        return bitmap
    }

    private fun drawCenteredBar(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        barWidth: Float,
        barHeight: Float,
        paint: Paint
    ) {
        val halfW = barWidth / 2f
        val halfH = barHeight / 2f
        val rect = RectF(
            centerX - halfW,
            centerY - halfH,
            centerX + halfW,
            centerY + halfH
        )
        canvas.drawRoundRect(rect, halfW, halfW, paint)
    }
}
