package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class LaunchableApp(val label: String, val packageName: String)

@Composable
fun AuraLauncherApp() {
    val context = LocalContext.current
    var commandOpen by remember { mutableStateOf(false) }
    var quickOpen by remember { mutableStateOf(false) }

    val suggestions = remember { listOf("Helista Markole", "Maksa elekter", "Vaikne režiim 1h") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 25) { commandOpen = true; quickOpen = false }
                    else if (dragAmount < -25) { quickOpen = true; commandOpen = false }
                }
            }
    ) {
        HomeScreen(suggestions = suggestions, onSuggestionClick = { commandOpen = true })

        if (quickOpen) {
            QuickActionsSheet(
                onClose = { quickOpen = false },
                onAction = { action ->
                    quickOpen = false
                    runQuickAction(context, action)
                }
            )
        }

        if (commandOpen) {
            CommandOverlay(
                onClose = { commandOpen = false },
                onLaunchApp = { pkg ->
                    commandOpen = false
                    launchApp(context, pkg)
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(suggestions: List<String>, onSuggestionClick: (String) -> Unit) {
    val timeText by rememberClockText()

    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(timeText, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Calm Mode • Home",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Column(Modifier.fillMaxWidth()) {
            Text("Mida soovid teha?", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                items(suggestions) { s ->
                    Surface(
                        onClick = { onSuggestionClick(s) },
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(s, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        Text(
            "Swipe ↓ Command • Swipe ↑ Quick",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun CommandOverlay(onClose: () -> Unit, onLaunchApp: (String) -> Unit) {
    val context = LocalContext.current
    val allApps = remember { queryLaunchableApps(context) }
    var query by remember { mutableStateOf("") }

    val results = remember(query) { CommandEngine.resolve(context, query, allApps) }

    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Command", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClose) { Text("Sulge") }
            }

            Spacer(Modifier.height(12.dp))

            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.padding(14.dp)) {
                    if (query.isBlank()) {
                        Text(
                            "Kirjuta: “pane äratus 6:30”, “taimer 10 min”, “ava wifi”, “helista 5551234”…",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results) { r ->
                    when (r) {
                        is CommandResult.Hint -> {
                            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 0.dp) {
                                Row(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(r.title, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        is CommandResult.Action -> {
                            Surface(onClick = { onClose(); context.startActivity(r.intent) }, shape = MaterialTheme.shapes.large) {
                                Row(Modifier.fillMaxWidth().padding(14.dp)) { Text(r.title, style = MaterialTheme.typography.bodyLarge) }
                            }
                        }
                        is CommandResult.AppLaunch -> {
                            Surface(onClick = { onLaunchApp(r.packageName) }, shape = MaterialTheme.shapes.large) {
                                Row(Modifier.fillMaxWidth().padding(14.dp)) { Text(r.title, style = MaterialTheme.typography.bodyLarge) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "V0.1: äratus • taimer • seaded • helistamine • äppide avamine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private enum class QuickAction { SETTINGS, DIALER }

@Composable
private fun QuickActionsSheet(onClose: () -> Unit, onAction: (QuickAction) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            tonalElevation = 10.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Quick", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onClose) { Text("Sulge") }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = { onAction(QuickAction.SETTINGS) }, label = { Text("Seaded") })
                    AssistChip(onClick = { onAction(QuickAction.DIALER) }, label = { Text("Kõned") })
                }
            }
        }
    }
}

private fun runQuickAction(context: Context, action: QuickAction) {
    when (action) {
        QuickAction.SETTINGS ->
            context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        QuickAction.DIALER ->
            context.startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun queryLaunchableApps(context: Context): List<LaunchableApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

    return resolved.mapNotNull { ri ->
        val label = ri.loadLabel(pm)?.toString() ?: return@mapNotNull null
        val pkg = ri.activityInfo.packageName ?: return@mapNotNull null
        LaunchableApp(label, pkg)
    }.sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private fun launchApp(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
    if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
private fun rememberClockText(): State<String> {
    val formatter = remember { SimpleDateFormat("HH:mm • EEE", Locale.getDefault()) }
    return produceState(initialValue = formatter.format(Date())) {
        while (true) {
            value = formatter.format(Date())
            delay(1000L)
        }
    }
}
