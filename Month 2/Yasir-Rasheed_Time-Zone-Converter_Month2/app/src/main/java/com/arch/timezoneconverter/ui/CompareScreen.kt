package com.arch.timezoneconverter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arch.timezoneconverter.logic.Availability
import com.arch.timezoneconverter.logic.TimeLogic
import com.arch.timezoneconverter.ui.theme.availabilityColor
import com.arch.timezoneconverter.ui.theme.availabilityText
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun CompareScreen(vm: TimeViewModel, modifier: Modifier = Modifier) {
    val zones by vm.zones.collectAsState()
    val is24h by vm.is24h.collectAsState()

    val ref = ZoneId.of(vm.homeZoneId)
    val today = vm.today()
    val step = vm.compareStep.roundToInt().coerceIn(0, 47)
    val selected = today.atTime(step / 2, (step % 2) * 30).atZone(ref)
    val common = TimeLogic.commonWorkingHours(zones, today, ref)

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Pick a time in ${TimeLogic.cityName(vm.homeZoneId)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    TimeLogic.formatTime(selected, is24h),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = vm.compareStep,
                    onValueChange = { vm.compareStep = it },
                    valueRange = 0f..47f,
                    steps = 46
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Legend(Availability.WORKING)
                    Legend(Availability.EDGE)
                    Legend(Availability.NIGHT)
                }
                Text(
                    if (common.isEmpty()) "No hour today is inside working hours for every city."
                    else "Everyone is in working hours (your time): ${TimeLogic.formatHourRanges(common)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        zones.forEach { id ->
            val local = selected.withZoneSameInstant(ZoneId.of(id))
            val availability = TimeLogic.availability(local.hour)
            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(availabilityColor(availability))
                        )
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(TimeLogic.cityName(id), fontWeight = FontWeight.SemiBold)
                            Text(
                                TimeLogic.dayLabel(TimeLogic.dayDifference(selected, local)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                        Text(
                            TimeLogic.formatTime(local, is24h),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // 24-hour strip: each block is one hour of the day in the reference zone
                    Row(
                        Modifier.fillMaxWidth().height(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (h in 0..23) {
                            val z = today.atTime(h, 0).atZone(ref).withZoneSameInstant(ZoneId.of(id))
                            val color = availabilityColor(TimeLogic.availability(z.hour))
                            val shape = RoundedCornerShape(3.dp)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(shape)
                                    .background(color)
                                    .then(
                                        if (h == step / 2) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, shape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }
        Text(
            "Each bar shows the 24 hours of today in ${TimeLogic.cityName(vm.homeZoneId)}, coloured by what the local hour is in that city.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun Legend(a: Availability) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(availabilityColor(a)))
        Text(
            availabilityText(a),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
