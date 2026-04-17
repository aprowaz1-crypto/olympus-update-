package com.olympus.launcher

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.statusText).text = listOf(
            getString(R.string.status_ready),
            "",
            NativeBridge.getCoreStatusSafe(),
            NativeBridge.getBuildInfoSafe()
        ).joinToString("\n")

        bindCommand(R.id.syncSourceButton, "cd ~/olympus-update- && bash tools/import-rpcs3.sh")
        bindCommand(R.id.installButton, "cd ~/olympus-update- && bash install.sh")
        bindCommand(R.id.startButton, "olympus-start")
        bindCommand(R.id.launchButton, "olympus-rpcs3")
        bindCommand(R.id.gamepadButton, "olympus-gamepad-fix")
        bindCommand(R.id.doctorButton, "olympus-doctor")

        findViewById<Button>(R.id.repoButton).setOnClickListener {
            openUrl("https://github.com/aprowaz1-crypto/olympus-update-")
        }
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
