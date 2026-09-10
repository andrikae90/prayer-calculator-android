package com.example.data.repository

import com.example.data.util.HijriDateCalculator
import com.example.domain.model.CalendarDay
import com.example.domain.model.HijriDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface CalendarRepository {
    fun getMonthDays(calendarYear: Int, calendarMonthZeroIndexed: Int): Flow<List<CalendarDay>>
    fun getTodayHijriDate(): HijriDate
}

class DefaultCalendarRepository : CalendarRepository {

    override fun getTodayHijriDate(): HijriDate {
        val now = Calendar.getInstance()
        return calculateHijriForDate(now)
    }

    override fun getMonthDays(calendarYear: Int, calendarMonthZeroIndexed: Int): Flow<List<CalendarDay>> = flow {
        val daysList = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calendarYear)
        cal.set(Calendar.MONTH, calendarMonthZeroIndexed)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayCal = Calendar.getInstance()
        val isCurrentYearMonth = todayCal.get(Calendar.YEAR) == calendarYear &&
                todayCal.get(Calendar.MONTH) == calendarMonthZeroIndexed

        val dayNameFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val dateMasehiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val isToday = isCurrentYearMonth && todayCal.get(Calendar.DAY_OF_MONTH) == day
            val hijri = calculateHijriForDate(cal)

            val events = mutableListOf<String>()
            // Sunnah fastings
            if (hijri.day in 13..15) {
                events.add("Puasa Sunnah Ayyamul Bidh (${hijri.day} ${hijri.monthName})")
            }
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                events.add("Sunnah Puasa Senin-Kamis")
            }

            daysList.add(
                CalendarDay(
                    dateMasehi = dateMasehiFormat.format(cal.time),
                    dayOfMonthMasehi = day,
                    dayOfWeekName = dayNameFormat.format(cal.time),
                    hijriDate = hijri,
                    isToday = isToday,
                    specialEvents = events
                )
            )
        }

        emit(daysList)
    }

    private fun calculateHijriForDate(cal: Calendar): HijriDate {
        return HijriDateCalculator.getHijriDate(cal)
    }
}
