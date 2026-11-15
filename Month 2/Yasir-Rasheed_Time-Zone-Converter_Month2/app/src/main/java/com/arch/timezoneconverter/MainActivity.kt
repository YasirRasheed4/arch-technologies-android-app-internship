package com.arch.timezoneconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arch.timezoneconverter.ui.CompareScreen
import com.arch.timezoneconverter.ui.ConverterScreen
import com.arch.timezoneconverter.ui.TimeViewModel
import com.arch.timezoneconverter.ui.WorldClockScreen
import com.arch.timezoneconverter.ui.ZonePickerDialog
import com.arch.timezoneconverter.ui.theme.TimeZoneTheme

class MainActivity : ComponentActivity() {
    private val vm: TimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeZoneTheme { AppRoot(vm) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(vm: TimeViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val is24h by vm.is24h.collectAsState()

    val titles = listOf("World Clock", "Convert", "Compare")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.Bold) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("24h")
                        Switch(
                            checked = is24h,
                            onCheckedChange = { vm.set24h(it) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Language, contentDescription = null) },
                    label = { Text("Clocks") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
                    label = { Text("Convert") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
                    label = { Text("Compare") }
                )
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add city")
                }
            }
        }
    ) { padding ->
        val content = Modifier.padding(padding)
        when (tab) {
            0 -> WorldClockScreen(vm, content)
            1 -> ConverterScreen(vm, content)
            else -> CompareScreen(vm, content)
        }
    }

    if (showAdd) {
        ZonePickerDialog(
            title = "Add a city",
            onPick = { vm.addZone(it); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
}
