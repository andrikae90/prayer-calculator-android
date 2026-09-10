package com.example.prayercalculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.prayercalculator.data.PrayerNotificationSound

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(PrayerNotificationManager.EXTRA_PRAYER_NAME) ?: return
        val prayerTime = intent.getStringExtra(PrayerNotificationManager.EXTRA_PRAYER_TIME) ?: ""
        val selectedSoundName = intent.getStringExtra(PrayerNotificationManager.EXTRA_NOTIFICATION_SOUND)
        val selectedSound = runCatching {
            selectedSoundName?.let { PrayerNotificationSound.valueOf(it) }
        }.getOrNull() ?: PrayerNotificationSound.BIP_PANJANG

        if (selectedSound == PrayerNotificationSound.TANPA_NOTIFIKASI) return

        val notification = NotificationCompat.Builder(context, PrayerNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Waktu Sholat $prayerName")
            .setContentText("Sekarang masuk waktu $prayerName${if (prayerTime.isNotBlank()) " • $prayerTime" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSilent(true)
            .build()

        NotificationManagerCompat.from(context).notify(prayerName.hashCode(), notification)

        when (selectedSound) {
            PrayerNotificationSound.BIP_PANJANG -> playLongBeep()
            PrayerNotificationSound.ADZAN_LENGKAP -> {
                val audioName = if (prayerName.equals("Subuh", ignoreCase = true)) {
                    "adzan_subuh"
                } else {
                    "adzan_lengkap"
                }
                playBundledAudio(context, audioName)
            }
            PrayerNotificationSound.TAKBIR_SAJA -> playBundledAudio(context, "takbir_saja")
            PrayerNotificationSound.GETAR_SAJA,
            PrayerNotificationSound.TANPA_NOTIFIKASI -> Unit
        }
    }

    private fun playLongBeep() {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
        tone.release()
    }

    private fun playBundledAudio(context: Context, resourceName: String) {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) return

        val player = MediaPlayer.create(context, resourceId) ?: return
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player.setOnCompletionListener { it.release() }
        player.setOnErrorListener { mp, _, _ ->
            mp.release()
            true
        }
        player.start()
    }
}
