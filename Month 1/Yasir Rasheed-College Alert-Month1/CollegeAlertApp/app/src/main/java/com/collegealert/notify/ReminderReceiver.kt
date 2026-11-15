package com.collegealert.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fires when an alarm goes off and shows the reminder notification. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_ID, 0)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Campus event"
        val location = intent.getStringExtra(ReminderScheduler.EXTRA_LOCATION).orEmpty()
        val start = intent.getLongExtra(ReminderScheduler.EXTRA_START, 0L)

        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(start))
        val where = if (location.isNotBlank()) " at $location" else ""
        NotificationHelper.show(context, id, title, "Starts at $time$where")
    }
}
