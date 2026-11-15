package com.arch.timezoneconverter.data

import android.content.Context
import java.time.ZoneId

/** Saves the user's chosen zones and clock format in SharedPreferences. */
class ZoneStore(context: Context) {

    private val prefs = context.getSharedPreferences("tz_prefs", Context.MODE_PRIVATE)

    fun loadZones(): List<String> {
        val saved = prefs.getString(KEY_ZONES, null)
        if (saved.isNullOrBlank()) return defaultZones()
        val valid = saved.split(",").filter { runCatching { ZoneId.of(it) }.isSuccess }
        return valid.ifEmpty { defaultZones() }
    }

    fun saveZones(zones: List<String>) {
        prefs.edit().putString(KEY_ZONES, zones.joinToString(",")).apply()
    }

    fun is24h(): Boolean = prefs.getBoolean(KEY_24H, false)

    fun set24h(value: Boolean) {
        prefs.edit().putBoolean(KEY_24H, value).apply()
    }

    private fun defaultZones(): List<String> {
        val home = ZoneId.systemDefault().id
        val base = listOf("Europe/London", "America/New_York", "Asia/Dubai", "Asia/Tokyo", "Australia/Sydney")
        return (listOf(home) + base).distinct()
    }

    private companion object {
        const val KEY_ZONES = "zones"
        const val KEY_24H = "is24h"
    }
}
