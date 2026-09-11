package com.example.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
            PrayerNotificationSound.BIP_PANJANG -> startPrayerAudioService(context, "beep_panjang")
            PrayerNotificationSound.ADZAN_LENGKAP -> {
                val audioName = if (prayerName.equals("Subuh", ignoreCase = true)) "adzan_subuh" else "adzan_lengkap"
                startPrayerAudioService(context, audioName)
            }
            PrayerNotificationSound.TAKBIR_SAJA -> startPrayerAudioService(context, "takbir_saja")
            PrayerNotificationSound.GETAR_SAJA,
            PrayerNotificationSound.TANPA_NOTIFIKASI -> Unit
        }
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
