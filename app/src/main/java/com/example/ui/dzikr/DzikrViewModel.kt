package com.example.ui.dzikr

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.DzikrRepository
import com.example.domain.model.DzikrItem
import com.example.domain.model.DzikrType
import com.example.domain.model.TasbihState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class DzikrTab(val displayName: String) {
    TASBIH("Tasbih Digital"),
    PAGI("Dzikir Pagi"),
    PETANG("Dzikir Petang"),
    SETELAH_SALAT("Setelah Salat")
}

class DzikrViewModel(
    private val dzikrRepository: DzikrRepository,
    private val appContext: Context
) : ViewModel() {

    private val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val tasbihPrefs = appContext.getSharedPreferences("tasbih_state", Context.MODE_PRIVATE)

    private val _selectedTab = MutableStateFlow(DzikrTab.TASBIH)
    val selectedTab: StateFlow<DzikrTab> = _selectedTab.asStateFlow()

    private val _customPhrases = MutableStateFlow(loadCustomPhrases())
    val customPhrases: StateFlow<List<String>> = _customPhrases.asStateFlow()

    private val _tasbihState = MutableStateFlow(
        TasbihState(
            count = tasbihPrefs.getInt(KEY_COUNT, 0),
            target = tasbihPrefs.getInt(KEY_TARGET, 33),
            totalRounds = tasbihPrefs.getInt(KEY_TOTAL_ROUNDS, 0),
            isVibrationEnabled = tasbihPrefs.getBoolean(KEY_VIBRATION, true),
            isSoundEnabled = tasbihPrefs.getBoolean(KEY_SOUND, false)
        )
    )
    val tasbihState: StateFlow<TasbihState> = _tasbihState.asStateFlow()

    val currentDzikrList: StateFlow<List<DzikrItem>> = _selectedTab
        .flatMapLatest { tab ->
            val type = when (tab) {
                DzikrTab.PAGI -> DzikrType.PAGI
                DzikrTab.PETANG -> DzikrType.PETANG
                DzikrTab.SETELAH_SALAT -> DzikrType.SETELAH_SALAT
                DzikrTab.TASBIH -> DzikrType.SETELAH_SALAT
            }
            dzikrRepository.getDzikrList(type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tab: DzikrTab) {
        _selectedTab.value = tab
    }

    fun addCustomPhrase(phrase: String) {
        val cleaned = phrase.trim()
        if (cleaned.isEmpty()) return
        val current = _customPhrases.value
        if (cleaned in current) return
        val updated = current + cleaned
        _customPhrases.value = updated
        saveCustomPhrases(updated)
    }

    fun incrementTasbih() {
        val current = _tasbihState.value
        val newCount = current.count + 1
        val targetReached = newCount >= current.target

        if (targetReached) {
            triggerVibration(long = true)
            _tasbihState.update { it.copy(count = 0, totalRounds = it.totalRounds + 1) }
        } else {
            triggerVibration(long = false)
            _tasbihState.update { it.copy(count = newCount) }
        }
        saveTasbihState()
    }

    fun resetTasbih() {
        _tasbihState.update { it.copy(count = 0) }
        saveTasbihState()
    }

    fun setTasbihTarget(newTarget: Int) {
        if (newTarget < 1) return
        _tasbihState.update { it.copy(target = newTarget, count = 0) }
        saveTasbihState()
    }

    fun toggleVibration() {
        _tasbihState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
        saveTasbihState()
    }

    private fun loadCustomPhrases(): List<String> {
        return tasbihPrefs.getStringSet(KEY_CUSTOM_PHRASES, emptySet())
            ?.toList()
            ?.sorted()
            ?: emptyList()
    }

    private fun saveCustomPhrases(phrases: List<String>) {
        tasbihPrefs.edit().putStringSet(KEY_CUSTOM_PHRASES, phrases.toSet()).apply()
    }

    private fun saveTasbihState() {
        val state = _tasbihState.value
        tasbihPrefs.edit()
            .putInt(KEY_COUNT, state.count)
            .putInt(KEY_TARGET, state.target)
            .putInt(KEY_TOTAL_ROUNDS, state.totalRounds)
            .putBoolean(KEY_VIBRATION, state.isVibrationEnabled)
            .putBoolean(KEY_SOUND, state.isSoundEnabled)
            .apply()
    }

    private fun triggerVibration(long: Boolean) {
        if (!_tasbihState.value.isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val duration = if (long) 80L else 30L
                val amplitude = if (long) VibrationEffect.DEFAULT_AMPLITUDE else 120
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (long) 80L else 30L)
            }
        } catch (_: Exception) {}
    }

    private companion object {
        const val KEY_COUNT = "count"
        const val KEY_TARGET = "target"
        const val KEY_TOTAL_ROUNDS = "total_rounds"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_CUSTOM_PHRASES = "custom_phrases"
    }
}
