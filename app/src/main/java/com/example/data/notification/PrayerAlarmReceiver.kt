package com.example.data.notification

import android.media.AudioManager
import android.media.ToneGenerator
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

        if (sound == PrayerNotificationSound.BIP_PANJANG) {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
            android.os.Handler(context.mainLooper).postDelayed({ tone.release() }, 1300)
        }
        // ADZAN_LENGKAP and TAKBIR_SAJA intentionally remain silent until
        // licensed audio assets are added to res/raw.
    }
}
