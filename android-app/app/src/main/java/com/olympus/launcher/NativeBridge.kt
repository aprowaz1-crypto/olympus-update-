package com.olympus.launcher

object NativeBridge {
    init {
        try {
            System.loadLibrary("olympus_port")
        } catch (_: UnsatisfiedLinkError) {
        }
    }

    external fun nativeGetCoreStatus(): String
    external fun nativeGetBuildInfo(): String

    fun getCoreStatusSafe(): String = runCatching { nativeGetCoreStatus() }
        .getOrElse { "Native library is not loaded yet. Build the Android project in Android Studio." }

    fun getBuildInfoSafe(): String = runCatching { nativeGetBuildInfo() }
        .getOrElse { "NDK bridge has not been built yet." }
}
