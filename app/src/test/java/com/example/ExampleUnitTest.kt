package com.example

import com.example.data.util.HijriDateCalculator
import com.example.data.util.PrayerCalculator
import com.example.domain.model.CalculationMethod
import com.example.domain.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun `astronomical prayer calculations return valid chronological sequence`() {
        val cal = Calendar.getInstance()
        val times = PrayerCalculator.calculatePrayerTimes(
            calendar = cal,
            latitude = -6.2088,
            longitude = 106.8456,
            method = CalculationMethod.KEMENAG
        )

        assertEquals(7, times.size)
        val prayerMap = times.associateBy { it.type }

        val subuh = prayerMap[PrayerType.SUBUH]
        val dzuhur = prayerMap[PrayerType.DZUHUR]
        val ashar = prayerMap[PrayerType.ASHAR]
        val maghrib = prayerMap[PrayerType.MAGHRIB]
        val isya = prayerMap[PrayerType.ISYA]

        assertNotNull(subuh)
        assertNotNull(dzuhur)
        assertNotNull(ashar)
        assertNotNull(maghrib)
        assertNotNull(isya)

        val subuhMins = subuh!!.hour * 60 + subuh.minute
        val dzuhurMins = dzuhur!!.hour * 60 + dzuhur.minute
        val asharMins = ashar!!.hour * 60 + ashar.minute
        val maghribMins = maghrib!!.hour * 60 + maghrib.minute
        val isyaMins = isya!!.hour * 60 + isya.minute

        assertTrue("Subuh must be before Dzuhur", subuhMins < dzuhurMins)
        assertTrue("Dzuhur must be before Ashar", dzuhurMins < asharMins)
        assertTrue("Ashar must be before Maghrib", asharMins < maghribMins)
        assertTrue("Maghrib must be before Isya", maghribMins < isyaMins)
    }

    @Test
    fun `hijri date calculation returns valid format`() {
        val cal = Calendar.getInstance()
        val hijriDate = HijriDateCalculator.getHijriDate(cal)

        assertTrue(hijriDate.day in 1..30)
        assertTrue(hijriDate.monthIndex in 1..12)
        assertTrue(hijriDate.year >= 1445)
        assertTrue(hijriDate.monthName.isNotEmpty())

        val formatted = HijriDateCalculator.formatHijriDate(cal)
        assertTrue(formatted.endsWith(" H"))
    }
}
