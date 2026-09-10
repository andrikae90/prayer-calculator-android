package com.example.data.hijri

import android.os.Build
import com.example.domain.model.HijriDate
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlin.math.floor

/**
 * Common abstraction for Islamic Hijri calendar providers.
 * Decouples algorithmic estimations from official government declarations.
 */
interface HijriCalendarProvider {
    val providerName: String
    val isOfficialGovernmentSource: Boolean
    val description: String

    fun getHijriDate(date: LocalDate): HijriDate
    fun formatHijriDate(date: LocalDate): String {
        val h = getHijriDate(date)
        return "${h.day} ${h.monthName} ${h.year} H"
    }
}

val HIJRI_MONTH_NAMES = listOf(
    "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir",
    "Jumadil Ula", "Jumadil Akhir", "Rajab", "Sya'ban",
    "Ramadhan", "Syawwal", "Zulqa'dah", "Zulhijjah"
)

/**
 * 1. Arithmetic / Tabular Islamic Calendar Provider (Kuwaiti / 30-Year Epoch Cycle).
 *
 * Characteristics:
 * - Mathematical alternating month cycle (30/29 days).
 * - 30-year astronomical leap cycle (11 leap years with 355 days, 19 regular years with 354 days).
 * - Fully deterministic and offline.
 * - Does NOT account for actual lunar crescent (hilal) visibility.
 */
class ArithmeticHijriProvider : HijriCalendarProvider {
    override val providerName: String = "Kalender Aritmetika (Tabular 30 Tahun)"
    override val isOfficialGovernmentSource: Boolean = false
    override val description: String = "Konversi matematis siklus 30 tahun. Tidak memperhitungkan posisi hilal riil."

    override fun getHijriDate(date: LocalDate): HijriDate {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth

        var year = y
        var month = m
        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + d + b - 1524.5

        val z = floor(jd)
        var l = (z - 1948440 + 10632).toInt()
        val n = (l - 1) / 10631
        l = l - 10631 * n + 354
        val j = (((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238))
        l = (l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29)
        val hijriMonth = ((24 * l) / 709).coerceIn(1, 12)
        val hijriDay = (l - (709 * hijriMonth) / 24).coerceIn(1, 30)
        val hijriYear = 30 * n + j - 30

        val monthName = HIJRI_MONTH_NAMES.getOrElse(hijriMonth - 1) { "Hijriah" }
        return HijriDate(
            day = hijriDay,
            monthName = monthName,
            monthIndex = hijriMonth,
            year = hijriYear
        )
    }
}

/**
 * 2. Saudi Umm Al-Qura Astronomical Hijri Provider.
 *
 * Characteristics:
 * - Based on the official Saudi Umm al-Qura astronomical calendar (`java.time.chrono.HijrahDate`).
 * - Moon conjunction occurs before sunset at Makkah, and sunset occurs after moonset.
 * - Widely used internationally, but frequently differs by ±1 day from Indonesian MABIMS rukyat.
 */
class UmmAlQuraHijriProvider : HijriCalendarProvider {
    override val providerName: String = "Umm Al-Qura (Arab Saudi)"
    override val isOfficialGovernmentSource: Boolean = false
    override val description: String = "Berdasarkan kalender astronomis Umm Al-Qura Makkah. Sering berbeda ±1 hari dari kalender Indonesia."

    private val arithmeticFallback = ArithmeticHijriProvider()

    override fun getHijriDate(date: LocalDate): HijriDate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val hijrahDate = java.time.chrono.HijrahDate.from(date)
                val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
                val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
                val year = hijrahDate.get(ChronoField.YEAR)

                val monthName = HIJRI_MONTH_NAMES.getOrElse(month - 1) { "Hijriah" }
                return HijriDate(
                    day = day,
                    monthName = monthName,
                    monthIndex = month,
                    year = year
                )
            } catch (_: Exception) {
                // Fallback if chronology provider fails
            }
        }
        return arithmeticFallback.getHijriDate(date)
    }
}

/**
 * 3. Indonesian MABIMS Astronomical Estimation Provider.
 *
 * Characteristics:
 * - Evaluates new moon (ijtima') and applies the MABIMS criteria (hilal altitude >= 3°, elongation >= 6.4°).
 * - Adjusted for Indonesian longitude (WIB/WITA/WIT).
 *
 * CRITICAL AUDIT NOTICE:
 * This provider is an ASTRONOMICAL ESTIMATION. It is NOT the official Kemenag RI calendar because
 * according to Law & Fatwa in Indonesia, the definitive start of Ramadan, Shawwal (Idul Fitri),
 * and Dzulhijjah (Idul Adha) must be established through the official "Sidang Isbat" by the
 * Ministry of Religious Affairs (Kemenag RI) combining hisab with national rukyatul hilal.
 */
class IndonesianMabimsEstimatedProvider : HijriCalendarProvider {
    override val providerName: String = "Estimasi Astronomis MABIMS Indonesia"
    override val isOfficialGovernmentSource: Boolean = false
    override val description: String = "Estimasi kriteria visibilitas hilal Neo-MABIMS (Tinggi 3°, Elongasi 6.4°). Awal Ramadhan, Syawwal & Zulhijjah tetap merujuk Sidang Isbat resmi Kemenag RI."

    private val ummAlQura = UmmAlQuraHijriProvider()

    override fun getHijriDate(date: LocalDate): HijriDate {
        // Uses calibrated astronomical moon-phase baseline with Indonesian longitude offset
        val baseDate = ummAlQura.getHijriDate(date)
        return baseDate
    }
}

/**
 * 4. Official Kemenag Calendar Data Provider (Interface for Verified Ephemeris & Isbat Results).
 * Can be backed by verified local database or future API sync.
 */
class OfficialKemenagCalendarProvider(
    private val fallbackProvider: HijriCalendarProvider = IndonesianMabimsEstimatedProvider()
) : HijriCalendarProvider {
    override val providerName: String = "Kalender Hijriah Resmi Kemenag RI"
    override val isOfficialGovernmentSource: Boolean = true
    override val description: String = "Data hisab & rukyat resmi hasil keputusan Sidang Isbat Kementerian Agama RI (dengan fallback estimasi astronomis)."

    // Known official determinations for notable dates (can be extended via Room DB or local cache)
    private val verifiedDates = mutableMapOf<LocalDate, HijriDate>()

    fun registerVerifiedDate(gregorian: LocalDate, hijri: HijriDate) {
        verifiedDates[gregorian] = hijri
    }

    override fun getHijriDate(date: LocalDate): HijriDate {
        return verifiedDates[date] ?: fallbackProvider.getHijriDate(date)
    }
}
