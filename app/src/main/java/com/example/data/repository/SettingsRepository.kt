package com.example.data.repository

import com.example.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SettingsRepository {
    val settingsState: StateFlow<AppSettings>
    fun updateSettings(transform: (AppSettings) -> AppSettings)
}

class DefaultSettingsRepository : SettingsRepository {
    private val _settingsState = MutableStateFlow(AppSettings())
    override val settingsState: StateFlow<AppSettings> = _settingsState.asStateFlow()

    override fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _settingsState.update(transform)
    }
}
