package com.example.data.util

import com.example.data.hijri.HIJRI_MONTH_NAMES
import com.example.data.hijri.HijriCalendarProvider
import com.example.data.hijri.IndonesianMabimsEstimatedProvider
import com.example.domain.model.HijriDate
import java.time.LocalDate
import java.util.Calendar

/**
 * Facade for Hijri date conversions.
 * Delegates to configurable [HijriCalendarProvider] to strictly separate
 * arithmetic conversions, astronomical estimations, and official government data.
 */
object HijriDateCalculator {

    val hijriMonths = HIJRI_MONTH_NAMES

    private var activeProvider: HijriCalendarProvider = IndonesianMabimsEstimatedProvider()

    fun setProvider(provider: HijriCalendarProvider) {
        activeProvider = provider
    }

    fun getActiveProvider(): HijriCalendarProvider = activeProvider

    fun getHijriDate(date: LocalDate): HijriDate {
        return activeProvider.getHijriDate(date)
    }

    fun getHijriDate(cal: Calendar = Calendar.getInstance()): HijriDate {
        val localDate = LocalDate.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return getHijriDate(localDate)
    }

    fun formatHijriDate(date: LocalDate): String {
        return activeProvider.formatHijriDate(date)
    }

    fun formatHijriDate(cal: Calendar = Calendar.getInstance()): String {
        val localDate = LocalDate.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return formatHijriDate(localDate)
    }
}
