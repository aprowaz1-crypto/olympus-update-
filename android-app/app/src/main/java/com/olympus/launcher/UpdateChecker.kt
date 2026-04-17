package com.olympus.launcher

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object UpdateChecker {
    private const val PREFS_NAME = "olympus_updates"
    private const val KEY_ACK_VERSION = "ack_version"
    private const val API_URL = "https://api.github.com/repos/RPCS3/rpcs3-binaries-linux-arm64/releases/latest"
    private const val HTML_URL = "https://github.com/RPCS3/rpcs3-binaries-linux-arm64/releases/latest"

    private val executor = Executors.newSingleThreadExecutor()

    data class UpdateState(
        val updateAvailable: Boolean,
        val latestVersion: String?,
        val message: String,
    )

    fun checkForUpdates(context: Context, onResult: (UpdateState) -> Unit) {
        executor.execute {
            val state = runCatching { fetchState(context) }
                .getOrElse {
                    UpdateState(
                        updateAvailable = false,
                        latestVersion = null,
                        message = "Could not check updates right now.",
                    )
                }

            Handler(Looper.getMainLooper()).post {
                onResult(state)
            }
        }
    }

    fun rememberVersion(context: Context, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACK_VERSION, version)
            .apply()
    }

    private fun fetchState(context: Context): UpdateState {
        val latest = fetchLatestVersion()
            ?: return UpdateState(false, null, "Could not check updates right now.")

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val acknowledged = prefs.getString(KEY_ACK_VERSION, null)

        return when {
            acknowledged.isNullOrBlank() -> UpdateState(
                updateAvailable = true,
                latestVersion = latest,
                message = "RPCS3 build available: $latest",
            )
            acknowledged != latest -> UpdateState(
                updateAvailable = true,
                latestVersion = latest,
                message = "Update available: $latest",
            )
            else -> UpdateState(
                updateAvailable = false,
                latestVersion = latest,
                message = "Core is up to date: $latest",
            )
        }
    }

    private fun fetchLatestVersion(): String? {
        return fetchFromApi() ?: fetchFromHtml()
    }

    private fun fetchFromApi(): String? {
        val connection = openConnection(API_URL)
        return connection.inputStream.bufferedReader().use { reader ->
            val body = reader.readText()
            val json = JSONObject(body)
            val assets = json.optJSONArray("assets") ?: return@use json.optString("tag_name", null)
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name")
                val lowered = name.lowercase()
                if ("appimage" in lowered || "aarch64" in lowered || "arm64" in lowered) {
                    return@use name
                }
            }
            json.optString("tag_name", null)
        }
    }

    private fun fetchFromHtml(): String? {
        val connection = openConnection(HTML_URL)
        val html = connection.inputStream.bufferedReader().use { it.readText() }
        val regex = Regex("/RPCS3/rpcs3-binaries-linux-arm64/releases/download/[^\"']*(rpcs3[^\"']+)")
        return regex.find(html)?.groupValues?.getOrNull(1)
    }

    private fun openConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("User-Agent", "Olympus-Android-Port")
            setRequestProperty("Accept", "application/json,text/html")
        }
    }
}
