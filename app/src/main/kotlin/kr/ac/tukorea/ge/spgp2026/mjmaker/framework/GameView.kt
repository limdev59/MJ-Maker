package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), Choreographer.FrameCallback {

    private var lastTimeNanos: Long = 0

    init {
        viewContext = context
        BitmapPool.init(resources)
        SoundEffects.init(context)
        BGM.init(context)
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        GameMetrics.layout.onSize(w, h)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (lastTimeNanos == 0L) {
            lastTimeNanos = frameTimeNanos
        }
        val frameTime = (frameTimeNanos - lastTimeNanos) / 1_000_000_000f
        lastTimeNanos = frameTimeNanos

        Scene.current?.update(frameTime)
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(GameMetrics.layout.transformMatrix)
        Scene.current?.draw(canvas)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pt = GameMetrics.layout.fromScreen(event.x, event.y)
        event.setLocation(pt.x, pt.y)
        return Scene.current?.onTouchEvent(event) ?: super.onTouchEvent(event)
    }

    companion object {
        lateinit var viewContext: Context
    }
}
