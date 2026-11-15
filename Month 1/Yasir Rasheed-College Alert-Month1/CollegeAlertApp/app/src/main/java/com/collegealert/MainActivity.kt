package com.collegealert

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.collegealert.ui.CollegeAlertTheme
import com.collegealert.ui.EventDetailScreen
import com.collegealert.ui.EventFormScreen
import com.collegealert.ui.EventListScreen

private sealed interface Screen {
    data object List : Screen
    data class Detail(val id: Int) : Screen
    data class Form(val id: Int?) : Screen      // id == null -> add new event
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollegeAlertTheme {
                RequestNotificationPermission()
                CollegeAlertApp()
            }
        }
    }
}

/** Android 13+ requires the user to allow notifications at runtime. */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
}

@Composable
private fun CollegeAlertApp(vm: EventViewModel = viewModel()) {
    val events by vm.events.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf<Screen>(Screen.List) }

    when (val s = screen) {
        Screen.List -> EventListScreen(
            events = events,
            onAdd = { screen = Screen.Form(null) },
            onOpen = { screen = Screen.Detail(it.id) },
            onToggleImportant = { vm.toggleImportant(it) }
        )

        is Screen.Detail -> {
            BackHandler { screen = Screen.List }
            val event = events.firstOrNull { it.id == s.id }
            if (event == null) {
                LaunchedEffect(Unit) { screen = Screen.List }
            } else EventDetailScreen(
                event = event,
                onBack = { screen = Screen.List },
                onEdit = { screen = Screen.Form(event.id) },
                onDelete = { vm.delete(event); screen = Screen.List }
            )
        }

        is Screen.Form -> {
            BackHandler { screen = if (s.id == null) Screen.List else Screen.Detail(s.id) }
            EventFormScreen(
                existing = events.firstOrNull { it.id == s.id },
                onBack = { screen = if (s.id == null) Screen.List else Screen.Detail(s.id) },
                onSave = { vm.save(it); screen = Screen.List }
            )
        }
    }
}
