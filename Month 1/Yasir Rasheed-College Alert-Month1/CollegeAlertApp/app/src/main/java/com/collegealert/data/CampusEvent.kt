package com.collegealert.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

/** One campus event stored in the local Room database. */
@Entity(tableName = "events")
data class CampusEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val location: String,
    val category: String,          // EventCategory.name
    val startMillis: Long,         // event start time (epoch millis)
    val reminderMinutes: Int,      // minutes before start; -1 = no reminder
    val important: Boolean = false
)

enum class EventCategory(val label: String, val color: Color) {
    ACADEMIC("Academic", Color(0xFF2563EB)),
    SEMINAR("Seminar", Color(0xFF7C3AED)),
    SPORTS("Sports", Color(0xFF16A34A)),
    CULTURAL("Cultural", Color(0xFFDB2777)),
    DEADLINE("Deadline", Color(0xFFDC2626)),
    OTHER("Other", Color(0xFF64748B));

    companion object {
        fun fromName(name: String): EventCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
