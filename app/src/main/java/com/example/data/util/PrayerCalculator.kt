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
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Astronomical prayer-time engine.
 *
 * KEMENAG_INDONESIA uses Subuh -20°, Isya -18°, Asar standard (shadow factor 1),
 * with Indonesian ihtiyat/rounding conventions. The Gregorian correction is
 * applied in the Julian-date calculation before the solar ephemeris is evaluated.
 *
 * This is an independent calculation engine. It is not a network connection to
 * Kemenag and is not labelled as an official Kemenag table until validated against
 * the relevant local Kemenag schedule.
 */
object PrayerCalculator {
    private const val KEMENAG_FAJR_ANGLE = -20.0
    private const val KEMENAG_ISHA_ANGLE = -18.0
    private const val DEFAULT_APPARENT_HORIZON_DEGREES = 0.8333

    // Kemenag/SIHAT-style published schedule convention: calculated times are
    // rounded up to the next minute, with special treatment for sunrise.
    private const val KEMENAG_SUBUH_IHTIYAT_MINUTES = 2
    private const val KEMENAG_DHUHR_IHTIYAT_MINUTES = 3
    private const val KEMENAG_ASHAR_IHTIYAT_MINUTES = 2
    private const val KEMENAG_MAGHRIB_IHTIYAT_MINUTES = 2
    private const val KEMENAG_ISYA_IHTIYAT_MINUTES = 2
    private const val KEMENAG_SUNRISE_OFFSET_MINUTES = -2

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
        // Gregorian correction B is essential; omitting it shifts the solar
        // ephemeris by roughly 13 days and materially breaks prayer times.
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun apparentHorizonAltitude(elevationMeters: Double): Double {
        val safeElevation = elevationMeters.coerceAtLeast(0.0)
        val dipDegrees = 0.0347 * sqrt(safeElevation)
        return -(DEFAULT_APPARENT_HORIZON_DEGREES + dipDegrees)
    }

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
        customOffsets: Map<PrayerType, Int> = emptyMap(),
        elevationMeters: Double = 0.0
    ): PrayerCalculationResult {
        val jd = julianDate(date.year, date.monthValue, date.dayOfMonth)
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(d2r(g)) + 0.020 * sin(d2r(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val delta = r2d(asin(sin(d2r(e)) * sin(d2r(l))))
        val ra = fixAngle(r2d(atan2(cos(d2r(e)) * sin(d2r(l)), cos(d2r(l))))) / 15.0
        val eqt = (q / 15.0) - ra
        val noon = fixHour(12.0 + timezoneOffsetHours - longitude / 15.0 - eqt)

        fun hourAngle(altitude: Double): Double {
            val cosH = (sin(d2r(altitude)) - sin(d2r(latitude)) * sin(d2r(delta))) /
                (cos(d2r(latitude)) * cos(d2r(delta)))
            return r2d(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
        }

        val isKemenag = method == PrayerCalculationMethod.KEMENAG_INDONESIA
        val fajrAngle = if (isKemenag) KEMENAG_FAJR_ANGLE else method.fajrAngleDegrees
        val ishaAngle = if (isKemenag) KEMENAG_ISHA_ANGLE else method.ishaAngleDegrees
        val sunriseAltitude = apparentHorizonAltitude(if (isKemenag) elevationMeters else 0.0)

        val fajrH = hourAngle(fajrAngle)
        val sunriseH = hourAngle(sunriseAltitude)
        val shadowFactor = if (isKemenag) AsrJuristicMethod.STANDARD.shadowFactor else asrJuristicMethod.shadowFactor
        val asrAlt = r2d(atan(1.0 / (shadowFactor + tan(d2r(kotlin.math.abs(latitude - delta))))))
        val asrH = hourAngle(asrAlt)
        val ishaH = hourAngle(ishaAngle)

        val fajrRaw = noon - fajrH
        val sunriseRaw = noon - sunriseH
        val dhuhrRaw = noon
        val asrRaw = noon + asrH
        val sunsetRaw = noon + sunriseH
        val maghribRaw = sunsetRaw
        val ishaRaw = noon + ishaH
        val imsakRaw = fajrRaw - imsakIntervalMinutes / 60.0

        fun formatTime(rawHours: Double, prayerType: PrayerType): Pair<String, LocalTime> {
            val userOffset = customOffsets[prayerType] ?: 0
            val totalSecondsExact = fixHour(rawHours) * 3600.0 + userOffset * 60.0

            // Kemenag published schedules distinguish Terbit from prayer times:
            // seconds are discarded for Terbit, then 2 minutes are subtracted.
            // Prayer times use minute rounding upward before their ihtiyat.
            val baseMinutes = if (isKemenag && prayerType == PrayerType.TERBIT) {
                floor(totalSecondsExact / 60.0).toInt()
            } else {
                when (roundingMode) {
                    PrayerRoundingMode.NONE -> (totalSecondsExact / 60.0).toInt()
                    PrayerRoundingMode.FLOOR -> floor(totalSecondsExact / 60.0).toInt()
                    PrayerRoundingMode.NEAREST -> Math.round(totalSecondsExact / 60.0).toInt()
                    PrayerRoundingMode.CEIL -> kotlin.math.ceil(totalSecondsExact / 60.0).toInt()
                }
            }

            val ihtiyat = if (isKemenag) {
                when (prayerType) {
                    PrayerType.IMSAK, PrayerType.SUBUH -> KEMENAG_SUBUH_IHTIYAT_MINUTES
                    PrayerType.DZUHUR -> KEMENAG_DHUHR_IHTIYAT_MINUTES
                    PrayerType.ASHAR -> KEMENAG_ASHAR_IHTIYAT_MINUTES
                    PrayerType.MAGHRIB -> KEMENAG_MAGHRIB_IHTIYAT_MINUTES
                    PrayerType.ISYA -> KEMENAG_ISYA_IHTIYAT_MINUTES
                    PrayerType.TERBIT -> KEMENAG_SUNRISE_OFFSET_MINUTES
                }
            } else {
                0
            }

            val totalMinutesRounded = baseMinutes + ihtiyat
            val finalHour = ((totalMinutesRounded / 60) % 24 + 24) % 24
            val finalMinute = ((totalMinutesRounded % 60) + 60) % 60
            return String.format(Locale.US, "%02d:%02d", finalHour, finalMinute) to LocalTime.of(finalHour, finalMinute)
        }

        val imsak = formatTime(imsakRaw, PrayerType.IMSAK)
        val fajr = formatTime(fajrRaw, PrayerType.SUBUH)
        val sunrise = formatTime(sunriseRaw, PrayerType.TERBIT)
        val dhuhr = formatTime(dhuhrRaw, PrayerType.DZUHUR)
        val asr = formatTime(asrRaw, PrayerType.ASHAR)
        val maghrib = formatTime(maghribRaw, PrayerType.MAGHRIB)
        val isha = formatTime(ishaRaw, PrayerType.ISYA)

        return PrayerCalculationResult(
            date = date,
            latitude = latitude,
            longitude = longitude,
            timezone = timezoneId,
            calculationMethod = method,
            asrJuristicMethod = if (isKemenag) AsrJuristicMethod.STANDARD else asrJuristicMethod,
            imsakIntervalMinutes = imsakIntervalMinutes,
            imsak = imsak.first,
            fajr = fajr.first,
            sunrise = sunrise.first,
            dhuhr = dhuhr.first,
            asr = asr.first,
            maghrib = maghrib.first,
            isha = isha.first,
            solarNoonHours = noon,
            solarDeclinationDegrees = delta,
            equationOfTimeMinutes = eqt * 60.0,
            rawTimes = mapOf(
                PrayerType.IMSAK to imsak.second,
                PrayerType.SUBUH to fajr.second,
                PrayerType.TERBIT to sunrise.second,
                PrayerType.DZUHUR to dhuhr.second,
                PrayerType.ASHAR to asr.second,
                PrayerType.MAGHRIB to maghrib.second,
                PrayerType.ISYA to isha.second
            ),
            isOfficialVerifiedKemenag = false
        )
    }

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
        asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
        elevationMeters: Double = 0.0
    ): List<PrayerScheduleItem> {
        val localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        val mappedMethod = when (method) {
            CalculationMethod.KEMENAG -> PrayerCalculationMethod.KEMENAG_INDONESIA
            CalculationMethod.MWL -> PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE
            CalculationMethod.EGYPT -> PrayerCalculationMethod.EGYPTIAN_AUTHORITY
            CalculationMethod.UMM_AL_QURA -> PrayerCalculationMethod.UMM_AL_QURA
        }
        val result = calculate(
            date = localDate,
            latitude = latitude,
            longitude = longitude,
            timezoneOffsetHours = timezoneOffset,
            method = mappedMethod,
            asrJuristicMethod = asrJuristicMethod,
            customOffsets = mapOf(
                PrayerType.IMSAK to imsakOffset,
                PrayerType.SUBUH to subuhOffset,
                PrayerType.DZUHUR to dzuhurOffset,
                PrayerType.ASHAR to asharOffset,
                PrayerType.MAGHRIB to maghribOffset,
                PrayerType.ISYA to isyaOffset
            ),
            elevationMeters = elevationMeters
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
