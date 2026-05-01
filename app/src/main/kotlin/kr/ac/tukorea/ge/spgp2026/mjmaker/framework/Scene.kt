package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Canvas
import android.view.MotionEvent

abstract class Scene {
    protected val objects = mutableListOf<GameObject>()

    open fun update(frameTime: Float) {
        for (obj in objects.toList()) {
            obj.update(frameTime)
        }
    }

    open fun draw(canvas: Canvas) {
        for (obj in objects) {
            obj.draw(canvas)
        }
    }

    open fun add(obj: GameObject) {
        objects.add(obj)
    }

    open fun onEnter() {}
    open fun onExit() {}
    open fun onPause() {}
    open fun onResume() {}
    open fun onTouchEvent(event: MotionEvent): Boolean = false

    companion object {
        private val scenes = mutableListOf<Scene>()
        val current: Scene? get() = scenes.lastOrNull()

        fun push(scene: Scene) {
            current?.onPause()
            scenes.add(scene)
            scene.onEnter()
        }

        fun pop() {
            val last = scenes.removeAt(scenes.lastIndex)
            last.onExit()
            current?.onResume()
        }

        fun change(scene: Scene) {
            if (scenes.isNotEmpty()) {
                val last = scenes.removeAt(scenes.lastIndex)
                last.onExit()
            }
            scenes.add(scene)
            scene.onEnter()
        }
    }
}
