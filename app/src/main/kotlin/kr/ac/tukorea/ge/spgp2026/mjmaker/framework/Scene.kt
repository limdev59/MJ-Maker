package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Canvas
import android.view.MotionEvent

abstract class Scene {
    protected val objects = mutableListOf<GameObject>()

    open fun update() {
        for (obj in objects.toList()) {
            obj.update()
        }
    }

    open fun draw(canvas: Canvas) {
        for (obj in objects) {
            obj.draw(canvas)
        }
    }

    open fun onTouchEvent(event: MotionEvent): Boolean = false
    open fun add(obj: GameObject) = objects.add(obj)
    open fun remove(obj: GameObject) = objects.remove(obj)

    companion object {
        var current: Scene? = null
    }
}
