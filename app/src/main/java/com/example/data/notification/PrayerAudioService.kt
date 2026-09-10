package com.example.data.notification

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R

class PrayerAudioService : Service() {
    companion object {
        const val EXTRA_AUDIO_NAME = "extra_audio_name"
        private const val SERVICE_NOTIFICATION_ID = 9101
    }

    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, DefaultPrayerNotificationManager.CHANNEL_PRAYER_ALERT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("TEMAN SHOLAT")
            .setContentText("Memutar audio waktu sholat")
            .setOngoing(true)
            .setSilent(true)
            .build()
        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resourceName = intent?.getStringExtra(EXTRA_AUDIO_NAME)
        if (resourceName.isNullOrBlank()) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        stopCurrentPlayer()
        val resourceId = resources.getIdentifier(resourceName, "raw", packageName)
        if (resourceId == 0) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        player = runCatching {
            MediaPlayer.create(this, resourceId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener {
                    stopCurrentPlayer()
                    stopSelf(startId)
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    player = null
                    stopSelf(startId)
                    true
                }
            }
        }.getOrNull()

        if (player == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        runCatching { player?.start() }.onFailure {
            stopCurrentPlayer()
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopCurrentPlayer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopCurrentPlayer() {
        player?.let { runCatching { if (it.isPlaying) it.stop() }; it.release() }
        player = null
    }
}
