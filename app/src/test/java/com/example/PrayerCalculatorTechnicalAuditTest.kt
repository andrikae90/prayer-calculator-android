package com.example

import com.example.data.hijri.ArithmeticHijriProvider
import com.example.data.hijri.IndonesianMabimsEstimatedProvider
import com.example.data.hijri.OfficialKemenagCalendarProvider
import com.example.data.hijri.UmmAlQuraHijriProvider
import com.example.data.repository.DefaultDailyReminderRepository
import com.example.data.repository.DefaultPrayerRepository
import com.example.data.util.PrayerCalculator
import com.example.data.util.TestAppClock
import com.example.domain.model.AsrJuristicMethod
import com.example.domain.model.HijriDate
import com.example.domain.model.PrayerCalculationMethod
import com.example.domain.model.PrayerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Technical Audit Automated Test Suite for PrayerCalculator and HijriDateCalculator.
 *
 * Tests 5 major Indonesian cities across multiple astronomical seasons (Equinox & Solstice),
 * verifies calculation parameters, Ashar shadow juristic methods, Imsak configurability,
 * timezones (WIB, WITA, WIT), and countdown rollover to next-day Subuh.
 */
class PrayerCalculatorTechnicalAuditTest {

    data class CityTestProfile(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val timezoneOffsetHours: Double,
        val timezoneId: String,
        val timezoneLabel: String
    )

    private val targetCities = listOf(
        CityTestProfile("Jakarta", -6.2088, 106.8456, 7.0, "Asia/Jakarta", "WIB (UTC+7)"),
        CityTestProfile("Surabaya", -7.2575, 112.7521, 7.0, "Asia/Jakarta", "WIB (UTC+7)"),
        CityTestProfile("Bandung", -6.9175, 107.6191, 7.0, "Asia/Jakarta", "WIB (UTC+7)"),
        CityTestProfile("Makassar", -5.1477, 119.4327, 8.0, "Asia/Makassar", "WITA (UTC+8)"),
        CityTestProfile("Jayapura", -2.5337, 140.7181, 9.0, "Asia/Jayapura", "WIT (UTC+9)")
    )

    private val testDates = listOf(
        LocalDate.of(2026, 3, 21),  // Vernal Equinox (Declination ~ 0°)
        LocalDate.of(2026, 6, 21),  // Summer Solstice (Declination ~ +23.44°)
        LocalDate.of(2026, 9, 23),  // Autumnal Equinox (Declination ~ 0°)
        LocalDate.of(2026, 12, 21)  // Winter Solstice (Declination ~ -23.44°)
    )

    @Test
    fun `test prayer times across 5 Indonesian cities on multiple astronomical dates`() {
        println("\n==================================================================================")
        println("AUDIT HASIL PERHITUNGAN PRAYER CALCULATOR (5 KOTA UTAMA INDONESIA)")
        println("Metode: PrayerCalculationMethod.KEMENAG_INDONESIA (Fajr -20°, Isha -18°)")
        println("Ashar: Standar Syafi'i/Maliki/Hanbali (Faktor Bayangan = 1.0)")
        println("Imsak: Parameter 10 Menit Sebelum Subuh")
        println("==================================================================================")

        for (city in targetCities) {
            println("\n----------------------------------------------------------------------------------")
            println("KOTA: ${city.name.uppercase()} (${city.latitude}°, ${city.longitude}°) - Zona: ${city.timezoneLabel}")
            println("----------------------------------------------------------------------------------")

            for (date in testDates) {
                val result = PrayerCalculator.calculate(
                    date = date,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    timezoneOffsetHours = city.timezoneOffsetHours,
                    timezoneId = city.timezoneId,
                    method = PrayerCalculationMethod.KEMENAG_INDONESIA,
                    asrJuristicMethod = AsrJuristicMethod.STANDARD,
                    imsakIntervalMinutes = 10
                )

                println(
                    String.format(
                        "Tanggal: %s | Imsak: %s | Subuh: %s | Terbit: %s | Dzuhur: %s | Ashar: %s | Maghrib: %s | Isya: %s",
                        date,
                        result.imsak,
                        result.fajr,
                        result.sunrise,
                        result.dhuhr,
                        result.asr,
                        result.maghrib,
                        result.isha
                    )
                )

                // Verify Chronological Sequence
                val imsakMin = result.rawTimes[PrayerType.IMSAK]!!.toSecondOfDay()
                val subuhMin = result.rawTimes[PrayerType.SUBUH]!!.toSecondOfDay()
                val terbitMin = result.rawTimes[PrayerType.TERBIT]!!.toSecondOfDay()
                val dzuhurMin = result.rawTimes[PrayerType.DZUHUR]!!.toSecondOfDay()
                val asharMin = result.rawTimes[PrayerType.ASHAR]!!.toSecondOfDay()
                val maghribMin = result.rawTimes[PrayerType.MAGHRIB]!!.toSecondOfDay()
                val isyaMin = result.rawTimes[PrayerType.ISYA]!!.toSecondOfDay()

                assertTrue("${city.name} [$date]: Imsak harus sebelum Subuh", imsakMin < subuhMin)
                assertTrue("${city.name} [$date]: Subuh harus sebelum Terbit", subuhMin < terbitMin)
                assertTrue("${city.name} [$date]: Terbit harus sebelum Dzuhur", terbitMin < dzuhurMin)
                assertTrue("${city.name} [$date]: Dzuhur harus sebelum Ashar", dzuhurMin < asharMin)
                assertTrue("${city.name} [$date]: Ashar harus sebelum Maghrib", asharMin < maghribMin)
                assertTrue("${city.name} [$date]: Maghrib harus sebelum Isya", maghribMin < isyaMin)

                // Verify telemetry data in PrayerCalculationResult
                assertFalse(result.isOfficialVerifiedKemenag)
                assertEquals(PrayerCalculationMethod.KEMENAG_INDONESIA, result.calculationMethod)
                assertEquals(city.timezoneId, result.timezone)
            }
        }
    }

    @Test
    fun `test asr shadow factor juristic methods - Hanafi must be later than Shafi'i`() {
        val date = LocalDate.of(2026, 4, 15)
        val lat = -6.2088 // Jakarta
        val lon = 106.8456

        val standardResult = PrayerCalculator.calculate(
            date = date,
            latitude = lat,
            longitude = lon,
            timezoneOffsetHours = 7.0,
            asrJuristicMethod = AsrJuristicMethod.STANDARD // 1x shadow
        )

        val hanafiResult = PrayerCalculator.calculate(
            date = date,
            latitude = lat,
            longitude = lon,
            timezoneOffsetHours = 7.0,
            asrJuristicMethod = AsrJuristicMethod.HANAFI // 2x shadow
        )

        val standardAsrSeconds = standardResult.rawTimes[PrayerType.ASHAR]!!.toSecondOfDay()
        val hanafiAsrSeconds = hanafiResult.rawTimes[PrayerType.ASHAR]!!.toSecondOfDay()

        println("\n=== AUDIT ASAR: PERBANDINGAN FAKTOR BAYANGAN ===")
        println("Jakarta, Tanggal: $date")
        println("Ashar Standar (Syafi'i/Maliki/Hanbali, 1x): ${standardResult.asr}")
        println("Ashar Hanafi (2x): ${hanafiResult.asr}")
        println("Selisih Waktu: ${(hanafiAsrSeconds - standardAsrSeconds) / 60} menit")

        assertTrue("Waktu Ashar Hanafi (2x bayangan) harus lebih lambat dari Standar (1x bayangan)", hanafiAsrSeconds > standardAsrSeconds)
    }

    @Test
    fun `test imsak as configurable parameter before subuh`() {
        val date = LocalDate.of(2026, 4, 15)
        val lat = -6.2088
        val lon = 106.8456

        val imsak10 = PrayerCalculator.calculate(
            date = date,
            latitude = lat,
            longitude = lon,
            timezoneOffsetHours = 7.0,
            imsakIntervalMinutes = 10
        )

        val imsak15 = PrayerCalculator.calculate(
            date = date,
            latitude = lat,
            longitude = lon,
            timezoneOffsetHours = 7.0,
            imsakIntervalMinutes = 15
        )

        val fajrSeconds = imsak10.rawTimes[PrayerType.SUBUH]!!.toSecondOfDay()
        val imsak10Seconds = imsak10.rawTimes[PrayerType.IMSAK]!!.toSecondOfDay()
        val imsak15Seconds = imsak15.rawTimes[PrayerType.IMSAK]!!.toSecondOfDay()

        println("\n=== AUDIT IMSAK: PARAMETER FLEKSIBEL ===")
        println("Subuh: ${imsak10.fajr}")
        println("Imsak (10 menit sebelum Subuh): ${imsak10.imsak} (selisih: ${(fajrSeconds - imsak10Seconds) / 60} menit)")
        println("Imsak (15 menit sebelum Subuh): ${imsak15.imsak} (selisih: ${(fajrSeconds - imsak15Seconds) / 60} menit)")

        assertEquals(10, (fajrSeconds - imsak10Seconds) / 60)
        assertEquals(15, (fajrSeconds - imsak15Seconds) / 60)
    }

    @Test
    fun `test countdown rollover to next day subuh when all prayers passed`() = runBlocking {
        // Mock clock to 22:30:00 WIB (after Isya ~19:15)
        val testDate = LocalDate.of(2026, 4, 15)
        val zoneId = ZoneId.of("Asia/Jakarta")
        val fixedZonedDateTime = ZonedDateTime.of(testDate, java.time.LocalTime.of(22, 30, 0), zoneId)
        val testClock = TestAppClock(fixedZonedDateTime.toInstant(), zoneId)

        val repository = DefaultPrayerRepository(
            dailyReminderRepository = DefaultDailyReminderRepository(),
            appClock = testClock
        )

        val schedule = repository.getTodaySchedule(
            cityName = "Jakarta",
            latitude = -6.2088,
            longitude = 106.8456,
            zoneId = zoneId,
            timezoneOffsetHours = 7.0
        ).first()

        println("\n=== AUDIT COUNTDOWN: EVENT ROLLOVER WAKTU MALAM ===")
        println("Waktu Perangkat: 22:30:00 WIB (Semua salat hari ini sudah lewat)")
        println("Next Prayer yang Dipilih: ${schedule.nextPrayer?.type?.displayName} pukul ${schedule.nextPrayer?.time}")
        println("Teks Countdown: ${schedule.countdownText}")

        assertNotNull("Next prayer tidak boleh null", schedule.nextPrayer)
        assertEquals("Jika semua salat hari ini lewat, event berikutnya WAJIB Subuh hari esok", PrayerType.SUBUH, schedule.nextPrayer?.type)
        assertTrue("Countdown text harus menunjukkan hitung mundur aktif", schedule.countdownText.startsWith("-"))
    }

    @Test
    fun `test hijri calendar provider architectural separation`() {
        val testDate = LocalDate.of(2026, 4, 15)

        val arithmeticProvider = ArithmeticHijriProvider()
        val ummAlQuraProvider = UmmAlQuraHijriProvider()
        val mabimsEstimatedProvider = IndonesianMabimsEstimatedProvider()
        val officialProvider = OfficialKemenagCalendarProvider(mabimsEstimatedProvider)

        val arithmeticDate = arithmeticProvider.getHijriDate(testDate)
        val ummAlQuraDate = ummAlQuraProvider.getHijriDate(testDate)
        val mabimsDate = mabimsEstimatedProvider.getHijriDate(testDate)

        println("\n=== AUDIT KALENDER HIJRIAH: PEMISAHAN PROVIDER ===")
        println("Tanggal Masehi: $testDate")
        println("1. Arithmetic Tabular: ${arithmeticProvider.formatHijriDate(testDate)} (Official: ${arithmeticProvider.isOfficialGovernmentSource})")
        println("2. Umm Al-Qura (Saudi): ${ummAlQuraProvider.formatHijriDate(testDate)} (Official: ${ummAlQuraProvider.isOfficialGovernmentSource})")
        println("3. Estimasi MABIMS: ${mabimsEstimatedProvider.formatHijriDate(testDate)} (Official: ${mabimsEstimatedProvider.isOfficialGovernmentSource})")

        assertFalse("Estimasi MABIMS tidak boleh mengklaim sebagai sumber resmi pemerintah", mabimsEstimatedProvider.isOfficialGovernmentSource)
        assertTrue("Official provider harus memiliki flag isOfficialGovernmentSource = true", officialProvider.isOfficialGovernmentSource)

        // Test registering official verification into OfficialKemenagCalendarProvider
        val verifiedHijri = HijriDate(day = 27, monthName = "Syawwal", monthIndex = 10, year = 1447)
        officialProvider.registerVerifiedDate(testDate, verifiedHijri)
        val resolvedOfficial = officialProvider.getHijriDate(testDate)
        assertEquals(27, resolvedOfficial.day)
        assertEquals("Syawwal", resolvedOfficial.monthName)
        println("4. Data Resmi Kemenag (Setelah Registrasi Isbat): ${officialProvider.formatHijriDate(testDate)}")
    }
}
