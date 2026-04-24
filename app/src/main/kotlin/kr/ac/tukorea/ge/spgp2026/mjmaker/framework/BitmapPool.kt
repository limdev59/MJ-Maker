package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class BitmapPool(private val resources: Resources) {
    private val bitmaps = mutableMapOf<Int, Bitmap>()
    private val decodeOptions = BitmapFactory.Options().apply {
        inScaled = false
    }

    fun get(id: Int): Bitmap {
        return bitmaps.getOrPut(id) {
            BitmapFactory.decodeResource(resources, id, decodeOptions)
        }
    }

    companion object {
        private var instance: BitmapPool? = null
        fun getInstance(): BitmapPool = instance!!
        fun init(resources: Resources) {
            instance = BitmapPool(resources)
        }
    }
}
