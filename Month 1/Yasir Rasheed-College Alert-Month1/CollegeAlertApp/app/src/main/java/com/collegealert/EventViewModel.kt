package com.collegealert

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.collegealert.data.AppDatabase
import com.collegealert.data.CampusEvent
import com.collegealert.data.EventCategory
import com.collegealert.notify.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).eventDao()

    val events: StateFlow<List<CampusEvent>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { if (dao.count() == 0) seedSampleEvents() }
    }

    fun save(event: CampusEvent) = viewModelScope.launch {
        val id = if (event.id == 0) dao.insert(event).toInt() else { dao.update(event); event.id }
        ReminderScheduler.schedule(getApplication(), event.copy(id = id))
    }

    fun delete(event: CampusEvent) = viewModelScope.launch {
        ReminderScheduler.cancel(getApplication(), event)
        dao.delete(event)
    }

    fun toggleImportant(event: CampusEvent) = save(event.copy(important = !event.important))

    private suspend fun seedSampleEvents() {
        val hour = 3_600_000L
        val day = 24 * hour
        val now = System.currentTimeMillis()
        listOf(
            CampusEvent(title = "Operating Systems Midterm", description = "Chapters 1-6. Bring your student ID and a pen.",
                location = "Exam Hall B", category = EventCategory.ACADEMIC.name,
                startMillis = now + 2 * day, reminderMinutes = 60, important = true),
            CampusEvent(title = "Inter-Department Cricket Match", description = "SE vs CS. Come and support the team!",
                location = "Main Ground", category = EventCategory.SPORTS.name,
                startMillis = now + 3 * day + 2 * hour, reminderMinutes = 30),
            CampusEvent(title = "AI Career Seminar", description = "Guest speaker from the industry on careers in AI.",
                location = "Auditorium", category = EventCategory.SEMINAR.name,
                startMillis = now + 5 * day, reminderMinutes = 1440),
            CampusEvent(title = "Database Project Submission", description = "Upload report and ERD to the LMS before the deadline.",
                location = "Online (LMS)", category = EventCategory.DEADLINE.name,
                startMillis = now + 7 * day, reminderMinutes = 1440, important = true),
            CampusEvent(title = "Cultural Night", description = "Music, food stalls and performances.",
                location = "Campus Lawn", category = EventCategory.CULTURAL.name,
                startMillis = now + 10 * day, reminderMinutes = 60),
            CampusEvent(title = "Welcome Orientation", description = "Orientation session for new students.",
                location = "Auditorium", category = EventCategory.OTHER.name,
                startMillis = now - 4 * day, reminderMinutes = -1)
        ).forEach { e ->
            val id = dao.insert(e).toInt()
            ReminderScheduler.schedule(getApplication(), e.copy(id = id))
        }
    }
}
