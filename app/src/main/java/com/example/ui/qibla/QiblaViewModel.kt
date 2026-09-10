package com.example.ui.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import com.example.data.sensor.CompassSensorManager
import com.example.data.sensor.CompassState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QiblaViewModel(
    private val compassSensorManager: CompassSensorManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val compassState: StateFlow<CompassState> = compassSensorManager.compassState

    init {
        // Initialize coordinates from user settings (e.g. Jakarta -6.2088, 106.8456)
        viewModelScope.launch {
            settingsRepository.settingsState.collect { settings ->
                compassSensorManager.updateCoordinates(settings.latitude, settings.longitude)
            }
        }
    }

    fun startListening() {
        compassSensorManager.startListening()
    }

    fun stopListening() {
        compassSensorManager.stopListening()
    }

    fun onLocationUpdated(lat: Double, lng: Double, cityName: String) {
        settingsRepository.updateSettings { current ->
            current.copy(latitude = lat, longitude = lng, cityName = cityName)
        }
        compassSensorManager.updateCoordinates(lat, lng)
    }

    override fun onCleared() {
        super.onCleared()
        compassSensorManager.stopListening()
    }
}
