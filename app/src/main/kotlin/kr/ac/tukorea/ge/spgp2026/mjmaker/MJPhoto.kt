package kr.ac.tukorea.ge.spgp2026.mjmaker

import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Sprite

class MJPhoto(resId: Int, startX: Float, startY: Float) : Sprite(resId) {
    init {
        x = startX
        y = startY
        width = 100f
        height = 100f
    }

    private var dy = 0f
    private val gravity = 1000f // 1000 pixels/sec^2

    override fun update(frameTime: Float) {
        dy += gravity * frameTime
        y += dy * frameTime

        // 바닥에 닿으면 멈추는 간단한 로직 (가상 높이 1600 기준)
        if (y > 1500f) {
            y = 1500f
            dy = 0f
        }
    }
}
