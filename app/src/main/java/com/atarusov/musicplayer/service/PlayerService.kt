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
import androidx.core.content.IntentCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.data.Song
import com.atarusov.musicplayer.domain.AudioPlayer
import com.atarusov.musicplayer.ui.MainActivity

class PlayerService : Service() {

    private val audioPlayer = AudioPlayer(this)
    private var isInitialized = false
    private var currentSong: Song? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.PLAY.toString() -> {
                currentSong = IntentCompat.getParcelableExtra(intent, MainActivity.SONG_EXTRA_KEY, Song::class.java)
                if (currentSong != null) play(currentSong!!)
                else throw IllegalArgumentException("Required data 'song_resource_id' is missing in the Intent extras")
            }

            Action.PAUSE.toString() -> pause()
            Action.STOP.toString() -> stopSelf()
            else -> sendEventToViewModel(Action.valueOf(intent?.action ?: ""))
        }

        return START_NOT_STICKY
    }

    private fun initialize() {
        isInitialized = true
        startForeground(1, createNotification(true))
    }

    private fun play(song: Song) {
        if (!isInitialized) initialize()

        audioPlayer.play(song)

        val notification = createNotification(isPlaying = true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) NotificationManagerCompat.from(this).notify(1, notification)
    }

    private fun pause() {
        audioPlayer.pause()
        val notification = createNotification(isPlaying = false)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) NotificationManagerCompat.from(this).notify(1, notification)
    }

    private fun sendEventToViewModel(action: Action) {
        val intent = Intent(PLAYER_ACTION).apply {
            putExtra(MainActivity.ACTION_EXTRA_KEY, action.toString())
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
            getString(R.string.accessibility_next_btn),
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
            getString(R.string.accessibility_prev_btn),
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
                getString(R.string.accessibility_play_pause_btn),
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
                getString(R.string.accessibility_play_pause_btn),
                playPendingIntent
            ).build()
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(currentSong?.name)
            .setContentText(currentSong?.author)
            .setOngoing(isPlaying)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setColor(getColor(R.color.surface))
            .setColorized(true)
            .setStyle(Notification.MediaStyle())
            .build()

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
        PLAY, PAUSE, STOP, NOTIFICATION_PLAY, NOTIFICATION_PAUSE, NOTIFICATION_PREV, NOTIFICATION_NEXT
    }

    companion object {
        const val PLAYER_ACTION = "PLAYER_ACTION"
        const val CHANNEL_ID = "player_channel"
    }
}
