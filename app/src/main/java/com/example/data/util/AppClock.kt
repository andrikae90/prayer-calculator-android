package com.example.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Clock abstraction interface to decouple business logic from system time.
 * Enables deterministic testing of countdowns, day boundaries, and timezone conversions.
 */
interface AppClock {
    fun now(zoneId: ZoneId = zoneId()): ZonedDateTime
    fun today(zoneId: ZoneId = zoneId()): LocalDate = now(zoneId).toLocalDate()
    fun currentTime(zoneId: ZoneId = zoneId()): LocalTime = now(zoneId).toLocalTime()
    fun instant(): Instant
    fun zoneId(): ZoneId
}

/**
 * Production system clock.
 */
class SystemAppClock(
    private val defaultZone: ZoneId = ZoneId.of("Asia/Jakarta")
) : AppClock {
    override fun now(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.now(zoneId)
    override fun instant(): Instant = Instant.now()
    override fun zoneId(): ZoneId = defaultZone
}

/**
 * Test clock with fixed or manipulable time.
 */
class TestAppClock(
    private var currentInstant: Instant,
    private val zoneId: ZoneId = ZoneId.of("Asia/Jakarta")
) : AppClock {
    override fun now(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.ofInstant(currentInstant, zoneId)
    override fun instant(): Instant = currentInstant
    override fun zoneId(): ZoneId = zoneId

    fun setInstant(newInstant: Instant) {
        currentInstant = newInstant
    }

    fun advanceMinutes(minutes: Long) {
        currentInstant = currentInstant.plusSeconds(minutes * 60)
    }

    fun advanceSeconds(seconds: Long) {
        currentInstant = currentInstant.plusSeconds(seconds)
    }
}
