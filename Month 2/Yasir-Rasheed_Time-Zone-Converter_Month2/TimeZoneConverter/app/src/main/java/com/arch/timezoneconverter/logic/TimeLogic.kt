package com.arch.timezoneconverter.logic

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/** How comfortable a given local hour is for a call or meeting. */
enum class Availability { WORKING, EDGE, NIGHT }

/**
 * Pure time-zone logic (no Android dependencies) so it can be unit tested on the JVM.
 */
object TimeLogic {

    private val regions = setOf(
        "Africa", "America", "Antarctica", "Asia", "Atlantic",
        "Australia", "Europe", "Indian", "Pacific"
    )

    /** Every selectable zone id, e.g. "Asia/Karachi", sorted by city name. */
    val allZoneIds: List<String> by lazy {
        val ids = ZoneId.getAvailableZoneIds()
            .filter { it.substringBefore('/') in regions && it.contains('/') }
            .sortedBy { cityName(it) }
        listOf("UTC") + ids
    }

    fun cityName(zoneId: String): String =
        if (zoneId == "UTC") "UTC" else zoneId.substringAfterLast('/').replace('_', ' ')

    fun regionName(zoneId: String): String =
        if (zoneId == "UTC") "Coordinated Universal Time"
        else zoneId.substringBefore('/').replace('_', ' ')

    /** Offset such as "UTC+05:00" or "UTC-03:30" at the given instant (so DST is respected). */
    fun offsetLabel(zoneId: String, at: Instant): String {
        val total = ZoneId.of(zoneId).rules.getOffset(at).totalSeconds
        val sign = if (total < 0) "-" else "+"
        val secs = abs(total)
        val h = secs / 3600
        val m = (secs % 3600) / 60
        return "UTC%s%02d:%02d".format(Locale.ENGLISH, sign, h, m)
    }

    /** Converts a wall-clock time in [from] to the same instant expressed in [to]. */
    fun convert(local: LocalDateTime, from: ZoneId, to: ZoneId): ZonedDateTime =
        local.atZone(from).withZoneSameInstant(to)

    /** Calendar-day difference of [other] relative to [base] (-1 yesterday, 0 same day, 1 tomorrow). */
    fun dayDifference(base: ZonedDateTime, other: ZonedDateTime): Int =
        ChronoUnit.DAYS.between(base.toLocalDate(), other.toLocalDate()).toInt()

    fun dayLabel(diff: Int): String = when {
        diff == 0 -> "Same day"
        diff == 1 -> "Next day"
        diff == -1 -> "Previous day"
        diff > 1 -> "+$diff days"
        else -> "$diff days"
    }

    /** Difference between two zones at an instant, in minutes (target minus source). */
    fun offsetDifferenceMinutes(from: String, to: String, at: Instant): Int {
        val a = ZoneId.of(from).rules.getOffset(at).totalSeconds
        val b = ZoneId.of(to).rules.getOffset(at).totalSeconds
        return (b - a) / 60
    }

    /** "+5h", "-3h 30m", "same time". */
    fun differenceLabel(minutes: Int): String {
        if (minutes == 0) return "Same time"
        val sign = if (minutes > 0) "+" else "-"
        val mins = abs(minutes)
        val h = mins / 60
        val m = mins % 60
        return buildString {
            append(sign)
            if (h > 0) append("${h}h")
            if (m > 0) append(if (h > 0) " ${m}m" else "${m}m")
        }
    }

    fun availability(hour: Int): Availability = when (hour) {
        in 9..17 -> Availability.WORKING
        in 7..8, in 18..21 -> Availability.EDGE
        else -> Availability.NIGHT
    }

    fun formatTime(time: ZonedDateTime, is24h: Boolean, withSeconds: Boolean = false): String {
        val pattern = when {
            is24h && withSeconds -> "HH:mm:ss"
            is24h -> "HH:mm"
            withSeconds -> "h:mm:ss a"
            else -> "h:mm a"
        }
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    }

    fun formatDate(time: ZonedDateTime): String =
        time.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH))

    /** Zone abbreviation such as "PKT", "EST" or "GMT+9". */
    fun abbreviation(time: ZonedDateTime): String =
        time.format(DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH))

    /**
     * Hours (0-23, in the [reference] zone on [date]) when every zone in [zoneIds]
     * is inside normal working hours.
     */
    fun commonWorkingHours(zoneIds: List<String>, date: LocalDate, reference: ZoneId): List<Int> =
        (0..23).filter { hour ->
            val base = date.atTime(hour, 0).atZone(reference)
            zoneIds.all { id ->
                availability(base.withZoneSameInstant(ZoneId.of(id)).hour) == Availability.WORKING
            }
        }

    /** Turns [13, 14, 15, 18] into "13:00-16:00, 18:00-19:00". */
    fun formatHourRanges(hours: List<Int>): String {
        if (hours.isEmpty()) return ""
        val ranges = mutableListOf<String>()
        var start = hours.first()
        var prev = start
        for (h in hours.drop(1)) {
            if (h != prev + 1) {
                ranges += "%02d:00-%02d:00".format(start, prev + 1)
                start = h
            }
            prev = h
        }
        ranges += "%02d:00-%02d:00".format(start, prev + 1)
        return ranges.joinToString(", ")
    }

    fun matches(zoneId: String, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase(Locale.ENGLISH)
        return zoneId.lowercase(Locale.ENGLISH).replace('_', ' ').contains(q)
    }
}
