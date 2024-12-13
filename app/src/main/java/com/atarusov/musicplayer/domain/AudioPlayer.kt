package com.atarusov.musicplayer.domain

import android.content.Context
import android.media.MediaPlayer

class AudioPlayer(
    private val context: Context
) {

    private var player: MediaPlayer? = null
    private var isPlaying: Boolean = false

    private fun createPlayer(audioResourceId: Int) {
        player = MediaPlayer.create(context, audioResourceId)
    }

    fun play() {
        player?.start()
        isPlaying = true
    }

    fun pause() {
        player?.pause()
        isPlaying = false
    }

    fun setNewSong(audioResourceId: Int) {
        stop()
        createPlayer(audioResourceId)
        if (isPlaying) play()
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}