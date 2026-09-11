package com.example.data.repository

import com.example.data.location.LocationProvider
import com.example.data.util.AppClock
import com.example.data.util.HijriDateCalculator
import com.example.data.util.PrayerCalculator
import com.example.data.util.SystemAppClock
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.CalculationMethod
import com.example.domain.model.PrayerCalculationMethod
import com.example.domain.model.PrayerCalculationResult
import com.example.domain.model.PrayerScheduleItem
import com.example.domain.model.PrayerType
import com.example.domain.model.TodaySchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

interface PrayerRepository {
    fun getTodaySchedule(
        cityName: String = "Jakarta, Indonesia",
        latitude: Double = -6.2088,
        longitude: Double = 106.8456,
        method: CalculationMethod = CalculationMethod.KEMENAG,
        offsets: Map<PrayerType, Int> = emptyMap(),
        zoneId: ZoneId? = null,
        timezoneOffsetHours: Double? = null,
        asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
        elevationMeters: Double = 0.0
    ): Flow<TodaySchedule>

    fun calculateResultForDate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        timezoneId: String = "Asia/Jakarta",
        method: CalculationMethod = CalculationMethod.KEMENAG,
        asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD,
        offsets: Map<PrayerType, Int> = emptyMap(),
        elevationMeters: Double = 0.0
    ): PrayerCalculationResult
}

class DefaultPrayerRepository(
    private val dailyReminderRepository: DailyReminderRepository = DefaultDailyReminderRepository(),
    private val appClock: AppClock = SystemAppClock(),
    private val locationProvider: LocationProvider? = null
) : PrayerRepository {

    override fun calculateResultForDate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timezoneOffsetHours: Double,
        timezoneId: String,
        method: CalculationMethod,
        asrJuristicMethod: AsrJuristicMethod,
        offsets: Map<PrayerType, Int>,
        elevationMeters: Double
    ): PrayerCalculationResult {
        val mappedMethod = when (method) {
            CalculationMethod.KEMENAG -> PrayerCalculationMethod.KEMENAG_INDONESIA
            CalculationMethod.MWL -> PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE
            CalculationMethod.EGYPT -> PrayerCalculationMethod.EGYPTIAN_AUTHORITY
            CalculationMethod.UMM_AL_QURA -> PrayerCalculationMethod.UMM_AL_QURA
        }

        return PrayerCalculator.calculate(
            date = date,
            latitude = latitude,
            longitude = longitude,
            timezoneOffsetHours = timezoneOffsetHours,
            timezoneId = timezoneId,
            method = mappedMethod,
            asrJuristicMethod = asrJuristicMethod,
            customOffsets = offsets,
            elevationMeters = elevationMeters
        )
    }

    override fun getTodaySchedule(
        cityName: String,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        offsets: Map<PrayerType, Int>,
        zoneId: ZoneId?,
        timezoneOffsetHours: Double?,
        asrJuristicMethod: AsrJuristicMethod,
        elevationMeters: Double
    ): Flow<TodaySchedule> = flow {
        val resolvedOffset = timezoneOffsetHours ?: resolveIndonesianTimezoneOffset(longitude)
        val resolvedZoneId = zoneId ?: resolveIndonesianZoneId(longitude)

        while (true) {
            val now = appClock.now(resolvedZoneId)
            val currentDate = now.toLocalDate()
            val currentTimeInSeconds = now.hour * 3600 + now.minute * 60 + now.second

            val todayResult = calculateResultForDate(
                date = currentDate,
                latitude = latitude,
                longitude = longitude,
                timezoneOffsetHours = resolvedOffset,
                timezoneId = resolvedZoneId.id,
                method = method,
                asrJuristicMethod = asrJuristicMethod,
                offsets = offsets,
                elevationMeters = elevationMeters
            )

            val scheduleData = listOf(
                PrayerScheduleItem(PrayerType.IMSAK, todayResult.imsak, todayResult.rawTimes[PrayerType.IMSAK]!!.hour, todayResult.rawTimes[PrayerType.IMSAK]!!.minute),
                PrayerScheduleItem(PrayerType.SUBUH, todayResult.fajr, todayResult.rawTimes[PrayerType.SUBUH]!!.hour, todayResult.rawTimes[PrayerType.SUBUH]!!.minute),
                PrayerScheduleItem(PrayerType.TERBIT, todayResult.sunrise, todayResult.rawTimes[PrayerType.TERBIT]!!.hour, todayResult.rawTimes[PrayerType.TERBIT]!!.minute),
                PrayerScheduleItem(PrayerType.DZUHUR, todayResult.dhuhr, todayResult.rawTimes[PrayerType.DZUHUR]!!.hour, todayResult.rawTimes[PrayerType.DZUHUR]!!.minute),
                PrayerScheduleItem(PrayerType.ASHAR, todayResult.asr, todayResult.rawTimes[PrayerType.ASHAR]!!.hour, todayResult.rawTimes[PrayerType.ASHAR]!!.minute),
                PrayerScheduleItem(PrayerType.MAGHRIB, todayResult.maghrib, todayResult.rawTimes[PrayerType.MAGHRIB]!!.hour, todayResult.rawTimes[PrayerType.MAGHRIB]!!.minute),
                PrayerScheduleItem(PrayerType.ISYA, todayResult.isha, todayResult.rawTimes[PrayerType.ISYA]!!.hour, todayResult.rawTimes[PrayerType.ISYA]!!.minute)
            )

            // Next event follows the requested sequence:
            // SUBUH -> TERBIT -> DHUHUR -> ASHAR -> MAGHRIB -> ISYA -> SUBUH (tomorrow).
            // TERBIT is an event, not a fard prayer, but is intentionally shown between Subuh and Dhuhur.
            var nextFound = false
            var nextItem: PrayerScheduleItem? = null
            var minutesUntilNext = 0
            var secondsUntilNext = 0

            val mappedPrayers = scheduleData.map { item ->
                val itemTimeInSeconds = item.hour * 3600 + item.minute * 60
                val isPassed = currentTimeInSeconds >= itemTimeInSeconds
                val isNext = !isPassed && !nextFound

                if (isNext) {
                    nextFound = true
                    nextItem = item
                    val diffSeconds = itemTimeInSeconds - currentTimeInSeconds
                    minutesUntilNext = diffSeconds / 60
                    secondsUntilNext = diffSeconds % 60
                }

                item.copy(isNext = isNext, isPassed = isPassed)
            }

            if (!nextFound) {
                val tomorrowDate = currentDate.plusDays(1)
                val tomorrowResult = calculateResultForDate(
                    date = tomorrowDate,
                    latitude = latitude,
                    longitude = longitude,
                    timezoneOffsetHours = resolvedOffset,
                    timezoneId = resolvedZoneId.id,
                    method = method,
                    asrJuristicMethod = asrJuristicMethod,
                    offsets = offsets,
                    elevationMeters = elevationMeters
                )

                val tomorrowSubuhHour = tomorrowResult.rawTimes[PrayerType.SUBUH]!!.hour
                val tomorrowSubuhMinute = tomorrowResult.rawTimes[PrayerType.SUBUH]!!.minute
                val secondsLeftToday = (24 * 3600) - currentTimeInSeconds
                val secondsInTomorrow = tomorrowSubuhHour * 3600 + tomorrowSubuhMinute * 60
                val diffSeconds = secondsLeftToday + secondsInTomorrow

                minutesUntilNext = diffSeconds / 60
                secondsUntilNext = diffSeconds % 60
                nextItem = PrayerScheduleItem(
                    type = PrayerType.SUBUH,
                    time = tomorrowResult.fajr,
                    hour = tomorrowSubuhHour,
                    minute = tomorrowSubuhMinute,
                    isNext = true,
                    isPassed = false
                )
            }

            val countdownFormatted = if (nextItem != null) {
                val hours = minutesUntilNext / 60
                val mins = minutesUntilNext % 60
                String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secondsUntilNext)
            } else {
                "--:--:--"
            }

            // The hero card must show the upcoming event, not the last prayer that passed.
            val displayPrayer = nextItem

            val cal = Calendar.getInstance()
            cal.set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth)
            val dateFormatMasehi = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val dateMasehi = dateFormatMasehi.format(cal.time)
            val dateHijri = HijriDateCalculator.formatHijriDate(currentDate)
            val reminder = dailyReminderRepository.getTodayReminder()

            emit(
                TodaySchedule(
                    locationName = cityName,
                    dateMasehiFormatted = dateMasehi,
                    dateHijriFormatted = dateHijri,
                    prayers = mappedPrayers,
                    nextPrayer = displayPrayer,
                    countdownText = countdownFormatted,
                    dailyReminder = reminder
                )
            )

            delay(1000)
        }
    }

    private fun resolveIndonesianTimezoneOffset(longitude: Double): Double {
        return when {
            longitude >= 125.0 -> 9.0
            longitude >= 115.0 -> 8.0
            else -> 7.0
        }
    }

    private fun resolveIndonesianZoneId(longitude: Double): ZoneId {
        return when {
            longitude >= 125.0 -> ZoneId.of("Asia/Jayapura")
            longitude >= 115.0 -> ZoneId.of("Asia/Makassar")
            else -> ZoneId.of("Asia/Jakarta")
        }
    }
}
