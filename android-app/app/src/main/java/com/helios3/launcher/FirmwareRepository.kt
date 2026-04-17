package com.helios3.launcher

import android.content.Context
import android.net.Uri

data class FirmwareState(
    val installed: Boolean,
    val displayName: String?,
    val sourceUri: String?,
) {
    fun summaryLine(): String {
        return if (installed) {
            "Firmware: Ready (${displayName ?: "PS3UPDAT.PUP"})"
        } else {
            "Firmware: Not installed"
        }
    }
}

object FirmwareRepository {
    private const val PREFS_NAME = "helios3_firmware"
    private const val KEY_INSTALLED = "installed"
    private const val KEY_NAME = "name"
    private const val KEY_URI = "uri"

    fun load(context: Context): FirmwareState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return FirmwareState(
            installed = prefs.getBoolean(KEY_INSTALLED, false),
            displayName = prefs.getString(KEY_NAME, null),
            sourceUri = prefs.getString(KEY_URI, null),
        )
    }

    fun rememberPickedFirmware(context: Context, uri: Uri) {
        val name = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.ifBlank { "PS3UPDAT.PUP" }
            ?: "PS3UPDAT.PUP"

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_INSTALLED, true)
            .putString(KEY_NAME, name)
            .putString(KEY_URI, uri.toString())
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
