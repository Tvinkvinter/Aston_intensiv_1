package com.atarusov.musicplayer.domain

import android.content.Context
import android.media.MediaPlayer
import com.atarusov.musicplayer.data.Song

class AudioPlayer(
    private val context: Context
) {

    private var player: MediaPlayer? = null
    private var currentSong: Song? = null

    private fun createPlayer(audioResourceId: Int) {
        player = MediaPlayer.create(context, audioResourceId)
    }

    fun play(song: Song) {
        if (currentSong == null || currentSong != song) setNewSong(song)
        player?.start()
    }

    private fun setNewSong(song: Song) {
        currentSong = song
        stop()
        createPlayer(song.resId)
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.stop()
        player?.release()
        currentSong = null
        player = null
    }
}