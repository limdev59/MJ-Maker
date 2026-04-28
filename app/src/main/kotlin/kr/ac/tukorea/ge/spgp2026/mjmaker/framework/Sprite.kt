package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF

open class Sprite(resId: Int) : GameObject {
    protected var bitmap: Bitmap = BitmapPool.getInstance().get(resId)
    protected var srcRect: Rect? = null
    protected val dstRect = RectF()

    var x = 0f
    var y = 0f
    var width = 0f
    var height = 0f

    val bitmapWidth: Int get() = bitmap.width
    val bitmapHeight: Int get() = bitmap.height

    override fun update(frameTime: Float) {}

    override fun draw(canvas: Canvas) {
        dstRect.set(
            x - width / 2f,
            y - height / 2f,
            x + width / 2f,
            y + height / 2f
        )
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }
}
