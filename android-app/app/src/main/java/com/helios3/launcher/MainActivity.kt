package com.helios3.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private var lastUpdateLine: String = ""

    private val firmwarePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            FirmwareRepository.rememberPickedFirmware(this, uri)
            val firmware = FirmwareRepository.load(this)
            lastUpdateLine = getString(
                R.string.firmware_loaded,
                firmware.displayName ?: "PS3UPDAT.PUP",
            )
            updateStatus(lastUpdateLine)
            toast(lastUpdateLine)
        }
    }

    private val driverPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            DriverRepository.rememberCustomDriver(this, uri)
            val driver = DriverRepository.load(this)
            lastUpdateLine = getString(
                R.string.driver_loaded,
                driver.packageName ?: getString(R.string.driver_custom_label),
            )
            updateStatus(lastUpdateLine)
            toast(lastUpdateLine)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastUpdateLine = getString(R.string.checking_updates)
        updateStatus(lastUpdateLine)

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            showGlobalSettingsDialog()
        }
        findViewById<Button>(R.id.firmwareButton).setOnClickListener {
            showFirmwareDialog()
        }
        findViewById<Button>(R.id.driverButton).setOnClickListener {
            showDriverDialog()
        }
        findViewById<Button>(R.id.syncSourceButton).setOnClickListener {
            showLibraryDialog()
        }
        findViewById<Button>(R.id.installButton).setOnClickListener {
            showInstallDialog()
        }
        findViewById<Button>(R.id.startButton).setOnClickListener {
            showLaunchDialog()
        }
        findViewById<Button>(R.id.launchButton).setOnClickListener {
            showGraphicsDialog()
        }
        findViewById<Button>(R.id.audioButton).setOnClickListener {
            showAudioDialog()
        }
        findViewById<Button>(R.id.systemButton).setOnClickListener {
            showSystemDialog()
        }
        findViewById<Button>(R.id.gamepadButton).setOnClickListener {
            showInputDialog()
        }
        findViewById<Button>(R.id.doctorButton).setOnClickListener {
            showDiagnosticsDialog()
        }
        findViewById<Button>(R.id.repoButton).setOnClickListener {
            openUrl("https://github.com/aprowaz1-crypto/olympus-update-")
        }
        findViewById<Button>(R.id.checkUpdatesButton).setOnClickListener {
            refreshUpdateState(showDialog = true)
        }

        val settings = SettingsRepository.load(this)
        refreshUpdateState(showDialog = settings.autoCheckUpdates)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (isGamepadSource(event.source)) {
            NativeBridge.onGamepadState(
                axes = floatArrayOf(
                    event.getAxisValue(MotionEvent.AXIS_X),
                    event.getAxisValue(MotionEvent.AXIS_Y),
                    event.getAxisValue(MotionEvent.AXIS_Z),
                    event.getAxisValue(MotionEvent.AXIS_RZ),
                    event.getAxisValue(MotionEvent.AXIS_LTRIGGER),
                    event.getAxisValue(MotionEvent.AXIS_RTRIGGER),
                ),
                buttons = collectButtons(event),
            )
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (isGamepadSource(event.source)) {
            NativeBridge.onGamepadState(FloatArray(6), collectButtons(event))
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (isGamepadSource(event.source)) {
            NativeBridge.onGamepadState(FloatArray(6), collectButtons(event))
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun showGlobalSettingsDialog() {
        val settings = SettingsRepository.load(this)
        val items = arrayOf(
            getString(R.string.graphics_settings),
            getString(R.string.audio_settings),
            getString(R.string.system_settings),
            getString(R.string.input_settings),
            getString(R.string.driver_settings),
            getString(R.string.apply_recommended),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_title)
            .setMessage(settings.summaryLines().joinToString("\n"))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showGraphicsDialog()
                    1 -> showAudioDialog()
                    2 -> showSystemDialog()
                    3 -> showInputDialog()
                    4 -> showDriverDialog()
                    else -> {
                        SettingsRepository.applyRecommended(this)
                        updateStatus(lastUpdateLine)
                        toast(getString(R.string.settings_saved))
                    }
                }
            }
            .show()
    }

    private fun showFirmwareDialog() {
        val firmware = FirmwareRepository.load(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.firmware_title)
            .setMessage(
                getString(
                    R.string.firmware_message,
                    firmware.summaryLine(),
                ),
            )
            .setPositiveButton(R.string.import_firmware) { _, _ ->
                firmwarePicker.launch(arrayOf("*/*"))
            }
            .setNeutralButton(R.string.open_official_firmware) { _, _ ->
                openUrl("https://www.playstation.com/en-us/support/hardware/ps3/system-software/")
            }
            .setNegativeButton(R.string.clear_firmware) { _, _ ->
                FirmwareRepository.clear(this)
                updateStatus(lastUpdateLine)
                toast(getString(R.string.firmware_cleared))
            }
            .show()
    }

    private fun showLibraryDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.library_title)
            .setMessage(R.string.library_message)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.view_release_checklist) { _, _ ->
                openUrl("https://github.com/aprowaz1-crypto/olympus-update-/blob/main/RELEASE_0_1.md")
            }
            .show()
    }

    private fun showInstallDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.install_title)
            .setMessage(R.string.install_message)
            .setPositiveButton(R.string.view_release_checklist) { _, _ ->
                openUrl("https://github.com/aprowaz1-crypto/olympus-update-/blob/main/PORTING.md")
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showLaunchDialog() {
        val settings = SettingsRepository.load(this)
        val firmware = FirmwareRepository.load(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.launch_title)
            .setMessage(
                getString(
                    R.string.launch_message,
                    settings.renderer,
                    settings.resolutionScale,
                    settings.frameLimit,
                ),
            )
            .setPositiveButton(R.string.start_native_preview) { _, _ ->
                if (!firmware.installed) {
                    toast(getString(R.string.firmware_missing_launch))
                    showFirmwareDialog()
                    return@setPositiveButton
                }
                lastUpdateLine = getString(R.string.native_launch_ready)
                updateStatus(lastUpdateLine)
                toast(getString(R.string.native_launch_ready))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showGraphicsDialog() {
        val actions = arrayOf(
            getString(R.string.action_cycle_renderer),
            getString(R.string.action_cycle_resolution),
            getString(R.string.action_cycle_frame_limit),
            getString(R.string.action_toggle_shader_cache),
            getString(R.string.action_toggle_vsync),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.graphics_title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> SettingsRepository.cycleRenderer(this)
                    1 -> SettingsRepository.cycleResolutionScale(this)
                    2 -> SettingsRepository.cycleFrameLimit(this)
                    3 -> SettingsRepository.toggleShaderCache(this)
                    else -> SettingsRepository.toggleVSync(this)
                }
                updateStatus(lastUpdateLine)
                toast(getString(R.string.settings_saved))
            }
            .show()
    }

    private fun showAudioDialog() {
        val actions = arrayOf(
            getString(R.string.action_cycle_audio_backend),
            getString(R.string.action_cycle_audio_buffer),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.audio_title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> SettingsRepository.cycleAudioBackend(this)
                    else -> SettingsRepository.cycleAudioBuffer(this)
                }
                updateStatus(lastUpdateLine)
                toast(getString(R.string.settings_saved))
            }
            .show()
    }

    private fun showSystemDialog() {
        val actions = arrayOf(
            getString(R.string.action_cycle_device_preset),
            getString(R.string.action_cycle_ppu_decoder),
            getString(R.string.action_cycle_spu_decoder),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.system_title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> SettingsRepository.cycleDevicePreset(this)
                    1 -> SettingsRepository.cyclePpuDecoder(this)
                    else -> SettingsRepository.cycleSpuDecoder(this)
                }
                updateStatus(lastUpdateLine)
                toast(getString(R.string.settings_saved))
            }
            .show()
    }

    private fun showDriverDialog() {
        val driver = DriverRepository.load(this)
        val actions = arrayOf(
            getString(R.string.action_cycle_driver_profile),
            getString(R.string.import_driver),
            getString(R.string.clear_driver),
            getString(R.string.open_driver_help),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.driver_title)
            .setMessage(driver.summaryLine())
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> {
                        DriverRepository.cycleMode(this)
                        updateStatus(lastUpdateLine)
                        toast(getString(R.string.settings_saved))
                    }
                    1 -> driverPicker.launch(arrayOf("*/*"))
                    2 -> {
                        DriverRepository.clearCustomDriver(this)
                        updateStatus(lastUpdateLine)
                        toast(getString(R.string.driver_cleared))
                    }
                    else -> openUrl("https://github.com/K11MCH1/AdrenoToolsDrivers")
                }
            }
            .show()
    }

    private fun showInputDialog() {
        val actions = arrayOf(
            getString(R.string.action_toggle_overlay),
            getString(R.string.action_toggle_haptics),
            getString(R.string.action_toggle_auto_updates),
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.input_title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> SettingsRepository.toggleOverlay(this)
                    1 -> SettingsRepository.toggleHaptics(this)
                    else -> SettingsRepository.toggleAutoUpdates(this)
                }
                updateStatus(lastUpdateLine)
                toast(getString(R.string.settings_saved))
            }
            .show()
    }

    private fun showDiagnosticsDialog() {
        val settings = SettingsRepository.load(this)
        val firmware = FirmwareRepository.load(this)
        val driver = DriverRepository.load(this)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.diagnostics_title)
            .setMessage(
                getString(
                    R.string.diagnostics_message,
                    NativeBridge.getCoreStatusSafe(),
                    NativeBridge.getBuildInfoSafe(),
                    firmware.summaryLine(),
                    driver.summaryLine(),
                    settings.summaryLines().joinToString("\n"),
                ),
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun refreshUpdateState(showDialog: Boolean) {
        UpdateChecker.checkForUpdates(this) { state ->
            lastUpdateLine = state.message
            updateStatus(lastUpdateLine)

            if (showDialog && state.updateAvailable && state.latestVersion != null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.update_title))
                    .setMessage(getString(R.string.update_message, state.latestVersion))
                    .setPositiveButton(R.string.update_now) { _, _ ->
                        UpdateChecker.rememberVersion(this, state.latestVersion)
                        openUrl("https://github.com/RPCS3/rpcs3-binaries-linux-arm64/releases/latest")
                    }
                    .setNeutralButton(R.string.view_release_checklist) { _, _ ->
                        openUrl("https://github.com/aprowaz1-crypto/olympus-update-/blob/main/RELEASE_0_1.md")
                    }
                    .setNegativeButton(R.string.update_later, null)
                    .show()
            }
        }
    }

    private fun updateStatus(updateLine: String) {
        val settings = SettingsRepository.load(this)
        val firmware = FirmwareRepository.load(this)
        val driver = DriverRepository.load(this)
        findViewById<TextView>(R.id.statusText).text = listOf(
            getString(R.string.status_ready),
            "",
            NativeBridge.getCoreStatusSafe(),
            NativeBridge.getBuildInfoSafe(),
            firmware.summaryLine(),
            driver.summaryLine(),
            "",
            getString(
                R.string.current_profile,
                settings.renderer,
                settings.resolutionScale,
                settings.frameLimit,
            ),
            settings.summaryLines().joinToString("\n"),
            "",
            updateLine,
        ).joinToString("\n")
    }

    private fun isGamepadSource(source: Int): Boolean {
        return source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
            source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
    }

    private fun collectButtons(event: KeyEvent): IntArray {
        fun pressed(code: Int): Int {
            return if (event.keyCode == code && event.action == KeyEvent.ACTION_DOWN) 1 else 0
        }

        return intArrayOf(
            pressed(KeyEvent.KEYCODE_BUTTON_A),
            pressed(KeyEvent.KEYCODE_BUTTON_B),
            pressed(KeyEvent.KEYCODE_BUTTON_X),
            pressed(KeyEvent.KEYCODE_BUTTON_Y),
        )
    }

    private fun collectButtons(event: MotionEvent): IntArray {
        return intArrayOf(
            if (event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) 1 else 0,
            if (event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)) 1 else 0,
            if (event.isButtonPressed(MotionEvent.BUTTON_TERTIARY)) 1 else 0,
            if (event.isButtonPressed(MotionEvent.BUTTON_BACK)) 1 else 0,
        )
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            toast(getString(R.string.browser_missing))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
