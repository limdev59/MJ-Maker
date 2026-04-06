package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), Choreographer.FrameCallback {

    init {
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        Scene.current?.update()
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        Scene.current?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return Scene.current?.onTouchEvent(event) ?: super.onTouchEvent(event)
    }
}
