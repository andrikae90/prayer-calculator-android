package com.example.domain.model

enum class PrayerType(val displayName: String) {
    IMSAK("Imsak"),
    SUBUH("Subuh"),
    TERBIT("Terbit"),
    DZUHUR("Dzuhur"),
    ASHAR("Ashar"),
    MAGHRIB("Maghrib"),
    ISYA("Isya")
}

data class PrayerScheduleItem(
    val type: PrayerType,
    val time: String, // HH:mm
    val hour: Int,
    val minute: Int,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)

data class TodaySchedule(
    val locationName: String,
    val dateMasehiFormatted: String,
    val dateHijriFormatted: String,
    val prayers: List<PrayerScheduleItem>,
    val nextPrayer: PrayerScheduleItem?,
    val countdownText: String, // e.g., "01:24:12"
    val dailyReminder: DailyReminder? = null
)
