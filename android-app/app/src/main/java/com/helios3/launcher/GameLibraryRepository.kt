package com.helios3.launcher

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class GameEntry(
    val title: String,
    val gameUri: String,
    val coverUri: String?,
    val lastEvent: String,
) {
    fun summaryLine(): String {
        val type = title.substringAfterLast('.', "Game")
            .takeIf { it.isNotBlank() && it != title }
            ?.uppercase()
            ?: "Game"
        val coverState = if (coverUri != null) "cover ready" else "default cover"
        return "$type • $coverState • $lastEvent"
    }
}

object GameLibraryRepository {
    private const val PREFS_NAME = "helios3_game_library"
    private const val KEY_GAMES = "games"
    private const val KEY_SELECTED_URI = "selected_uri"

    fun load(context: Context): List<GameEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_GAMES, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())

        return buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                add(
                    GameEntry(
                        title = obj.optString("title", "PS3 Game"),
                        gameUri = obj.optString("gameUri", ""),
                        coverUri = obj.optString("coverUri").ifBlank { null },
                        lastEvent = obj.optString("lastEvent", "Ready to boot"),
                    ),
                )
            }
        }.filter { it.gameUri.isNotBlank() }
    }

    fun selected(context: Context): GameEntry? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedUri = prefs.getString(KEY_SELECTED_URI, null)
        val games = load(context)
        return games.firstOrNull { it.gameUri == selectedUri } ?: games.firstOrNull()
    }

    fun addGame(context: Context, uri: Uri): GameEntry {
        val title = displayName(context, uri)
        val entry = GameEntry(
            title = title,
            gameUri = uri.toString(),
            coverUri = if (context.contentResolver.getType(uri)?.startsWith("image/") == true) uri.toString() else null,
            lastEvent = "Added to library",
        )

        val games = load(context)
            .filterNot { it.gameUri == entry.gameUri }
            .toMutableList()
            .apply { add(0, entry) }

        save(context, games, entry.gameUri)
        return entry
    }

    fun attachCover(context: Context, gameUri: String, coverUri: Uri) {
        val updated = load(context).map {
            if (it.gameUri == gameUri) it.copy(coverUri = coverUri.toString(), lastEvent = "Custom art added") else it
        }
        save(context, updated, gameUri)
    }

    fun selectGame(context: Context, gameUri: String): GameEntry? {
        val games = load(context)
        save(context, games, gameUri)
        return games.firstOrNull { it.gameUri == gameUri }
    }

    fun markLaunched(context: Context, gameUri: String) {
        val updated = load(context).map {
            if (it.gameUri == gameUri) it.copy(lastEvent = "Launch request sent") else it
        }
        save(context, updated, gameUri)
    }

    fun removeGame(context: Context, gameUri: String) {
        val remaining = load(context).filterNot { it.gameUri == gameUri }
        val selectedUri = remaining.firstOrNull()?.gameUri
        save(context, remaining, selectedUri)
    }

    fun clear(context: Context) {
        save(context, emptyList(), null)
    }

    private fun save(context: Context, games: List<GameEntry>, selectedUri: String?) {
        val array = JSONArray().apply {
            games.forEach { game ->
                put(
                    JSONObject().apply {
                        put("title", game.title)
                        put("gameUri", game.gameUri)
                        put("coverUri", game.coverUri)
                        put("lastEvent", game.lastEvent)
                    },
                )
            }
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GAMES, array.toString())
            .putString(KEY_SELECTED_URI, selectedUri)
            .apply()
    }

    private fun displayName(context: Context, uri: Uri): String {
        val fromPath = Uri.decode(uri.lastPathSegment ?: "PS3 Game")
            .substringAfterLast('/')
            .ifBlank { "PS3 Game" }

        return fromPath.substringBeforeLast('.').ifBlank {
            context.getString(R.string.generic_game_title)
        }
    }
}
