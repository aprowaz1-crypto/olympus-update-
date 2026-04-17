package com.olympus.launcher

import android.view.Surface

object NativeBridge {
    init {
        try {
            System.loadLibrary("olympus_port")
        } catch (_: UnsatisfiedLinkError) {
        }
    }

    external fun nativeGetCoreStatus(): String
    external fun nativeGetBuildInfo(): String
    external fun nativeSetSurface(surface: Surface?, width: Int, height: Int)
    external fun nativeClearSurface()
    external fun nativeOnGamepadState(axes: FloatArray, buttons: IntArray)

    fun getCoreStatusSafe(): String = runCatching { nativeGetCoreStatus() }
        .getOrElse { "Native library is not loaded yet. Build the Android project in Android Studio." }

    fun getBuildInfoSafe(): String = runCatching { nativeGetBuildInfo() }
        .getOrElse { "NDK bridge has not been built yet." }

    fun setSurface(surface: Surface?, width: Int, height: Int) {
        runCatching { nativeSetSurface(surface, width, height) }
    }

    fun clearSurface() {
        runCatching { nativeClearSurface() }
    }

    fun onGamepadState(axes: FloatArray, buttons: IntArray) {
        runCatching { nativeOnGamepadState(axes, buttons) }
    }
}
