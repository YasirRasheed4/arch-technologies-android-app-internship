package com.arch.timezoneconverter.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arch.timezoneconverter.logic.TimeLogic
import com.arch.timezoneconverter.ui.theme.availabilityColor
import com.arch.timezoneconverter.ui.theme.availabilityText
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

private enum class PickerTarget { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(vm: TimeViewModel, modifier: Modifier = Modifier) {
    val is24h by vm.is24h.collectAsState()
    val zones by vm.zones.collectAsState()

    var picker by remember { mutableStateOf<PickerTarget?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    val from = ZoneId.of(vm.fromZone)
    val to = ZoneId.of(vm.toZone)
    val source = vm.dateTime.atZone(from)
    val result = TimeLogic.convert(vm.dateTime, from, to)
    val dayDiff = TimeLogic.dayDifference(source, result)
    val minutesDiff = TimeLogic.offsetDifferenceMinutes(vm.fromZone, vm.toZone, source.toInstant())
    val availability = TimeLogic.availability(result.hour)

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- From ----
        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("From", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                ZoneSelector(vm.fromZone, source.toInstant()) { picker = PickerTarget.FROM }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDate = true }, modifier = Modifier.weight(1f)) {
                        Text(TimeLogic.formatDate(source))
                    }
                    OutlinedButton(onClick = { showTime = true }, modifier = Modifier.weight(1f)) {
                        Text(TimeLogic.formatTime(source, is24h))
                    }
                }
                TextButton(onClick = { vm.useCurrentTime() }) { Text("Use current time") }
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledTonalIconButton(onClick = { vm.swapZones() }) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Swap zones")
            }
        }

        // ---- To ----
        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("To", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                ZoneSelector(vm.toZone, source.toInstant()) { picker = PickerTarget.TO }
                Spacer(Modifier.height(4.dp))
                Text(
                    TimeLogic.formatTime(result, is24h),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = availabilityColor(availability)
                )
                Text(
                    "${TimeLogic.formatDate(result)}  ${TimeLogic.abbreviation(result)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${TimeLogic.dayLabel(dayDiff)}  |  ${TimeLogic.differenceLabel(minutesDiff)}  |  ${availabilityText(availability)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (vm.toZone !in zones) {
                    OutlinedButton(onClick = { vm.addZone(vm.toZone) }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("  Add to World Clock")
                    }
                }
            }
        }
    }

    // ---- Dialogs ----
    picker?.let { target ->
        ZonePickerDialog(
            title = if (target == PickerTarget.FROM) "Convert from" else "Convert to",
            onPick = { id ->
                if (target == PickerTarget.FROM) vm.fromZone = id else vm.toZone = id
                picker = null
            },
            onDismiss = { picker = null }
        )
    }

    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = vm.dateTime.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        vm.dateTime = LocalDateTime.of(date, vm.dateTime.toLocalTime())
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showTime) {
        val state = rememberTimePickerState(
            initialHour = vm.dateTime.hour,
            initialMinute = vm.dateTime.minute,
            is24Hour = is24h
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.dateTime = vm.dateTime.withHour(state.hour).withMinute(state.minute)
                    showTime = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancel") } },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            }
        )
    }
}

@Composable
private fun ZoneSelector(zoneId: String, at: Instant, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(TimeLogic.cityName(zoneId), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${TimeLogic.regionName(zoneId)}  |  ${TimeLogic.offsetLabel(zoneId, at)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}
