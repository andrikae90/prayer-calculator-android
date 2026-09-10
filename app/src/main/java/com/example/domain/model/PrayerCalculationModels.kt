package com.example.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Calculation methods for astronomical prayer times.
 *
 * NOTE: The KEMENAG_INDONESIA configuration utilizes the solar angles (-20° Fajr, -18° Isha)
 * and ihtiyat safety margin (+2 minutes) adopted by the Ministry of Religious Affairs (Kemenag RI).
 * However, this is an algorithmic calculation engine and should NOT be considered identical to the
 * official Kemenag Badan Hisab Rukyat (BHR) published tables until tested and verified against official data.
 */
enum class PrayerCalculationMethod(
    val id: String,
    val title: String,
    val fajrAngleDegrees: Double,
    val ishaAngleDegrees: Double,
    val description: String
) {
    KEMENAG_INDONESIA(
        id = "KEMENAG_INDONESIA",
        title = "Kemenag RI (Parameter Hisab)",
        fajrAngleDegrees = -20.0,
        ishaAngleDegrees = -18.0,
        description = "Sudut Subuh -20.0°, Isya -18.0° dengan standar ikhtiyat Kementerian Agama RI (parameter hisab astronomis)"
    ),
    MUSLIM_WORLD_LEAGUE(
        id = "MWL",
        title = "Muslim World League (MWL)",
        fajrAngleDegrees = -18.0,
        ishaAngleDegrees = -17.0,
        description = "Liga Muslim Dunia (Sudut Subuh -18.0°, Isya -17.0°)"
    ),
    EGYPTIAN_AUTHORITY(
        id = "EGYPT",
        title = "Egyptian General Authority of Survey",
        fajrAngleDegrees = -19.5,
        ishaAngleDegrees = -17.5,
        description = "Otoritas Mesir (Sudut Subuh -19.5°, Isya -17.5°)"
    ),
    UMM_AL_QURA(
        id = "UMM_AL_QURA",
        title = "Umm Al-Qura University, Makkah",
        fajrAngleDegrees = -18.5,
        ishaAngleDegrees = -19.0,
        description = "Universitas Umm Al-Qura (Sudut Subuh -18.5°, Isya -19.0°)"
    )
}

/**
 * Juristic method for Asr prayer calculation.
 * Standard (Shafi'i, Maliki, Hanbali) uses shadow factor = 1.0 (object height + noon shadow).
 * Hanafi uses shadow factor = 2.0 (2x object height + noon shadow).
 */
enum class AsrJuristicMethod(
    val shadowFactor: Double,
    val displayName: String,
    val description: String
) {
    STANDARD(
        shadowFactor = 1.0,
        displayName = "Syafi'i, Maliki, Hanbali (1x Bayangan)",
        description = "Waktu Ashar dimulai saat panjang bayangan sama dengan tinggi benda ditambah panjang bayangan waktu zawal"
    ),
    HANAFI(
        shadowFactor = 2.0,
        displayName = "Hanafi (2x Bayangan)",
        description = "Waktu Ashar dimulai saat panjang bayangan dua kali tinggi benda ditambah panjang bayangan waktu zawal"
    )
}

/**
 * Minute rounding strategy for calculated prayer times.
 */
enum class PrayerRoundingMode(val displayName: String) {
    NONE("Tanpa Pembulatan (Desimal)"),
    FLOOR("Pembulatan ke Bawah (Truncate)"),
    NEAREST("Pembulatan Terdekat (Standard Round)"),
    CEIL("Pembulatan ke Atas (Ceiling - Direkomendasikan Fikih)")
}

/**
 * Structured container for prayer calculation results and parameters,
 * as required by technical audit specifications.
 */
data class PrayerCalculationResult(
    val date: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val calculationMethod: PrayerCalculationMethod,
    val asrJuristicMethod: AsrJuristicMethod,
    val imsakIntervalMinutes: Int,
    val imsak: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val solarNoonHours: Double = 0.0,
    val solarDeclinationDegrees: Double = 0.0,
    val equationOfTimeMinutes: Double = 0.0,
    val rawTimes: Map<PrayerType, LocalTime> = emptyMap(),
    val isOfficialVerifiedKemenag: Boolean = false
)
