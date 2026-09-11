package com.example.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.PrayerNotificationSound
import kotlin.math.PI
import kotlin.math.sin

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
            PrayerNotificationSound.ADZAN_LENGKAP -> {
                val audioName = if (prayerName.equals("Subuh", ignoreCase = true)) "adzan_subuh" else "adzan_lengkap"
                startPrayerAudioService(context, audioName)
            }
            PrayerNotificationSound.TAKBIR_SAJA -> startPrayerAudioService(context, "takbir_saja")
            PrayerNotificationSound.GETAR_SAJA,
            PrayerNotificationSound.TANPA_NOTIFIKASI -> Unit
        }
    }

    private fun playLongBeep(context: Context) {
        val sampleRate = 44100
        val durationMs = 1500
        val sampleCount = sampleRate * durationMs / 1000
        val samples = ShortArray(sampleCount)
        val frequency = 880.0
        for (i in samples.indices) {
            val envelope = when {
                i < sampleRate * 20 / 1000 -> i.toDouble() / (sampleRate * 20 / 1000)
                i >= sampleCount - sampleRate * 30 / 1000 -> (sampleCount - i).toDouble() / (sampleRate * 30 / 1000)
                else -> 1.0
            }.coerceIn(0.0, 1.0)
            samples[i] = (sin(2.0 * PI * frequency * i / sampleRate) * 0.65 * Short.MAX_VALUE * envelope).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        android.os.Handler(context.mainLooper).postDelayed({
            runCatching { audioTrack.stop() }
            audioTrack.release()
        }, durationMs + 100L)
    }

    private fun startPrayerAudioService(context: Context, resourceName: String) {
        val audioIntent = Intent(context, PrayerAudioService::class.java).apply {
            putExtra(PrayerAudioService.EXTRA_AUDIO_NAME, resourceName)
        }
        runCatching {
            context.startForegroundService(audioIntent)
        }
    }
}
