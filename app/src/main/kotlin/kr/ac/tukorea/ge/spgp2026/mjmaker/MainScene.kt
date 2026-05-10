package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.World

class MainScene : Scene() {
    enum class Layer {
        Background, Stall, Current, UI
    }

    override val world = World(Layer.entries.toTypedArray())

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 현재 투하 중인 객체는 'Current' 레이어에 추가
            world.add(Layer.Current, MJPhoto(android.R.drawable.ic_menu_gallery, event.x, event.y))
            return true
        }
        return false
    }
}
