package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object BGM {
    private const val TAG = "BGM"
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId = 0
    private var appContext: Context? = null
    private var isPausedByGame = false

    var isSoundOn = true
        set(value) {
            field = value
            SoundEffects.isSoundOn = value
            updateVolume()
            if (!value) {
                pauseCurrentPlayer()
            } else if (!isPausedByGame) {
                resume()
            }
        }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @Synchronized
    fun play(resId: Int) {
        val ctx = appContext ?: run {
            Log.w(TAG, "play() failed: appContext is null")
            return
        }

        if (currentResId == resId && mediaPlayer != null) {
            isPausedByGame = false
            resume()
            return
        }

        stop()

        try {
            currentResId = resId
            isPausedByGame = false
            mediaPlayer = MediaPlayer.create(ctx, resId).apply {
                isLooping = true
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "BGM MediaPlayer error: what=$what, extra=$extra")
                    stop()
                    true
                }
                updateVolume(this)
                if (isSoundOn) start()
            }
            Log.d(TAG, "play() started for resId: $resId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play BGM: ${e.message}")
            stop()
        }
    }

    @Synchronized
    fun stop() {
        Log.d(TAG, "stop() called (currentResId: $currentResId)")
        try {
            mediaPlayer?.setOnErrorListener(null)
            mediaPlayer?.setOnCompletionListener(null)
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop BGM: ${e.message}")
        } finally {
            mediaPlayer = null
            currentResId = 0
            isPausedByGame = false
        }
    }

    @Synchronized
    fun pause() {
        isPausedByGame = true
        pauseCurrentPlayer()
    }

    @Synchronized
    fun resume() {
        try {
            isPausedByGame = false
            if (isSoundOn && mediaPlayer != null && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume BGM: ${e.message}")
        }
    }

    private fun pauseCurrentPlayer() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause BGM: ${e.message}")
        }
    }

    private fun updateVolume(player: MediaPlayer? = mediaPlayer) {
        try {
            val volume = if (isSoundOn) 0.6f else 0f
            player?.setVolume(volume, volume)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update volume: ${e.message}")
        }
    }
}
