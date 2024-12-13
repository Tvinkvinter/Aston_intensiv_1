package com.atarusov.musicplayer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.domain.AudioPlayer

class PlayerService : Service() {

    private val audioPlayer = AudioPlayer(this)
    private var isInitialized = false

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.INIT.toString() -> {
                if (!isInitialized) {
                    setNewSong(intent.getIntExtra("song_resource_id", -1))
                    isInitialized = true
                }
            }
            Action.START.toString() -> start()
            Action.PAUSE.toString() -> pause()
            Action.SET.toString() -> setNewSong(intent.getIntExtra("song_resource_id", -1))
            Action.STOP.toString() -> stopSelf()
        }

        return START_NOT_STICKY
    }


    private fun start() {
        val notification = NotificationCompat.Builder(this, "player_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Test Title")
            .setContentText("Test")
            .build()
        startForeground(1, notification)

        audioPlayer.play()
    }

    private fun pause() {
        audioPlayer.pause()
    }

    private fun setNewSong(songResourceId: Int) {
        if (songResourceId != -1) audioPlayer.setNewSong(songResourceId)
        else throw IllegalArgumentException("Required data 'song_resource_id' is missing in the Intent extras")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
    }

    enum class Action {
        INIT, START, PAUSE, SET, STOP
    }
}
