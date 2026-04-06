package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Canvas

interface GameObject {
    fun update()
    fun draw(canvas: Canvas)
}
