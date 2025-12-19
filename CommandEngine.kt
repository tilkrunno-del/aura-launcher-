package com.aura.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import java.util.Locale
import kotlin.math.max

sealed class CommandResult {
    data class Action(val title: String, val intent: Intent) : CommandResult()
    data class AppLaunch(val title: String, val packageName: String) : CommandResult()
    data class Hint(val title: String) : CommandResult()
}

object CommandEngine {

    fun resolve(context: Context, raw: String, apps: List<LaunchableApp>): List<CommandResult> {
        val q = normalize(raw)
        if (q.isBlank()) {
            return listOf(CommandResult.Hint("Näited: “pane äratus 6:30”, “taimer 10 min”, “ava wifi”, “helista 5551234”."))
        }

        val results = mutableListOf<CommandResult>()

        parseAlarm(q)?.let { (h, m) ->
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, h)
                putExtra(AlarmClock.EXTRA_MINUTES, m)
                putExtra(AlarmClock.EXTRA_MESSAGE, "AURA")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            results += CommandResult.Action("Pane äratus $h:${m.toString().padStart(2, '0')}", intent)
        }

        parseTimer(q)?.let { seconds ->
            val minutes = max(1, seconds / 60)
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, "AURA taimer")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            results += CommandResult.Action("Pane taimer ~${minutes} min", intent)
        }

        parsePhoneNumber(q)?.let { number ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            results += CommandResult.Action("Helista $number", intent)
        }

        parseSettings(q)?.let { settingsIntent ->
            results += CommandResult.Action("Ava seaded", settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        val appMatches = searchApps(q, apps).take(8)
        appMatches.forEach { app ->
            results += CommandResult.AppLaunch("Ava ${app.label}", app.packageName)
        }

        if (results.isEmpty()) {
            results += CommandResult.Hint("Ei leidnud käsku. Proovi: “ava kaamera”, “pane äratus 7:00”, “taimer 10 min”.")
        }

        return results
    }

    private fun normalize(s: String): String =
        s.trim().lowercase(Locale.getDefault()).replace(Regex("\\s+"), " ")

    private fun parseAlarm(q: String): Pair<Int, Int>? {
        val triggers = listOf("äratus", "pane äratus", "kell", "alarm")
        if (triggers.none { q.contains(it) }) return null

        // HH:MM
        Regex("\\b([01]?\\d|2[0-3])[:\\.]([0-5]\\d)\\b").find(q)?.let {
            val h = it.groupValues[1].toInt()
            val m = it.groupValues[2].toInt()
            return h to m
        }

        // HH
        Regex("\\b([01]?\\d|2[0-3])\\b").find(q)?.let {
            val h = it.groupValues[1].toInt()
            return h to 0
        }
        return null
    }

    private fun parseTimer(q: String): Int? {
        val triggers = listOf("taimer", "timer", "aeg maha")
        if (triggers.none { q.contains(it) }) return null

        var seconds = 0
        Regex("\\b(\\d+)\\s*(h|t|tund|tundi|hours?)\\b").findAll(q).forEach {
            seconds += it.groupValues[1].toInt() * 3600
        }
        Regex("\\b(\\d+)\\s*(min|m|minut|minutit|minutes?)\\b").findAll(q).forEach {
            seconds += it.groupValues[1].toInt() * 60
        }
        Regex("\\b(\\d+)\\s*(s|sek|sekund|sekundit|seconds?)\\b").findAll(q).forEach {
            seconds += it.groupValues[1].toInt()
        }

        if (seconds == 0) {
            Regex("\\btaimer\\s+(\\d+)\\b").find(q)?.let {
                seconds = it.groupValues[1].toInt() * 60
            }
        }

        return if (seconds > 0) seconds else null
    }

    private fun parsePhoneNumber(q: String): String? {
        val triggers = listOf("helista", "vali", "call", "dial")
        if (triggers.none { q.contains(it) }) return null

        Regex("(\\+?\\d[\\d\\s-]{5,}\\d)").find(q)?.let {
            return it.value.replace(" ", "").replace("-", "")
        }
        return null
    }

    private fun parseSettings(q: String): Intent? = when {
        q.contains("wifi") || q.contains("wi-fi") -> Intent(Settings.ACTION_WIFI_SETTINGS)
        q.contains("bluetooth") || q.contains("bt") -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        q.contains("heli") || q.contains("sound") -> Intent(Settings.ACTION_SOUND_SETTINGS)
        q.contains("ekraan") || q.contains("display") -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        q.contains("seaded") || q.contains("settings") -> Intent(Settings.ACTION_SETTINGS)
        else -> null
    }

    private fun searchApps(q: String, apps: List<LaunchableApp>): List<LaunchableApp> {
        val cleaned = q.replace(Regex("^ava\\s+"), "").replace(Regex("^open\\s+"), "").trim()
        if (cleaned.isBlank()) return emptyList()
        return apps.filter { it.label.lowercase(Locale.getDefault()).contains(cleaned) }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}
