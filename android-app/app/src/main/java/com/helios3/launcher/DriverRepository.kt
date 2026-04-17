package com.helios3.launcher

import android.content.Context
import android.net.Uri

data class DriverState(
    val mode: String,
    val customInstalled: Boolean,
    val packageName: String?,
    val sourceUri: String?,
) {
    fun summaryLine(): String {
        return when {
            mode == "Custom" && customInstalled -> "Driver: Custom (${packageName ?: "driver package"})"
            mode == "Custom" -> "Driver: Custom (package missing)"
            else -> "Driver: $mode"
        }
    }
}

object DriverRepository {
    private const val PREFS_NAME = "helios3_drivers"
    private const val KEY_MODE = "mode"
    private const val KEY_CUSTOM_INSTALLED = "custom_installed"
    private const val KEY_PACKAGE_NAME = "package_name"
    private const val KEY_URI = "uri"

    private val modes = listOf(
        "System default",
        "Generic safe",
        "Adreno optimized",
        "Mali compatibility",
        "PowerVR legacy",
        "Turnip experimental",
        "Zink compatibility",
        "Custom",
    )

    fun load(context: Context): DriverState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return DriverState(
            mode = prefs.getString(KEY_MODE, modes.first()) ?: modes.first(),
            customInstalled = prefs.getBoolean(KEY_CUSTOM_INSTALLED, false),
            packageName = prefs.getString(KEY_PACKAGE_NAME, null),
            sourceUri = prefs.getString(KEY_URI, null),
        )
    }

    fun cycleMode(context: Context): DriverState {
        val current = load(context)
        val currentIndex = modes.indexOf(current.mode).takeIf { it >= 0 } ?: 0
        val nextMode = modes[(currentIndex + 1) % modes.size]
        return current.copy(mode = nextMode).also { save(context, it) }
    }

    fun rememberCustomDriver(context: Context, uri: Uri) {
        val name = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.ifBlank { "custom-driver.zip" }
            ?: "custom-driver.zip"

        save(
            context,
            load(context).copy(
                mode = "Custom",
                customInstalled = true,
                packageName = name,
                sourceUri = uri.toString(),
            ),
        )
    }

    fun clearCustomDriver(context: Context) {
        save(
            context,
            load(context).copy(
                mode = "System default",
                customInstalled = false,
                packageName = null,
                sourceUri = null,
            ),
        )
    }

    private fun save(context: Context, state: DriverState) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, state.mode)
            .putBoolean(KEY_CUSTOM_INSTALLED, state.customInstalled)
            .putString(KEY_PACKAGE_NAME, state.packageName)
            .putString(KEY_URI, state.sourceUri)
            .apply()
    }
}
