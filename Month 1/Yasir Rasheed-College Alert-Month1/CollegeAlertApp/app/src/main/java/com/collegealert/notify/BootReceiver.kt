package com.collegealert.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.collegealert.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms are cleared on reboot, so re-schedule every saved event. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.get(context).eventDao().getAll()
                    .forEach { ReminderScheduler.schedule(context, it) }
            } finally {
                pending.finish()
            }
        }
    }
}
