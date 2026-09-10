package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.CalculationMethod
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateCity(city: String) {
        settingsRepository.updateSettings { it.copy(cityName = city) }
    }

    fun updateCalculationMethod(method: CalculationMethod) {
        settingsRepository.updateSettings { it.copy(calculationMethod = method) }
    }

    fun updateTheme(theme: AppThemeSetting) {
        settingsRepository.updateSettings { it.copy(themeSetting = theme) }
    }

    fun togglePrayerNotification(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(prayerNotificationEnabled = enabled) }
    }

    fun toggleAdzanSound(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(adzanSoundEnabled = enabled) }
    }

    fun selectAdzanVoice(voice: String) {
        settingsRepository.updateSettings { it.copy(selectedAdzanVoice = voice) }
    }

    fun updateSubuhOffset(offsetMinutes: Int) {
        settingsRepository.updateSettings { it.copy(subuhOffsetMinutes = offsetMinutes) }
    }

    fun updateDzuhurOffset(offsetMinutes: Int) {
        settingsRepository.updateSettings { it.copy(dzuhurOffsetMinutes = offsetMinutes) }
    }

    fun updateAsharOffset(offsetMinutes: Int) {
        settingsRepository.updateSettings { it.copy(asharOffsetMinutes = offsetMinutes) }
    }

    fun updateMaghribOffset(offsetMinutes: Int) {
        settingsRepository.updateSettings { it.copy(maghribOffsetMinutes = offsetMinutes) }
    }

    fun updateIsyaOffset(offsetMinutes: Int) {
        settingsRepository.updateSettings { it.copy(isyaOffsetMinutes = offsetMinutes) }
    }
}
