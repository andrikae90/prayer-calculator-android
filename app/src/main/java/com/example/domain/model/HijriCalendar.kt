package com.example.domain.model

data class HijriDate(
    val day: Int,
    val monthName: String,
    val monthIndex: Int, // 1-12
    val year: Int
) {
    fun formatDisplay(): String = "$day $monthName $year H"
}

data class CalendarDay(
    val dateMasehi: String, // YYYY-MM-DD
    val dayOfMonthMasehi: Int,
    val dayOfWeekName: String,
    val hijriDate: HijriDate,
    val isToday: Boolean = false,
    val specialEvents: List<String> = emptyList() // e.g. "Puasa Ayyamul Bidh", "Tahun Baru Hijriah"
)
