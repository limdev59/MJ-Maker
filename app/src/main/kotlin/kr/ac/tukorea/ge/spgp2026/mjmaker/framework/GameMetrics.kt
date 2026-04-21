package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log

class GameMetrics {
    var width = 900f
        private set
    var height = 1600f
        private set

    val transformMatrix = Matrix()
    val inverseTransformMatrix = Matrix()
    val screenRect = RectF()

    private val touchPoint = floatArrayOf(0f, 0f)
    private val sharedPointForReturn = PointF()

    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    fun onSize(w: Int, h: Int) {
        val scaleX = w / width
        val scaleY = h / height
        val scale = minOf(scaleX, scaleY)
        val contentWidth = width * scale
        val contentHeight = height * scale
        val offsetX = (w - contentWidth) / 2f
        val offsetY = (h - contentHeight) / 2f
        
        transformMatrix.reset()
        transformMatrix.postTranslate(offsetX, offsetY)
        transformMatrix.postScale(scale, scale, offsetX, offsetY)
        transformMatrix.invert(inverseTransformMatrix)

        screenRect.set(0f, 0f, w.toFloat(), h.toFloat())
        inverseTransformMatrix.mapRect(screenRect)
        Log.d("GameMetrics", "onSize: screen=${w}x$h, virtual=${width}x$height, screenRect=$screenRect")
    }

    fun fromScreen(x: Float, y: Float): PointF {
        touchPoint[0] = x
        touchPoint[1] = y
        inverseTransformMatrix.mapPoints(touchPoint)
        sharedPointForReturn.set(touchPoint[0], touchPoint[1])
        return sharedPointForReturn
    }

    fun toScreen(x: Float, y: Float): PointF {
        touchPoint[0] = x
        touchPoint[1] = y
        transformMatrix.mapPoints(touchPoint)
        sharedPointForReturn.set(touchPoint[0], touchPoint[1])
        return sharedPointForReturn
    }

    companion object {
        val layout = GameMetrics()
    }
}
