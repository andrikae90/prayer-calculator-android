package com.example.data.util

import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.PrayerCalculationMethod
import com.example.domain.model.PrayerCalculationResult
import com.example.domain.model.PrayerRoundingMode
import com.example.domain.model.PrayerScheduleItem
import com.example.domain.model.PrayerType
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Technical Astronomical Prayer Time Calculation Engine.
 *
 * ==============================================================================
 * 1. FORMULA & METHODOLOGY SPECIFICATION
 * ==============================================================================
 * This engine calculates solar prayer times using celestial mechanics algorithms:
 *
 * A. Julian Day (JD) & Astronomical Epoch:
 *    JD = floor(365.25 * (Y + 4716)) + floor(30.6001 * (M + 1)) + D + B - 1524.5
 *    where B = 2 - A + floor(A / 4) for Gregorian calendar (A = floor(Y / 100)).
 *    d = JD - 2451545.0 (Days elapsed since J2000.0 epoch).
 *
 * B. Low-Precision Solar Coordinates:
 *    - Solar Mean Anomaly (g): g = 357.529° + 0.98560028° * d
 *    - Solar Mean Longitude (q): q = 280.459° + 0.98564736° * d
 *    - Ecliptic Longitude (L): L = q + 1.915° * sin(g) + 0.020° * sin(2*g)
 *    - Obliquity of the Ecliptic (e): e = 23.439° - 0.00000036° * d
 *    - Solar Declination (δ): δ = arcsin(sin(e) * sin(L))
 *    - Right Ascension (RA): α = atan2(cos(e) * sin(L), cos(L)) / 15.0 (in hours)
 *    - Equation of Time (EqT): EqT = (q / 15.0) - α (in hours)
 *
 * C. Solar Transit (Solar Noon / Zawal):
 *    Noon = 12.0 + timezoneOffset - (longitude / 15.0) - EqT
 *
 * D. Hour Angle Equation:
 *    cos(H) = (sin(altitude) - sin(latitude) * sin(δ)) / (cos(latitude) * cos(δ))
 *    H = arccos(clamp(cos(H), -1, 1)) / 15.0 (in hours)
 *
 * E. Altitudes used:
 *    - Sunrise / Terbit: -0.8333° (atmospheric refraction 34' + sun semidiameter 16')
 *    - Sunset: -0.8333°
 *    - Subuh / Fajr: Method-specific (Kemenag: -20.0°)
 *    - Isya / Isha: Method-specific (Kemenag: -18.0°)
 *    - Ashar: Altitude where shadow = noon shadow + N * object height
 *      alt_asr = -atan(N + tan(|latitude - δ|)) where N = shadow factor (1.0 standard, 2.0 Hanafi)
 *    - Imsak: fajrHours - (imsakIntervalMinutes / 60.0)
 *
 * ==============================================================================
 * 2. LIMITATIONS & WHY THIS IS NOT IDENTICAL TO OFFICIAL KEMENAG RI TABLES
 * ==============================================================================
 * This class uses standard astronomical hisab formulas. While configured with Kemenag
 * parameters (-20° Fajr, -18° Isha), it must NOT be claimed as an official Kemenag RI
 * synchronization because:
 * 1. Kemenag RI's Badan Hisab Rukyat (BHR) uses high-precision Ephemeris (VSOP87/ELP2000
 *    or Nautical Almanac), not low-precision solar approximation.
 * 2. BHR accounts for observer altitude above sea level (irtifa' / dip of horizon).
 * 3. Kemenag applies regional calibration and rounds with localized ihtiyat (+2 to +3 minutes).
 * 4. Official schedule requires verification against local Kanwil Kemenag tables.
 */
object PrayerCalculator {

    private fun d2r(d: Double): Double = d * Math.PI / 180.0
    private fun r2d(r: Double): Double = r * 180.0 / Math.PI

    private fun fixHour(a: Double): Double {
        var res = a - 24.0 * floor(a / 24.0)
        if (res < 0) res += 24.0
        return res
    }

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Computes full prayer calculation result with rigorous astronomical telemetry.
     */
    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        timezoneId: String = "Asia/Jakarta",
        method: PrayerCalculationMethod = PrayerCalculationMethod.KEMENAG_INDONESIA,
        asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
        imsakIntervalMinutes: Int = 10,
        ihtiyatMinutes: Int = 2,
        roundingMode: PrayerRoundingMode = PrayerRoundingMode.CEIL,
        customOffsets: Map<PrayerType, Int> = emptyMap()
    ): PrayerCalculationResult {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        // Celestial coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(d2r(g)) + 0.020 * sin(d2r(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val sinDelta = sin(d2r(e)) * sin(d2r(l))
        val delta = r2d(asin(sinDelta))

        val ra = fixAngle(r2d(atan2(cos(d2r(e)) * sin(d2r(l)), cos(d2r(l))))) / 15.0
        val eqt = (q / 15.0) - ra

        // Solar noon (transit) in hours
        val noon = fixHour(12.0 + timezoneOffsetHours - (longitude / 15.0) - eqt)

        fun hourAngle(altitude: Double): Double {
            val cosH = (sin(d2r(altitude)) - sin(d2r(latitude)) * sin(d2r(delta))) /
                    (cos(d2r(latitude)) * cos(d2r(delta)))
            val clamped = cosH.coerceIn(-1.0, 1.0)
            return r2d(acos(clamped)) / 15.0
        }

        // Angles
        val fajrH = hourAngle(method.fajrAngleDegrees)
        val sunriseH = hourAngle(-0.8333)

        // Asr angle with configurable shadow factor (1.0 vs 2.0)
        // FiQH & Astronomical formula: cot(alt) = shadowFactor + tan(|latitude - delta|)
        // Therefore: alt = arctan(1.0 / (shadowFactor + tan(|latitude - delta|)))
        val shadowFactor = asrJuristicMethod.shadowFactor
        val asrAlt = r2d(atan(1.0 / (shadowFactor + tan(d2r(kotlin.math.abs(latitude - delta))))))
        val asrH = hourAngle(asrAlt)

        val ishaH = hourAngle(method.ishaAngleDegrees)

        // Raw astronomical times in hours
        val fajrRaw = noon - fajrH
        val sunriseRaw = noon - sunriseH
        val dhuhrRaw = noon + (ihtiyatMinutes / 60.0) // 2 min ihtiyat after transit
        val asrRaw = noon + asrH
        val sunsetRaw = noon + sunriseH
        val maghribRaw = sunsetRaw + (ihtiyatMinutes / 60.0) // Sunset + ihtiyat
        val ishaRaw = noon + ishaH
        val imsakRaw = fajrRaw - (imsakIntervalMinutes / 60.0)

        fun formatTime(rawHours: Double, prayerType: PrayerType): Pair<String, LocalTime> {
            val userOffset = customOffsets[prayerType] ?: 0
            val normalizedHours = fixHour(rawHours)
            val totalSecondsExact = normalizedHours * 3600.0 + (userOffset * 60.0)

            val totalMinutesRounded = when (roundingMode) {
                PrayerRoundingMode.NONE -> (totalSecondsExact / 60.0).toInt()
                PrayerRoundingMode.FLOOR -> floor(totalSecondsExact / 60.0).toInt()
                PrayerRoundingMode.NEAREST -> Math.round(totalSecondsExact / 60.0).toInt()
                PrayerRoundingMode.CEIL -> kotlin.math.ceil(totalSecondsExact / 60.0).toInt()
            }

            val finalHour = ((totalMinutesRounded / 60) % 24 + 24) % 24
            val finalMinute = ((totalMinutesRounded % 60) + 60) % 60
            val formatted = String.format(Locale.US, "%02d:%02d", finalHour, finalMinute)
            val localTime = LocalTime.of(finalHour, finalMinute)
            return Pair(formatted, localTime)
        }

        val imsakFormatted = formatTime(imsakRaw, PrayerType.IMSAK)
        val fajrFormatted = formatTime(fajrRaw, PrayerType.SUBUH)
        val sunriseFormatted = formatTime(sunriseRaw, PrayerType.TERBIT)
        val dhuhrFormatted = formatTime(dhuhrRaw, PrayerType.DZUHUR)
        val asrFormatted = formatTime(asrRaw, PrayerType.ASHAR)
        val maghribFormatted = formatTime(maghribRaw, PrayerType.MAGHRIB)
        val ishaFormatted = formatTime(ishaRaw, PrayerType.ISYA)

        val rawTimesMap = mapOf(
            PrayerType.IMSAK to imsakFormatted.second,
            PrayerType.SUBUH to fajrFormatted.second,
            PrayerType.TERBIT to sunriseFormatted.second,
            PrayerType.DZUHUR to dhuhrFormatted.second,
            PrayerType.ASHAR to asrFormatted.second,
            PrayerType.MAGHRIB to maghribFormatted.second,
            PrayerType.ISYA to ishaFormatted.second
        )

        return PrayerCalculationResult(
            date = date,
            latitude = latitude,
            longitude = longitude,
            timezone = timezoneId,
            calculationMethod = method,
            asrJuristicMethod = asrJuristicMethod,
            imsakIntervalMinutes = imsakIntervalMinutes,
            imsak = imsakFormatted.first,
            fajr = fajrFormatted.first,
            sunrise = sunriseFormatted.first,
            dhuhr = dhuhrFormatted.first,
            asr = asrFormatted.first,
            maghrib = maghribFormatted.first,
            isha = ishaFormatted.first,
            solarNoonHours = noon,
            solarDeclinationDegrees = delta,
            equationOfTimeMinutes = eqt * 60.0,
            rawTimes = rawTimesMap,
            isOfficialVerifiedKemenag = false // Explicitly audited: algorithmic approximation
        )
    }

    /**
     * Backward-compatible helper for UI consumers returning List<PrayerScheduleItem>.
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double = 7.0,
        method: CalculationMethod = CalculationMethod.KEMENAG,
        imsakOffset: Int = 0,
        subuhOffset: Int = 0,
        dzuhurOffset: Int = 0,
        asharOffset: Int = 0,
        maghribOffset: Int = 0,
        isyaOffset: Int = 0,
        asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD
    ): List<PrayerScheduleItem> {
        val localDate = LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val mappedMethod = when (method) {
            CalculationMethod.KEMENAG -> PrayerCalculationMethod.KEMENAG_INDONESIA
            CalculationMethod.MWL -> PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE
            CalculationMethod.EGYPT -> PrayerCalculationMethod.EGYPTIAN_AUTHORITY
            CalculationMethod.UMM_AL_QURA -> PrayerCalculationMethod.UMM_AL_QURA
        }

        val offsets = mapOf(
            PrayerType.IMSAK to imsakOffset,
            PrayerType.SUBUH to subuhOffset,
            PrayerType.DZUHUR to dzuhurOffset,
            PrayerType.ASHAR to asharOffset,
            PrayerType.MAGHRIB to maghribOffset,
            PrayerType.ISYA to isyaOffset
        )

        val result = calculate(
            date = localDate,
            latitude = latitude,
            longitude = longitude,
            timezoneOffsetHours = timezoneOffset,
            method = mappedMethod,
            asrJuristicMethod = asrJuristicMethod,
            customOffsets = offsets
        )

        return listOf(
            PrayerScheduleItem(PrayerType.IMSAK, result.imsak, result.rawTimes[PrayerType.IMSAK]!!.hour, result.rawTimes[PrayerType.IMSAK]!!.minute),
            PrayerScheduleItem(PrayerType.SUBUH, result.fajr, result.rawTimes[PrayerType.SUBUH]!!.hour, result.rawTimes[PrayerType.SUBUH]!!.minute),
            PrayerScheduleItem(PrayerType.TERBIT, result.sunrise, result.rawTimes[PrayerType.TERBIT]!!.hour, result.rawTimes[PrayerType.TERBIT]!!.minute),
            PrayerScheduleItem(PrayerType.DZUHUR, result.dhuhr, result.rawTimes[PrayerType.DZUHUR]!!.hour, result.rawTimes[PrayerType.DZUHUR]!!.minute),
            PrayerScheduleItem(PrayerType.ASHAR, result.asr, result.rawTimes[PrayerType.ASHAR]!!.hour, result.rawTimes[PrayerType.ASHAR]!!.minute),
            PrayerScheduleItem(PrayerType.MAGHRIB, result.maghrib, result.rawTimes[PrayerType.MAGHRIB]!!.hour, result.rawTimes[PrayerType.MAGHRIB]!!.minute),
            PrayerScheduleItem(PrayerType.ISYA, result.isha, result.rawTimes[PrayerType.ISYA]!!.hour, result.rawTimes[PrayerType.ISYA]!!.minute)
        )
    }
}
