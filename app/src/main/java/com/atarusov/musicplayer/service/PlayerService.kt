package com.atarusov.musicplayer.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

            Action.PLAY.toString() -> play()
            Action.PAUSE.toString() -> pause()
            Action.SET.toString() -> setNewSong(intent.getIntExtra("song_resource_id", -1))
            Action.STOP.toString() -> stopSelf()
            else -> sendEventToViewModel(Action.valueOf(intent?.action ?: ""))
        }

        return START_NOT_STICKY
    }


    private fun play() {
        startForeground(1, createNotification(true))

        audioPlayer.play()
    }

    private fun pause() {
        audioPlayer.pause()
        val notification = createNotification(isPlaying = false)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) NotificationManagerCompat.from(this).notify(1, notification)
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

    private fun createNotification(isPlaying: Boolean): Notification {

        val nextPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PlayerService::class.java).apply {
                action = Action.NOTIFICATION_NEXT.toString()
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_notification_next_24),
            "Next",
            nextPendingIntent
        ).build()

        val prevPendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PlayerService::class.java).apply {
                action = Action.NOTIFICATION_PREV.toString()
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val prevAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_notification_prev_24),
            "Prev",
            prevPendingIntent
        ).build()

        val playPauseAction = if (isPlaying) {
            val pausePendingIntent = PendingIntent.getService(
                this, 1,
                Intent(this, PlayerService::class.java).apply {
                    action = Action.NOTIFICATION_PAUSE.toString()
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_notification_pause_24),
                "Pause",
                pausePendingIntent
            ).build()
        } else {
            val playPendingIntent = PendingIntent.getService(
                this, 2,
                Intent(this, PlayerService::class.java).apply {
                    action = Action.NOTIFICATION_PLAY.toString()
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_notification_play_24),
                "Play",
                playPendingIntent
            ).build()
        }
        return Notification.Builder(this, "player_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Audio Player")
            .setContentText(if (isPlaying) "Playing..." else "Paused")
            .setOngoing(isPlaying)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setColor(getColor(R.color.surface))
            .setColorized(true)
            .setStyle(Notification.MediaStyle())
            .build()

    }

    private fun sendEventToViewModel(action: Action) {
        val intent = Intent("PLAYER_ACTION").apply {
            putExtra("action", action.toString())
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    enum class Action {
        INIT, PLAY, PAUSE, SET, STOP, NOTIFICATION_PLAY, NOTIFICATION_PAUSE, NOTIFICATION_PREV, NOTIFICATION_NEXT
    }
}
