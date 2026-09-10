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
 * KEMENAG_INDONESIA follows the currently documented Indonesian Kemenag parameter
 * profile used in local Kemenag schedules: Subuh -20°, Isya -18°, Asar standard
 * (shadow factor 1), and an explicit 2-minute ihtiyat for the prayer-time output.
 * Sunrise/Maghrib use the apparent solar altitude with semidiameter/refraction and
 * horizon dip derived from observer elevation.
 *
 * This is an independent implementation of the Kemenag parameter/method profile;
 * it is NOT a network connection to Kemenag and is not marked as an official
 * Kemenag-published table until results are validated against the relevant local
 * Kemenag schedule.
 */
object PrayerCalculator {

    private const val KEMENAG_FAJR_ANGLE = -20.0
    private const val KEMENAG_ISHA_ANGLE = -18.0
    private const val DEFAULT_APPARENT_HORIZON_DEGREES = 0.8333
    private const val KEMENAG_IHTIYAT_MINUTES = 2

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
     * Observer-horizon altitude used for sunrise/sunset.
     * Kemenag schedules account for semidiameter + atmospheric refraction + dip of
     * the visible horizon. Dip is approximated from observer elevation in metres.
     */
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
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        // Solar coordinates using the compact astronomical ephemeris approximation.
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(d2r(g)) + 0.020 * sin(d2r(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val delta = r2d(asin(sin(d2r(e)) * sin(d2r(l))))
        val ra = fixAngle(r2d(atan2(cos(d2r(e)) * sin(d2r(l)), cos(d2r(l))))) / 15.0
        val eqt = (q / 15.0) - ra
        val noon = fixHour(12.0 + timezoneOffsetHours - (longitude / 15.0) - eqt)

        fun hourAngle(altitude: Double): Double {
            val cosH = (sin(d2r(altitude)) - sin(d2r(latitude)) * sin(d2r(delta))) /
                (cos(d2r(latitude)) * cos(d2r(delta)))
            return r2d(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
        }

        val isKemenag = method == PrayerCalculationMethod.KEMENAG_INDONESIA
        val fajrAngle = if (isKemenag) KEMENAG_FAJR_ANGLE else method.fajrAngleDegrees
        val ishaAngle = if (isKemenag) KEMENAG_ISHA_ANGLE else method.ishaAngleDegrees
        val effectiveIhtiyat = if (isKemenag) KEMENAG_IHTIYAT_MINUTES else ihtiyatMinutes
        val sunriseAltitude = apparentHorizonAltitude(if (isKemenag) elevationMeters else 0.0)

        val fajrH = hourAngle(fajrAngle)
        val sunriseH = hourAngle(sunriseAltitude)

        val shadowFactor = if (isKemenag) AsrJuristicMethod.STANDARD.shadowFactor else asrJuristicMethod.shadowFactor
        val asrAlt = r2d(atan(1.0 / (shadowFactor + tan(d2r(kotlin.math.abs(latitude - delta))))))
        val asrH = hourAngle(asrAlt)
        val ishaH = hourAngle(ishaAngle)

        val fajrRaw = noon - fajrH
        val sunriseRaw = noon - sunriseH
        val dhuhrRaw = noon + (effectiveIhtiyat / 60.0)
        val asrRaw = noon + asrH
        val sunsetRaw = noon + sunriseH
        val maghribRaw = sunsetRaw + (effectiveIhtiyat / 60.0)
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
            return formatted to LocalTime.of(finalHour, finalMinute)
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
            asrJuristicMethod = if (isKemenag) AsrJuristicMethod.STANDARD else asrJuristicMethod,
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
            customOffsets = offsets,
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
