package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Canvas
import android.view.MotionEvent
import android.util.Log

abstract class Scene {
    open val world: World<*>? = null

    open fun update(frameTime: Float) {
        world?.update(frameTime)
    }

    open fun draw(canvas: Canvas) {
        world?.draw(canvas)
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
            Log.d("Scene", "push() called with scene: ${scene::class.simpleName}")
            current?.onPause()
            scenes.add(scene)
            scene.onEnter()
        }

        fun pop() {
            if (scenes.isEmpty()) {
                Log.w("Scene", "pop() called but scenes stack is empty")
                return
            }
            val last = scenes.removeAt(scenes.lastIndex)
            Log.d("Scene", "pop() called. Popped scene: ${last::class.simpleName}")
            last.onExit()
            current?.onResume()
        }

        fun change(scene: Scene) {
            Log.d("Scene", "change() called to scene: ${scene::class.simpleName}")
            if (scenes.isNotEmpty()) {
                val last = scenes.removeAt(scenes.lastIndex)
                last.onExit()
            }
            scenes.add(scene)
            scene.onEnter()
        }
    }
}
