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

    private val _selectedTab = MutableStateFlow(DzikrTab.TASBIH)
    val selectedTab: StateFlow<DzikrTab> = _selectedTab.asStateFlow()

    private val _tasbihState = MutableStateFlow(TasbihState(count = 0, target = 33))
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

    fun incrementTasbih() {
        val current = _tasbihState.value
        val newCount = current.count + 1
        val targetReached = newCount >= current.target

        if (targetReached) {
            triggerVibration(long = true)
            _tasbihState.update {
                it.copy(
                    count = 0,
                    totalRounds = it.totalRounds + 1
                )
            }
        } else {
            triggerVibration(long = false)
            _tasbihState.update {
                it.copy(count = newCount)
            }
        }
    }

    fun resetTasbih() {
        _tasbihState.update { it.copy(count = 0) }
    }

    fun setTasbihTarget(newTarget: Int) {
        _tasbihState.update { it.copy(target = newTarget, count = 0) }
    }

    fun toggleVibration() {
        _tasbihState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
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
}
