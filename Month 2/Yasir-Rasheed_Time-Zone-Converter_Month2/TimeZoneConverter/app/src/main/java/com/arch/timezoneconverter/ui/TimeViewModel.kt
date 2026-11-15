package com.arch.timezoneconverter.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arch.timezoneconverter.data.ZoneStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ZoneStore(app)

    /** The device's own zone - everything is compared relative to this. */
    val homeZoneId: String = ZoneId.systemDefault().id

    val zones = MutableStateFlow(store.loadZones())
    val is24h = MutableStateFlow(store.is24h())

    /** Ticks once a second so the world clocks stay live. */
    val now: StateFlow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Instant.now())

    // ---- Converter state ----
    var fromZone by mutableStateOf(homeZoneId)
    var toZone by mutableStateOf(if (homeZoneId == "America/New_York") "Europe/London" else "America/New_York")
    var dateTime: LocalDateTime by mutableStateOf(currentTimeIn(homeZoneId))

    // ---- Compare state: half-hour slot 0..47 of today in the home zone ----
    var compareStep by mutableFloatStateOf(currentHalfHourSlot())

    fun addZone(id: String) {
        if (id in zones.value) return
        zones.value = zones.value + id
        store.saveZones(zones.value)
    }

    fun removeZone(id: String) {
        if (zones.value.size <= 1) return
        zones.value = zones.value - id
        store.saveZones(zones.value)
    }

    fun set24h(value: Boolean) {
        is24h.value = value
        store.set24h(value)
    }

    fun swapZones() {
        val tmp = fromZone
        fromZone = toZone
        toZone = tmp
    }

    fun useCurrentTime() {
        dateTime = currentTimeIn(fromZone)
    }

    private fun currentTimeIn(zoneId: String): LocalDateTime =
        ZonedDateTime.now(ZoneId.of(zoneId)).toLocalDateTime().withSecond(0).withNano(0)

    private fun currentHalfHourSlot(): Float {
        val t = ZonedDateTime.now(ZoneId.of(homeZoneId))
        return (t.hour * 2 + if (t.minute >= 30) 1 else 0).toFloat()
    }

    fun today(): LocalDate = LocalDate.now(ZoneId.of(homeZoneId))
}
