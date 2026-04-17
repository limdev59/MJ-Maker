package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.graphics.Canvas
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene

class TitleScene : Scene() {
    override fun update(frameTime: Float) {}
    override fun draw(canvas: Canvas) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            Scene.push(MainScene())
            return true
        }
        return false
    }
}
