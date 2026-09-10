package com.example.data.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain.model.PrayerNotificationSound
import com.example.domain.model.PrayerScheduleItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface PrayerNotificationManager {
    fun initializeChannels()
    fun schedulePrayerReminder(prayerItem: PrayerScheduleItem, sound: PrayerNotificationSound = PrayerNotificationSound.BIP_PANJANG)
    fun cancelAllReminders()
    fun isNotificationPermissionGranted(): Boolean
}

class DefaultPrayerNotificationManager(private val context: Context) : PrayerNotificationManager {

    companion object {
        const val CHANNEL_PRAYER_ALERT = "channel_prayer_alert"
        const val CHANNEL_NAME = "Pengingat Jadwal Salat"
        const val CHANNEL_DESC = "Notifikasi waktu masuk salat lima waktu"
        const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_NOTIFICATION_SOUND = "extra_notification_sound"
        private const val PREFS = "prayer_notifications"
        private const val KEY_IDS = "scheduled_ids"
    }

    override fun initializeChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel(CHANNEL_PRAYER_ALERT, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun schedulePrayerReminder(prayerItem: PrayerScheduleItem, sound: PrayerNotificationSound) {
        if (!isNotificationPermissionGranted() || sound == PrayerNotificationSound.TANPA_NOTIFIKASI) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prayerItem.hour)
            set(Calendar.MINUTE, prayerItem.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val requestCode = buildRequestCode(prayerItem, target)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerItem.type.displayName)
            putExtra(EXTRA_PRAYER_TIME, prayerItem.time)
            putExtra(EXTRA_NOTIFICATION_SOUND, sound.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
        }
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().toMutableSet()
        ids.add(requestCode.toString())
        prefs.edit().putStringSet(KEY_IDS, ids).apply()
    }

    override fun cancelAllReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty()
        ids.forEach { value ->
            value.toIntOrNull()?.let { requestCode ->
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply { action = ACTION_PRAYER_ALARM }
                val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                if (pendingIntent != null) alarmManager.cancel(pendingIntent)
            }
        }
        prefs.edit().remove(KEY_IDS).apply()
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun buildRequestCode(prayerItem: PrayerScheduleItem, target: Calendar): Int {
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(target.timeInMillis))
        return (dateKey.hashCode() * 31 + prayerItem.type.ordinal).absoluteValue
    }
}

private val Int.absoluteValue: Int
    get() = if (this == Int.MIN_VALUE) Int.MAX_VALUE else kotlin.math.abs(this)
