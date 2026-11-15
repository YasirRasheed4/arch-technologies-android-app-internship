package com.collegealert.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.collegealert.data.CampusEvent

/** Schedules / cancels one alarm per event using AlarmManager. */
object ReminderScheduler {

    const val EXTRA_ID = "event_id"
    const val EXTRA_TITLE = "event_title"
    const val EXTRA_LOCATION = "event_location"
    const val EXTRA_START = "event_start"

    private fun pendingIntent(context: Context, event: CampusEvent): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, event.id)
            putExtra(EXTRA_TITLE, event.title)
            putExtra(EXTRA_LOCATION, event.location)
            putExtra(EXTRA_START, event.startMillis)
        }
        return PendingIntent.getBroadcast(
            context, event.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, event: CampusEvent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = event.startMillis - event.reminderMinutes * 60_000L
        if (event.reminderMinutes < 0 || triggerAt <= System.currentTimeMillis()) {
            cancel(context, event)
            return
        }
        // Inexact-while-idle alarm: no special permission needed, fires within a few minutes.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context, event))
    }

    fun cancel(context: Context, event: CampusEvent) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, event))
    }
}
