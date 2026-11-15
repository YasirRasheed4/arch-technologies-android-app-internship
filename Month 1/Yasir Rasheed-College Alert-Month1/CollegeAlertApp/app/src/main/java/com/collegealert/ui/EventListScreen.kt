@file:OptIn(ExperimentalMaterial3Api::class)

package com.collegealert.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegealert.data.CampusEvent
import com.collegealert.data.EventCategory

@Composable
fun EventListScreen(
    events: List<CampusEvent>,
    onAdd: () -> Unit,
    onOpen: (CampusEvent) -> Unit,
    onToggleImportant: (CampusEvent) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }                 // 0 = Upcoming, 1 = Past
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<EventCategory?>(null) }

    val now = System.currentTimeMillis()
    val visible = events
        .filter { if (tab == 0) it.startMillis >= now else it.startMillis < now }
        .filter { filter == null || it.category == filter!!.name }
        .filter { query.isBlank() || it.title.contains(query, true) || it.location.contains(query, true) }
        .let { if (tab == 0) it else it.reversed() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Alert", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Filled.Add, contentDescription = "Add event")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Upcoming") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Past") })
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search events or places") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
                }
                items(EventCategory.entries.toList()) { cat ->
                    FilterChip(
                        selected = filter == cat,
                        onClick = { filter = if (filter == cat) null else cat },
                        label = { Text(cat.label) }
                    )
                }
            }
            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Event, null, Modifier.size(56.dp), tint = Color.Gray)
                        Spacer(Modifier.size(8.dp))
                        Text("No events here yet", color = Color.Gray)
                        Text("Tap + to add one", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.id }) { event ->
                        EventCard(event, onClick = { onOpen(event) }, onStar = { onToggleImportant(event) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: CampusEvent, onClick: () -> Unit, onStar: () -> Unit) {
    val category = EventCategory.fromName(event.category)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.width(5.dp).size(width = 5.dp, height = 72.dp)
                    .clip(RoundedCornerShape(3.dp)).background(category.color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(category.label.uppercase(), color = category.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(event.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, null, Modifier.size(15.dp), tint = Color.Gray)
                    Text("  ${formatDate(event.startMillis)} · ${formatTime(event.startMillis)}",
                        fontSize = 13.sp, color = Color.DarkGray)
                }
                if (event.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, Modifier.size(15.dp), tint = Color.Gray)
                        Text("  ${event.location}", fontSize = 13.sp, color = Color.DarkGray, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.size(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(countdown(event.startMillis), fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary)
                    if (event.reminderMinutes >= 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.NotificationsActive, null, Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Text(" ${reminderLabel(event.reminderMinutes)}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            IconButton(onClick = onStar) {
                Icon(
                    if (event.important) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Mark important",
                    tint = if (event.important) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
        }
    }
}
