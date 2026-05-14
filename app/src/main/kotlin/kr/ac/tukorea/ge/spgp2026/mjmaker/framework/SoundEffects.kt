package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kr.ac.tukorea.ge.spgp2026.mjmaker.R

class SoundEffects {
    companion object {
        private const val TAG = "SoundEffects"
        private var soundPool: SoundPool? = null
        private val soundIdMap = mutableMapOf<Int, Int>()
        private var lastPlayTime = 0L
        private const val GLOBAL_COOLDOWN_MS = 100L
        var isSoundOn = true

        fun init(context: Context) {
            if (soundPool != null) return

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            // 효과음 등록 및 캐싱
            load(context, R.raw.dahowda)
            load(context, R.raw.daw)
            load(context, R.raw.dhow)
            load(context, R.raw.heehee)
            load(context, R.raw.siu)
            load(context, R.raw.yeahyeah)
            load(context, R.raw.yeeeeah)
            load(context, R.raw.ddadda)
            load(context, R.raw.do2)
            load(context, R.raw.vocal)

            Log.d(TAG, "SoundEffects system initialized.")
        }

        private fun load(context: Context, resId: Int) {
            soundPool?.let { pool ->
                val soundId = pool.load(context, resId, 1)
                soundIdMap[resId] = soundId
            }
        }

        fun play(resId: Int) {
            if (!isSoundOn) return
            val now = System.currentTimeMillis()
            if (now - lastPlayTime < GLOBAL_COOLDOWN_MS) {
                return // 너무 빠른 연속 재생 차단해 음질 깨짐 방지
            }
            lastPlayTime = now

            val soundId = soundIdMap[resId]
            if (soundId != null && soundId != 0) {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            } else {
                Log.w(TAG, "Sound resource $resId not loaded.")
            }
        }
    }
}
