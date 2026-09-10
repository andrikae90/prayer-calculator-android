package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.LocationProvider
import com.example.data.location.UserLocation
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AppSettings
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.CalculationMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settingsState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())
    private val _locationResults = MutableStateFlow<List<UserLocation>>(emptyList())
    val locationResults: StateFlow<List<UserLocation>> = _locationResults
    private val _isSearchingLocation = MutableStateFlow(false)
    val isSearchingLocation: StateFlow<Boolean> = _isSearchingLocation

    fun updateCity(city: String) { settingsRepository.updateSettings { it.copy(cityName = city) } }

    fun selectLocation(location: UserLocation) {
        locationProvider.setManualLocation(location)
        settingsRepository.updateSettings { it.copy(cityName = location.cityName, latitude = location.latitude, longitude = location.longitude) }
        _locationResults.value = emptyList()
    }

    fun useGpsLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation() ?: return@launch
            locationProvider.setManualLocation(location)
            settingsRepository.updateSettings { it.copy(cityName = location.cityName, latitude = location.latitude, longitude = location.longitude) }
        }
    }

    fun searchLocations(query: String) {
        if (query.trim().length < 3) { _locationResults.value = emptyList(); return }
        viewModelScope.launch {
            _isSearchingLocation.value = true
            _locationResults.value = locationProvider.searchLocations(query)
            _isSearchingLocation.value = false
        }
    }
    fun clearLocationResults() { _locationResults.value = emptyList() }
    fun updateCalculationMethod(method: CalculationMethod) { settingsRepository.updateSettings { it.copy(calculationMethod = method) } }
    fun updateTheme(theme: AppThemeSetting) { settingsRepository.updateSettings { it.copy(themeSetting = theme) } }
    fun togglePrayerNotification(enabled: Boolean) { settingsRepository.updateSettings { it.copy(prayerNotificationEnabled = enabled) } }
    fun toggleAdzanSound(enabled: Boolean) { settingsRepository.updateSettings { it.copy(adzanSoundEnabled = enabled) } }
    fun selectAdzanVoice(voice: String) { settingsRepository.updateSettings { it.copy(selectedAdzanVoice = voice) } }
    fun updateSubuhOffset(offsetMinutes: Int) { settingsRepository.updateSettings { it.copy(subuhOffsetMinutes = offsetMinutes) } }
    fun updateDzuhurOffset(offsetMinutes: Int) { settingsRepository.updateSettings { it.copy(dzuhurOffsetMinutes = offsetMinutes) } }
    fun updateAsharOffset(offsetMinutes: Int) { settingsRepository.updateSettings { it.copy(asharOffsetMinutes = offsetMinutes) } }
    fun updateMaghribOffset(offsetMinutes: Int) { settingsRepository.updateSettings { it.copy(maghribOffsetMinutes = offsetMinutes) } }
    fun updateIsyaOffset(offsetMinutes: Int) { settingsRepository.updateSettings { it.copy(isyaOffsetMinutes = offsetMinutes) } }
}
