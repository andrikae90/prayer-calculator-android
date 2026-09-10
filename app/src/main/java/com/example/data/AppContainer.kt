package com.example.data

import android.content.Context
import com.example.data.location.DefaultLocationProvider
import com.example.data.location.LocationProvider
import com.example.data.notification.DefaultPrayerNotificationManager
import com.example.data.notification.PrayerNotificationManager
import com.example.data.repository.CalendarRepository
import com.example.data.repository.DailyReminderRepository
import com.example.data.repository.DefaultCalendarRepository
import com.example.data.repository.DefaultDailyReminderRepository
import com.example.data.repository.DefaultPrayerRepository
import com.example.data.repository.DefaultSettingsRepository
import com.example.data.repository.DuaRepository
import com.example.data.repository.DzikrRepository
import com.example.data.repository.LocalDuaRepository
import com.example.data.repository.LocalDzikrRepository
import com.example.data.repository.LocalQuranRepository
import com.example.data.repository.PrayerRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.data.sensor.CompassSensorManager
import com.example.data.util.AppClock
import com.example.data.util.SystemAppClock

interface AppContainer {
    val appClock: AppClock
    val locationProvider: LocationProvider
    val prayerRepository: PrayerRepository
    val quranRepository: QuranRepository
    val duaRepository: DuaRepository
    val dzikrRepository: DzikrRepository
    val calendarRepository: CalendarRepository
    val settingsRepository: SettingsRepository
    val dailyReminderRepository: DailyReminderRepository
    val notificationManager: PrayerNotificationManager
    fun provideCompassSensorManager(): CompassSensorManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val appClock: AppClock by lazy { SystemAppClock() }
    override val locationProvider: LocationProvider by lazy { DefaultLocationProvider(context) }
    override val dailyReminderRepository: DailyReminderRepository by lazy { DefaultDailyReminderRepository() }
    override val prayerRepository: PrayerRepository by lazy {
        DefaultPrayerRepository(
            dailyReminderRepository = dailyReminderRepository,
            appClock = appClock,
            locationProvider = locationProvider
        )
    }
    override val quranRepository: QuranRepository by lazy { LocalQuranRepository() }
    override val duaRepository: DuaRepository by lazy { LocalDuaRepository() }
    override val dzikrRepository: DzikrRepository by lazy { LocalDzikrRepository() }
    override val calendarRepository: CalendarRepository by lazy { DefaultCalendarRepository() }
    override val settingsRepository: SettingsRepository by lazy { DefaultSettingsRepository() }
    override val notificationManager: PrayerNotificationManager by lazy {
        DefaultPrayerNotificationManager(context).apply { initializeChannels() }
    }

    override fun provideCompassSensorManager(): CompassSensorManager {
        return CompassSensorManager(context)
    }
}
