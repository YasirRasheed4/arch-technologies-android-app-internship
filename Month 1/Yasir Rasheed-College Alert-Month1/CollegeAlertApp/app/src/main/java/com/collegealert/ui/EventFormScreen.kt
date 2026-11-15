@file:OptIn(ExperimentalMaterial3Api::class)

package com.collegealert.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.collegealert.data.CampusEvent
import com.collegealert.data.EventCategory
import java.util.Calendar

/** Used for both "Add event" (existing == null) and "Edit event". */
@Composable
fun EventFormScreen(
    existing: CampusEvent?,
    onBack: () -> Unit,
    onSave: (CampusEvent) -> Unit
) {
    val context = LocalContext.current
    val defaultStart = remember {
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2); set(Calendar.MINUTE, 0) }.timeInMillis
    }

    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var location by remember { mutableStateOf(existing?.location.orEmpty()) }
    var category by remember { mutableStateOf(EventCategory.fromName(existing?.category ?: "ACADEMIC")) }
    var startMillis by remember { mutableLongStateOf(existing?.startMillis ?: defaultStart) }
    var reminder by remember { mutableIntStateOf(existing?.reminderMinutes ?: 30) }
    var important by remember { mutableStateOf(existing?.important ?: false) }
    var titleError by remember { mutableStateOf(false) }

    fun pickDate() {
        val c = Calendar.getInstance().apply { timeInMillis = startMillis }
        DatePickerDialog(context, { _, y, m, d ->
            val n = Calendar.getInstance().apply { timeInMillis = startMillis; set(y, m, d) }
            startMillis = n.timeInMillis
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun pickTime() {
        val c = Calendar.getInstance().apply { timeInMillis = startMillis }
        TimePickerDialog(context, { _, h, min ->
            val n = Calendar.getInstance().apply {
                timeInMillis = startMillis; set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            startMillis = n.timeInMillis
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add event" else "Edit event") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Event title *") },
                isError = titleError,
                supportingText = { if (titleError) Text("Please enter a title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth()
            )

            Text("Category")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventCategory.entries.forEach { cat ->
                    FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.label) })
                }
            }

            Text("Date & time")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { pickDate() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Event, null); Text("  ${formatDate(startMillis)}")
                }
                OutlinedButton(onClick = { pickTime() }) {
                    Icon(Icons.Filled.Schedule, null); Text("  ${formatTime(startMillis)}")
                }
            }

            Text("Remind me")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                reminderOptions.forEach { (minutes, label) ->
                    FilterChip(selected = reminder == minutes, onClick = { reminder = minutes }, label = { Text(label) })
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mark as important")
                Switch(checked = important, onCheckedChange = { important = it })
            }

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    onSave(
                        CampusEvent(
                            id = existing?.id ?: 0,
                            title = title.trim(),
                            description = description.trim(),
                            location = location.trim(),
                            category = category.name,
                            startMillis = startMillis,
                            reminderMinutes = reminder,
                            important = important
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (existing == null) "Save event" else "Update event") }
        }
    }
}
