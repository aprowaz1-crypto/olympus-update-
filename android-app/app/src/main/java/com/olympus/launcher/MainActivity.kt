package com.olympus.launcher

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateStatus(getString(R.string.checking_updates))

        bindCommand(R.id.syncSourceButton, "cd ~/olympus-update- && bash tools/import-rpcs3.sh")
        bindCommand(R.id.installButton, "cd ~/olympus-update- && bash install.sh")
        bindCommand(R.id.startButton, "olympus-start")
        bindCommand(R.id.launchButton, "olympus-rpcs3")
        bindCommand(R.id.gamepadButton, "olympus-gamepad-fix")
        bindCommand(R.id.doctorButton, "olympus-doctor")

        findViewById<Button>(R.id.repoButton).setOnClickListener {
            openUrl("https://github.com/aprowaz1-crypto/olympus-update-")
        }

        findViewById<Button>(R.id.checkUpdatesButton).setOnClickListener {
            refreshUpdateState(showDialog = true)
            if (!runInTermux("olympus-check-updates")) {
                copyToClipboard("olympus-check-updates")
                openTermux()
                toast(getString(R.string.termux_fallback_toast))
            }
        }

        refreshUpdateState(showDialog = true)
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

    private fun bindCommand(buttonId: Int, command: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            if (!runInTermux(command)) {
                copyToClipboard(command)
                openTermux()
                toast(getString(R.string.termux_fallback_toast))
            }
        }
    }

    private fun refreshUpdateState(showDialog: Boolean) {
        UpdateChecker.checkForUpdates(this) { state ->
            updateStatus(state.message)

            if (showDialog && state.updateAvailable && state.latestVersion != null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.update_title))
                    .setMessage(getString(R.string.update_message, state.latestVersion))
                    .setPositiveButton(R.string.update_now) { _, _ ->
                        UpdateChecker.rememberVersion(this, state.latestVersion)
                        runInTermux("cd ~/olympus-update- && bash install.sh")
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
        findViewById<TextView>(R.id.statusText).text = listOf(
            getString(R.string.status_ready),
            "",
            NativeBridge.getCoreStatusSafe(),
            NativeBridge.getBuildInfoSafe(),
            "",
            updateLine,
        ).joinToString("\n")
    }

    private fun runInTermux(command: String): Boolean {
        return try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setClassName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-lc", command))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            }
            startService(intent)
            toast(getString(R.string.command_sent, command))
            true
        } catch (_: Exception) {
            false
        }
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

    private fun openTermux() {
        val launchIntent = packageManager.getLaunchIntentForPackage("com.termux")
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            toast(getString(R.string.termux_missing))
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            toast(getString(R.string.browser_missing))
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Olympus command", text))
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
