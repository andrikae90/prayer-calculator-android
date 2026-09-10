package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.CalculationMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

interface SettingsRepository {
    val settingsState: StateFlow<AppSettings>
    fun updateSettings(transform: (AppSettings) -> AppSettings)
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val cityName = stringPreferencesKey("city_name")
        val latitude = doublePreferencesKey("latitude")
        val longitude = doublePreferencesKey("longitude")
        val elevationMeters = doublePreferencesKey("elevation_meters")
        val calculationMethod = stringPreferencesKey("calculation_method")
        val imsakOffset = intPreferencesKey("imsak_offset")
        val subuhOffset = intPreferencesKey("subuh_offset")
        val dzuhurOffset = intPreferencesKey("dzuhur_offset")
        val asharOffset = intPreferencesKey("ashar_offset")
        val maghribOffset = intPreferencesKey("maghrib_offset")
        val isyaOffset = intPreferencesKey("isya_offset")
        val notificationEnabled = booleanPreferencesKey("notification_enabled")
        val adzanSoundEnabled = booleanPreferencesKey("adzan_sound_enabled")
        val selectedAdzanVoice = stringPreferencesKey("selected_adzan_voice")
        val themeSetting = stringPreferencesKey("theme_setting")
        val appLanguage = stringPreferencesKey("app_language")
    }

    private val settingsFlow: Flow<AppSettings> = appContext.settingsDataStore.data.map { p ->
        AppSettings(
            cityName = p[Keys.cityName] ?: AppSettings().cityName,
            latitude = p[Keys.latitude] ?: AppSettings().latitude,
            longitude = p[Keys.longitude] ?: AppSettings().longitude,
            elevationMeters = p[Keys.elevationMeters] ?: AppSettings().elevationMeters,
            calculationMethod = p[Keys.calculationMethod]
                ?.let { runCatching { CalculationMethod.valueOf(it) }.getOrNull() }
                ?: AppSettings().calculationMethod,
            imsakOffsetMinutes = p[Keys.imsakOffset] ?: 0,
            subuhOffsetMinutes = p[Keys.subuhOffset] ?: 0,
            dzuhurOffsetMinutes = p[Keys.dzuhurOffset] ?: 0,
            asharOffsetMinutes = p[Keys.asharOffset] ?: 0,
            maghribOffsetMinutes = p[Keys.maghribOffset] ?: 0,
            isyaOffsetMinutes = p[Keys.isyaOffset] ?: 0,
            prayerNotificationEnabled = p[Keys.notificationEnabled] ?: true,
            adzanSoundEnabled = p[Keys.adzanSoundEnabled] ?: true,
            selectedAdzanVoice = p[Keys.selectedAdzanVoice] ?: "Adzan Makkah",
            themeSetting = p[Keys.themeSetting]
                ?.let { runCatching { AppThemeSetting.valueOf(it) }.getOrNull() }
                ?: AppThemeSetting.DARK,
            appLanguage = p[Keys.appLanguage] ?: "Bahasa Indonesia"
        )
    }

    override val settingsState: StateFlow<AppSettings> = settingsFlow.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    override fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(settingsState.value)
        scope.launch {
            appContext.settingsDataStore.edit { p ->
                p[Keys.cityName] = updated.cityName
                p[Keys.latitude] = updated.latitude
                p[Keys.longitude] = updated.longitude
                p[Keys.elevationMeters] = updated.elevationMeters
                p[Keys.calculationMethod] = updated.calculationMethod.name
                p[Keys.imsakOffset] = updated.imsakOffsetMinutes
                p[Keys.subuhOffset] = updated.subuhOffsetMinutes
                p[Keys.dzuhurOffset] = updated.dzuhurOffsetMinutes
                p[Keys.asharOffset] = updated.asharOffsetMinutes
                p[Keys.maghribOffset] = updated.maghribOffsetMinutes
                p[Keys.isyaOffset] = updated.isyaOffsetMinutes
                p[Keys.notificationEnabled] = updated.prayerNotificationEnabled
                p[Keys.adzanSoundEnabled] = updated.adzanSoundEnabled
                p[Keys.selectedAdzanVoice] = updated.selectedAdzanVoice
                p[Keys.themeSetting] = updated.themeSetting.name
                p[Keys.appLanguage] = updated.appLanguage
            }
        }
    }
}
