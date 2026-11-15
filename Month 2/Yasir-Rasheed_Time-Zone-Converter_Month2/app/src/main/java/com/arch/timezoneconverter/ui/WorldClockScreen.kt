package com.arch.timezoneconverter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arch.timezoneconverter.logic.TimeLogic
import com.arch.timezoneconverter.ui.theme.availabilityColor
import java.time.ZoneId

@Composable
fun WorldClockScreen(vm: TimeViewModel, modifier: Modifier = Modifier) {
    val zones by vm.zones.collectAsState()
    val now by vm.now.collectAsState()
    val is24h by vm.is24h.collectAsState()

    val home = ZoneId.of(vm.homeZoneId)
    val homeNow = now.atZone(home)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(zones, key = { it }) { id ->
            val zoned = now.atZone(ZoneId.of(id))
            val isHome = id == vm.homeZoneId
            val diffMinutes = TimeLogic.offsetDifferenceMinutes(vm.homeZoneId, id, now)
            val subtitle = if (isHome) "Your location" else
                "${TimeLogic.dayLabel(TimeLogic.dayDifference(homeNow, zoned))}  |  " +
                    TimeLogic.differenceLabel(diffMinutes)

            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(TimeLogic.cityName(id), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${TimeLogic.regionName(id)}  |  ${TimeLogic.offsetLabel(id, now)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            TimeLogic.formatTime(zoned, is24h),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = availabilityColor(TimeLogic.availability(zoned.hour))
                        )
                        Text(
                            "${TimeLogic.formatDate(zoned)}  ${TimeLogic.abbreviation(zoned)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    if (zones.size > 1 && !isHome) {
                        IconButton(onClick = { vm.removeZone(id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove ${TimeLogic.cityName(id)}")
                        }
                    }
                }
            }
        }
    }
}
