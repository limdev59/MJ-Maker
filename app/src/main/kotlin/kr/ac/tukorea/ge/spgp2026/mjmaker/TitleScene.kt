package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.graphics.*
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.SoundEffects

class TitleScene : Scene() {
    private var lpAngle = 0f

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#0F0F1E")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint().apply {
        color = Color.parseColor("#FFD600")
        isAntiAlias = true
        textSize = 120f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val subTitlePaint = Paint().apply {
        color = Color.parseColor("#888888")
        isAntiAlias = true
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val startPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        isAntiAlias = true
        textSize = 55f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val lpPaint = Paint().apply {
        color = Color.parseColor("#111111")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val lpGroovePaint = Paint().apply {
        color = Color.parseColor("#22FFFFFF")
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val lpOutlinePaint = Paint().apply {
        color = Color.parseColor("#33FFFFFF")
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val quotePaint = Paint().apply {
        color = Color.parseColor("#66FFFFFF")
        isAntiAlias = true
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
    }

    override fun update(frameTime: Float) {
        // 매초 35도씩 LP판 회전
        lpAngle += 35f * frameTime
    }

    override fun draw(canvas: Canvas) {
        // 1. 배경 채우기
        canvas.drawPaint(bgPaint)

        // 2. 타이틀 텍스트 드로잉
        titlePaint.color = Color.parseColor("#33000000")
        canvas.drawText("MJ-MAKER", 450f + 6f, 250f + 6f, titlePaint)
        titlePaint.color = Color.parseColor("#FFD600")
        canvas.drawText("MJ-MAKER", 450f, 250f, titlePaint)

        subTitlePaint.color = Color.parseColor("#00E5FF")
        canvas.drawText("예예 히히 샤몬", 450f, 310f, subTitlePaint)

        // 3. 중앙 거대 LP 레코드 드로잉
        val lpX = 450f
        val lpY = 750f
        val lpRadius = 260f
        
        // LP판 바디
        canvas.drawCircle(lpX, lpY, lpRadius, lpPaint)
        canvas.drawCircle(lpX, lpY, lpRadius, lpOutlinePaint)

        // 음각 홈
        val numGrooves = 8
        for (i in 1..numGrooves) {
            canvas.drawCircle(lpX, lpY, lpRadius * (i.toFloat() / (numGrooves + 1)), lpGroovePaint)
        }

        // 중앙 라벨 그라데이션
        canvas.save()
        canvas.rotate(lpAngle, lpX, lpY)
        
        val labelRadius = lpRadius * 0.42f
        val labelPaint = Paint().apply {
            isAntiAlias = true
            shader = RadialGradient(
                lpX, lpY, labelRadius,
                Color.parseColor("#D50000"), Color.parseColor("#FFAB00"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(lpX, lpY, labelRadius, labelPaint)
        canvas.drawCircle(lpX, lpY, labelRadius, lpOutlinePaint)

        // 라벨 내부 텍스트
        val labelTextPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("KING OF POP", lpX, lpY - 20f, labelTextPaint)
        labelTextPaint.textSize = 22f
        canvas.drawText("CAREER LP", lpX, lpY + 20f, labelTextPaint)

        // 센터 홀
        val centerPaint = Paint().apply {
            color = Color.parseColor("#0F0F1E")
            isAntiAlias = true
        }
        canvas.drawCircle(lpX, lpY, labelRadius * 0.12f, centerPaint)
        canvas.drawCircle(lpX, lpY, labelRadius * 0.12f, lpOutlinePaint)

        canvas.restore()

        // 광택 (고정된 각도로 얹어줌)
        val shinePaint = Paint().apply {
            color = Color.parseColor("#15FFFFFF")
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.save()
        val path = Path().apply {
            moveTo(lpX, lpY)
            arcTo(lpX - lpRadius, lpY - lpRadius, lpX + lpRadius, lpY + lpRadius, -55f, 30f, false)
            close()
            moveTo(lpX, lpY)
            arcTo(lpX - lpRadius, lpY - lpRadius, lpX + lpRadius, lpY + lpRadius, 125f, 30f, false)
            close()
        }
        canvas.drawPath(path, shinePaint)
        canvas.restore()

        // 4. 명언 텍스트
        canvas.drawText("\"히히! 예예! 아우! 샤몬!\"", 450f, 1150f, quotePaint)

        // 5. Tap To Start 깜빡임
        val blink = (System.currentTimeMillis() / 500) % 2 == 0L
        if (blink) {
            startPaint.color = Color.parseColor("#00E5FF")
        } else {
            startPaint.color = Color.parseColor("#00838F")
        }
        canvas.drawText("히히 눌러!", 450f, 1330f, startPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            SoundEffects.play(R.raw.heehee)
            Scene.change(MainScene())
            return true
        }
        return false
    }
}

