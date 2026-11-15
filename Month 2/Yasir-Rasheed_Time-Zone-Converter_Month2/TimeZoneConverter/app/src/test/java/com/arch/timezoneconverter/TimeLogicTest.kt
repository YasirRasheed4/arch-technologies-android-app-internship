package com.arch.timezoneconverter

import com.arch.timezoneconverter.logic.Availability
import com.arch.timezoneconverter.logic.TimeLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TimeLogicTest {

    private val khi = ZoneId.of("Asia/Karachi")
    private val ny = ZoneId.of("America/New_York")
    private val london = ZoneId.of("Europe/London")

    @Test
    fun karachiToNewYork_standardTime() {
        val r = TimeLogic.convert(LocalDateTime.of(2026, 11, 9, 10, 0), khi, ny)
        assertEquals(LocalDateTime.of(2026, 11, 9, 0, 0), r.toLocalDateTime())
    }

    @Test
    fun karachiToNewYork_daylightSaving() {
        val r = TimeLogic.convert(LocalDateTime.of(2026, 7, 1, 10, 0), khi, ny)
        assertEquals(LocalDateTime.of(2026, 7, 1, 1, 0), r.toLocalDateTime())
    }

    @Test
    fun conversionCanCrossMidnight() {
        val source = LocalDateTime.of(2026, 11, 9, 2, 0).atZone(khi)
        val r = TimeLogic.convert(source.toLocalDateTime(), khi, london)
        assertEquals(LocalDateTime.of(2026, 11, 8, 21, 0), r.toLocalDateTime())
        assertEquals(-1, TimeLogic.dayDifference(source, r))
        assertEquals("Previous day", TimeLogic.dayLabel(-1))
    }

    @Test
    fun halfHourOffsetIsHandled() {
        val r = TimeLogic.convert(LocalDateTime.of(2026, 11, 9, 12, 0), khi, ZoneId.of("Asia/Kolkata"))
        assertEquals(LocalDateTime.of(2026, 11, 9, 12, 30), r.toLocalDateTime())
    }

    @Test
    fun roundTripReturnsOriginalTime() {
        val original = LocalDateTime.of(2026, 11, 9, 15, 45)
        val there = TimeLogic.convert(original, khi, ZoneId.of("Australia/Sydney"))
        val back = TimeLogic.convert(there.toLocalDateTime(), ZoneId.of("Australia/Sydney"), khi)
        assertEquals(original, back.toLocalDateTime())
    }

    @Test
    fun offsetLabelsRespectDaylightSaving() {
        val winter = LocalDateTime.of(2026, 11, 9, 12, 0).atZone(khi).toInstant()
        val summer = LocalDateTime.of(2026, 7, 1, 12, 0).atZone(khi).toInstant()
        assertEquals("UTC+05:00", TimeLogic.offsetLabel("Asia/Karachi", winter))
        assertEquals("UTC+05:30", TimeLogic.offsetLabel("Asia/Kolkata", winter))
        assertEquals("UTC-05:00", TimeLogic.offsetLabel("America/New_York", winter))
        assertEquals("UTC-04:00", TimeLogic.offsetLabel("America/New_York", summer))
    }

    @Test
    fun differenceLabels() {
        val at = LocalDateTime.of(2026, 11, 9, 12, 0).atZone(khi).toInstant()
        assertEquals("-10h", TimeLogic.differenceLabel(TimeLogic.offsetDifferenceMinutes("Asia/Karachi", "America/New_York", at)))
        assertEquals("+30m", TimeLogic.differenceLabel(TimeLogic.offsetDifferenceMinutes("Asia/Karachi", "Asia/Kolkata", at)))
        assertEquals("Same time", TimeLogic.differenceLabel(0))
    }

    @Test
    fun timeFormatting() {
        val t = LocalDateTime.of(2026, 11, 9, 15, 5).atZone(khi)
        assertEquals("3:05 PM", TimeLogic.formatTime(t, is24h = false))
        assertEquals("15:05", TimeLogic.formatTime(t, is24h = true))
    }

    @Test
    fun availabilityBands() {
        assertEquals(Availability.WORKING, TimeLogic.availability(10))
        assertEquals(Availability.EDGE, TimeLogic.availability(19))
        assertEquals(Availability.NIGHT, TimeLogic.availability(3))
    }

    @Test
    fun commonWorkingHours() {
        val date = LocalDate.of(2026, 11, 9)
        val dubai = TimeLogic.commonWorkingHours(listOf("Asia/Karachi", "Asia/Dubai"), date, khi)
        assertEquals("10:00-18:00", TimeLogic.formatHourRanges(dubai))
        val london = TimeLogic.commonWorkingHours(listOf("Asia/Karachi", "Europe/London"), date, khi)
        assertEquals("14:00-18:00", TimeLogic.formatHourRanges(london))
        val three = TimeLogic.commonWorkingHours(listOf("Asia/Karachi", "Europe/London", "America/New_York"), date, khi)
        assertTrue(three.isEmpty())
    }

    @Test
    fun searchAndZoneList() {
        assertTrue(TimeLogic.matches("America/New_York", "new yo"))
        assertTrue("Asia/Karachi" in TimeLogic.allZoneIds)
    }
}
