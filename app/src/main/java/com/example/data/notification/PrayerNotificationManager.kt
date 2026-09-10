package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.domain.model.PrayerScheduleItem

interface PrayerNotificationManager {
    fun initializeChannels()
    fun schedulePrayerReminder(prayerItem: PrayerScheduleItem)
    fun cancelAllReminders()
    fun isNotificationPermissionGranted(): Boolean
}

class DefaultPrayerNotificationManager(private val context: Context) : PrayerNotificationManager {

    companion object {
        const val CHANNEL_PRAYER_ALERT = "channel_prayer_alert"
        const val CHANNEL_NAME = "Pengingat Jadwal Salat"
        const val CHANNEL_DESC = "Notifikasi waktu masuk salat lima waktu"
    }

    override fun initializeChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel(
                CHANNEL_PRAYER_ALERT,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun schedulePrayerReminder(prayerItem: PrayerScheduleItem) {
        // Architecture placeholder ready for AlarmManager & WorkManager in Phase 2
    }

    override fun cancelAllReminders() {
        // Architecture placeholder for cancelling scheduled alarms
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
