package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.PrayerType
import com.example.domain.model.TodaySchedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val schedule: TodaySchedule) : HomeUiState
}

class HomeViewModel(
    prayerRepository: PrayerRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = settingsRepository.settingsState
        .flatMapLatest { settings ->
            val offsets = mapOf(
                PrayerType.IMSAK to settings.imsakOffsetMinutes,
                PrayerType.SUBUH to settings.subuhOffsetMinutes,
                PrayerType.DZUHUR to settings.dzuhurOffsetMinutes,
                PrayerType.ASHAR to settings.asharOffsetMinutes,
                PrayerType.MAGHRIB to settings.maghribOffsetMinutes,
                PrayerType.ISYA to settings.isyaOffsetMinutes
            )
            prayerRepository.getTodaySchedule(
                cityName = settings.cityName,
                latitude = settings.latitude,
                longitude = settings.longitude,
                method = settings.calculationMethod,
                offsets = offsets
            )
        }
        .combine(settingsRepository.settingsState) { schedule, _ ->
            HomeUiState.Success(schedule) as HomeUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )
}
