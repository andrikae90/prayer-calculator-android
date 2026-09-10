package com.example.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.PrayerNotificationSound

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(DefaultPrayerNotificationManager.EXTRA_PRAYER_NAME) ?: "Salat"
        val prayerTime = intent.getStringExtra(DefaultPrayerNotificationManager.EXTRA_PRAYER_TIME) ?: ""
        val sound = intent.getStringExtra(DefaultPrayerNotificationManager.EXTRA_NOTIFICATION_SOUND)
            ?.let { runCatching { PrayerNotificationSound.valueOf(it) }.getOrNull() }
            ?: PrayerNotificationSound.BIP_PANJANG

        if (sound == PrayerNotificationSound.TANPA_NOTIFIKASI) return

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context, prayerName.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val vibration = if (sound == PrayerNotificationSound.GETAR_SAJA) longArrayOf(0, 500, 250, 500) else longArrayOf()
        val notification = NotificationCompat.Builder(context, DefaultPrayerNotificationManager.CHANNEL_PRAYER_ALERT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktu $prayerName")
            .setContentText("Sudah masuk waktu salat $prayerName${if (prayerTime.isNotBlank()) " • $prayerTime" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .setSilent(true)
            .apply { if (vibration.isNotEmpty()) setVibrate(vibration) }
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify("prayer_${prayerName}_${System.currentTimeMillis()}".hashCode(), notification)

        when (sound) {
            PrayerNotificationSound.BIP_PANJANG -> playLongBeep(context)
            PrayerNotificationSound.ADZAN_LENGKAP -> playBundledAudio(context, "adzan_lengkap")
            PrayerNotificationSound.TAKBIR_SAJA -> playBundledAudio(context, "takbir_saja")
            PrayerNotificationSound.GETAR_SAJA,
            PrayerNotificationSound.TANPA_NOTIFIKASI -> Unit
        }
    }

    private fun playLongBeep(context: Context) {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
        android.os.Handler(context.mainLooper).postDelayed({ tone.release() }, 1300)
    }

    /**
     * Plays a legally distributable audio asset bundled in res/raw.
     * Expected filenames:
     *   res/raw/adzan_lengkap.*
     *   res/raw/takbir_saja.*
     *
     * The resource is looked up dynamically so the project can compile even
     * before the licensed audio files are supplied. If the asset is missing,
     * the notification still appears but no audio is played.
     */
    private fun playBundledAudio(context: Context, resourceName: String) {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) return

        val player = runCatching {
            MediaPlayer.create(context, resourceId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener { it.release() }
                setOnErrorListener { mp, _, _ -> mp.release(); true }
            }
        }.getOrNull() ?: return

        runCatching { player.start() }.onFailure { player.release() }
    }
}
