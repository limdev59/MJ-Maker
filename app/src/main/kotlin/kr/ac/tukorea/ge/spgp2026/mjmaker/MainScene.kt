package kr.ac.tukorea.ge.spgp2026.mjmaker

import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.mjmaker.framework.Scene

class MainScene : Scene() {
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 터치한 위치(event.x, event.y)에 MJ 사진 객체 생성
            // 리소스 ID는 임시로 설정
            add(MJPhoto(android.R.drawable.ic_menu_gallery, event.x, event.y))
            return true
        }
        return false
    }
}
