package com.example.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(DefaultPrayerNotificationManager.EXTRA_PRAYER_NAME)
            ?: "Salat"
        val prayerTime = intent.getStringExtra(DefaultPrayerNotificationManager.EXTRA_PRAYER_TIME)
            ?: ""

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            DefaultPrayerNotificationManager.CHANNEL_PRAYER_ALERT
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktu $prayerName")
            .setContentText("Sudah masuk waktu salat $prayerName${if (prayerTime.isNotBlank()) " • $prayerTime" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .setVibrate(longArrayOf(0, 400, 250, 400))
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            "prayer_${prayerName}_${System.currentTimeMillis()}".hashCode(),
            notification
        )
    }
}
