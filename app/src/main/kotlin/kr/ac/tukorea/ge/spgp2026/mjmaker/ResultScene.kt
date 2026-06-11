package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene

class ResultScene(private val score: Int, private val bestScore: Int) : Scene() {
    private val bgPaint = Paint().apply {
        color = Color.parseColor("#0F0F1E")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#E53935")
        isAntiAlias = true
        textSize = 110f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val scoreLabelPaint = Paint().apply {
        color = Color.parseColor("#888888")
        isAntiAlias = true
        textSize = 42f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        textSize = 85f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val lobbyPaint = Paint().apply {
        color = Color.parseColor("#00ACC1")
        isAntiAlias = true
        textSize = 55f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#333344")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val cardRect = RectF(150f, 480f, 750f, 980f)

    override fun update(frameTime: Float) {}

    override fun draw(canvas: Canvas) {
        canvas.drawPaint(bgPaint)

        titlePaint.color = Color.parseColor("#33000000")
        canvas.drawText("GAME OVER", 456f, 356f, titlePaint)
        titlePaint.color = Color.parseColor("#FF1744")
        canvas.drawText("GAME OVER", 450f, 350f, titlePaint)

        val shadowPaint = Paint().apply {
            color = Color.parseColor("#55000000")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            cardRect.left + 8f,
            cardRect.top + 8f,
            cardRect.right + 8f,
            cardRect.bottom + 8f,
            20f,
            20f,
            shadowPaint
        )

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1B1B2F")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 20f, 20f, bodyPaint)
        canvas.drawRoundRect(cardRect, 20f, 20f, borderPaint)

        canvas.drawText("YOUR SCORE", 450f, 580f, scoreLabelPaint)
        canvas.drawText(String.format("%05d", score), 450f, 670f, scorePaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#333344")
            strokeWidth = 3f
        }
        canvas.drawLine(200f, 720f, 700f, 720f, linePaint)

        canvas.drawText("BEST SCORE", 450f, 800f, scoreLabelPaint)
        canvas.drawText(String.format("%05d", bestScore), 450f, 890f, scorePaint)

        lobbyPaint.color = if ((System.currentTimeMillis() / 500) % 2 == 0L) {
            Color.parseColor("#00E5FF")
        } else {
            Color.parseColor("#00838F")
        }
        canvas.drawText("TAP TO LOBBY", 450f, 1200f, lobbyPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Scene.change(TitleScene())
            return true
        }
        return false
    }
}
