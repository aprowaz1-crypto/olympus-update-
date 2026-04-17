package com.helios3.launcher

import android.content.Context

data class LauncherSettings(
    val renderer: String,
    val resolutionScale: String,
    val frameLimit: String,
    val audioBackend: String,
    val audioBuffer: String,
    val ppuDecoder: String,
    val spuDecoder: String,
    val devicePreset: String,
    val shaderCache: Boolean,
    val vSync: Boolean,
    val autoLandscape: Boolean,
    val overlay: Boolean,
    val haptics: Boolean,
    val autoCheckUpdates: Boolean,
) {
    fun summaryLines(): List<String> = listOf(
        "Device preset: $devicePreset",
        "Renderer: $renderer",
        "Resolution scale: $resolutionScale",
        "Frame limit: $frameLimit",
        "Audio backend: $audioBackend",
        "Audio buffer: $audioBuffer",
        "PPU compiler: $ppuDecoder",
        "SPU compiler: $spuDecoder",
        "VSync: ${if (vSync) "On" else "Off"}",
        "Auto landscape: ${if (autoLandscape) "On" else "Off"}",
        "Shader cache: ${if (shaderCache) "On" else "Off"}",
        "Overlay controls: ${if (overlay) "On" else "Off"}",
        "Gamepad haptics: ${if (haptics) "On" else "Off"}",
        "Auto updates: ${if (autoCheckUpdates) "On" else "Off"}",
    )
}

object SettingsRepository {
    private const val PREFS_NAME = "helios3_native_settings"
    private const val KEY_RENDERER = "renderer"
    private const val KEY_RESOLUTION_SCALE = "resolution_scale"
    private const val KEY_FRAME_LIMIT = "frame_limit"
    private const val KEY_AUDIO_BACKEND = "audio_backend"
    private const val KEY_AUDIO_BUFFER = "audio_buffer"
    private const val KEY_PPU_DECODER = "ppu_decoder"
    private const val KEY_SPU_DECODER = "spu_decoder"
    private const val KEY_DEVICE_PRESET = "device_preset"
    private const val KEY_SHADER_CACHE = "shader_cache"
    private const val KEY_VSYNC = "vsync"
    private const val KEY_AUTO_LANDSCAPE = "auto_landscape"
    private const val KEY_OVERLAY = "overlay"
    private const val KEY_HAPTICS = "haptics"
    private const val KEY_AUTO_UPDATES = "auto_updates"

    private val renderers = listOf("Vulkan", "OpenGL (compat)", "Zink test")
    private val scales = listOf("75%", "100%", "125%", "150%", "200%")
    private val frameLimits = listOf("Auto", "30 FPS", "60 FPS", "120 FPS", "Unlocked")
    private val audioBackends = listOf("AAudio", "OpenSL ES", "Cubeb")
    private val audioBuffers = listOf("Low latency", "Balanced", "Very safe")
    private val ppuDecoders = listOf("LLVM recompiler", "Fast interpreter", "Interpreter")
    private val spuDecoders = listOf("LLVM recompiler", "ASMJIT recompiler", "Safe interpreter")
    private val devicePresets = listOf("Balanced", "High compatibility", "Battery saver", "High performance")

    fun load(context: Context): LauncherSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return LauncherSettings(
            renderer = prefs.getString(KEY_RENDERER, renderers.first()) ?: renderers.first(),
            resolutionScale = prefs.getString(KEY_RESOLUTION_SCALE, scales[1]) ?: scales[1],
            frameLimit = prefs.getString(KEY_FRAME_LIMIT, frameLimits.first()) ?: frameLimits.first(),
            audioBackend = prefs.getString(KEY_AUDIO_BACKEND, audioBackends.first()) ?: audioBackends.first(),
            audioBuffer = prefs.getString(KEY_AUDIO_BUFFER, audioBuffers.first()) ?: audioBuffers.first(),
            ppuDecoder = prefs.getString(KEY_PPU_DECODER, ppuDecoders.first()) ?: ppuDecoders.first(),
            spuDecoder = prefs.getString(KEY_SPU_DECODER, spuDecoders.first()) ?: spuDecoders.first(),
            devicePreset = prefs.getString(KEY_DEVICE_PRESET, devicePresets.first()) ?: devicePresets.first(),
            shaderCache = prefs.getBoolean(KEY_SHADER_CACHE, true),
            vSync = prefs.getBoolean(KEY_VSYNC, true),
            autoLandscape = prefs.getBoolean(KEY_AUTO_LANDSCAPE, true),
            overlay = prefs.getBoolean(KEY_OVERLAY, false),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            autoCheckUpdates = prefs.getBoolean(KEY_AUTO_UPDATES, true),
        )
    }

    fun save(context: Context, settings: LauncherSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RENDERER, settings.renderer)
            .putString(KEY_RESOLUTION_SCALE, settings.resolutionScale)
            .putString(KEY_FRAME_LIMIT, settings.frameLimit)
            .putString(KEY_AUDIO_BACKEND, settings.audioBackend)
            .putString(KEY_AUDIO_BUFFER, settings.audioBuffer)
            .putString(KEY_PPU_DECODER, settings.ppuDecoder)
            .putString(KEY_SPU_DECODER, settings.spuDecoder)
            .putString(KEY_DEVICE_PRESET, settings.devicePreset)
            .putBoolean(KEY_SHADER_CACHE, settings.shaderCache)
            .putBoolean(KEY_VSYNC, settings.vSync)
            .putBoolean(KEY_AUTO_LANDSCAPE, settings.autoLandscape)
            .putBoolean(KEY_OVERLAY, settings.overlay)
            .putBoolean(KEY_HAPTICS, settings.haptics)
            .putBoolean(KEY_AUTO_UPDATES, settings.autoCheckUpdates)
            .apply()
    }

    fun applyRecommended(context: Context): LauncherSettings {
        return LauncherSettings(
            renderer = "Vulkan",
            resolutionScale = "100%",
            frameLimit = "Auto",
            audioBackend = "AAudio",
            audioBuffer = "Low latency",
            ppuDecoder = "LLVM recompiler",
            spuDecoder = "LLVM recompiler",
            devicePreset = "Balanced",
            shaderCache = true,
            vSync = true,
            autoLandscape = true,
            overlay = false,
            haptics = true,
            autoCheckUpdates = true,
        ).also { save(context, it) }
    }

    fun cycleRenderer(context: Context): LauncherSettings = load(context)
        .copy(renderer = renderers.nextAfter(load(context).renderer))
        .also { save(context, it) }

    fun cycleResolutionScale(context: Context): LauncherSettings = load(context)
        .copy(resolutionScale = scales.nextAfter(load(context).resolutionScale))
        .also { save(context, it) }

    fun cycleFrameLimit(context: Context): LauncherSettings = load(context)
        .copy(frameLimit = frameLimits.nextAfter(load(context).frameLimit))
        .also { save(context, it) }

    fun cycleAudioBackend(context: Context): LauncherSettings = load(context)
        .copy(audioBackend = audioBackends.nextAfter(load(context).audioBackend))
        .also { save(context, it) }

    fun cycleAudioBuffer(context: Context): LauncherSettings = load(context)
        .copy(audioBuffer = audioBuffers.nextAfter(load(context).audioBuffer))
        .also { save(context, it) }

    fun cyclePpuDecoder(context: Context): LauncherSettings = load(context)
        .copy(ppuDecoder = ppuDecoders.nextAfter(load(context).ppuDecoder))
        .also { save(context, it) }

    fun cycleSpuDecoder(context: Context): LauncherSettings = load(context)
        .copy(spuDecoder = spuDecoders.nextAfter(load(context).spuDecoder))
        .also { save(context, it) }

    fun cycleDevicePreset(context: Context): LauncherSettings = load(context)
        .copy(devicePreset = devicePresets.nextAfter(load(context).devicePreset))
        .also { save(context, it) }

    fun toggleShaderCache(context: Context): LauncherSettings = load(context)
        .copy(shaderCache = !load(context).shaderCache)
        .also { save(context, it) }

    fun toggleVSync(context: Context): LauncherSettings = load(context)
        .copy(vSync = !load(context).vSync)
        .also { save(context, it) }

    fun toggleAutoLandscape(context: Context): LauncherSettings = load(context)
        .copy(autoLandscape = !load(context).autoLandscape)
        .also { save(context, it) }

    fun toggleOverlay(context: Context): LauncherSettings = load(context)
        .copy(overlay = !load(context).overlay)
        .also { save(context, it) }

    fun toggleHaptics(context: Context): LauncherSettings = load(context)
        .copy(haptics = !load(context).haptics)
        .also { save(context, it) }

    fun toggleAutoUpdates(context: Context): LauncherSettings = load(context)
        .copy(autoCheckUpdates = !load(context).autoCheckUpdates)
        .also { save(context, it) }

    private fun List<String>.nextAfter(current: String): String {
        val currentIndex = indexOf(current).takeIf { it >= 0 } ?: 0
        return this[(currentIndex + 1) % size]
    }
}
