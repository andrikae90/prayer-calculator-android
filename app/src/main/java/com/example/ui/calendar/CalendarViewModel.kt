package com.example.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CalendarRepository
import com.example.domain.model.CalendarDay
import com.example.domain.model.HijriDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class MonthYear(
    val year: Int,
    val monthIndex: Int, // 0-11
    val monthName: String
)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val masehiMonthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val now = Calendar.getInstance()
    private val _currentMonthYear = MutableStateFlow(
        MonthYear(
            year = now.get(Calendar.YEAR),
            monthIndex = now.get(Calendar.MONTH),
            monthName = masehiMonthNames[now.get(Calendar.MONTH)]
        )
    )
    val currentMonthYear: StateFlow<MonthYear> = _currentMonthYear.asStateFlow()

    val todayHijri: HijriDate = calendarRepository.getTodayHijriDate()

    val daysInMonth: StateFlow<List<CalendarDay>> = _currentMonthYear
        .flatMapLatest { my ->
            calendarRepository.getMonthDays(my.year, my.monthIndex)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun nextMonth() {
        val current = _currentMonthYear.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.year)
            set(Calendar.MONTH, current.monthIndex)
            add(Calendar.MONTH, 1)
        }
        _currentMonthYear.value = MonthYear(
            year = cal.get(Calendar.YEAR),
            monthIndex = cal.get(Calendar.MONTH),
            monthName = masehiMonthNames[cal.get(Calendar.MONTH)]
        )
    }

    fun previousMonth() {
        val current = _currentMonthYear.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.year)
            set(Calendar.MONTH, current.monthIndex)
            add(Calendar.MONTH, -1)
        }
        _currentMonthYear.value = MonthYear(
            year = cal.get(Calendar.YEAR),
            monthIndex = cal.get(Calendar.MONTH),
            monthName = masehiMonthNames[cal.get(Calendar.MONTH)]
        )
    }
}
