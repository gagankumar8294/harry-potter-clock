package com.yourname.harrypotterclock

import android.Manifest
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnSetWallpaper: Button
    private lateinit var btnFullScreenIntent: Button
    private lateinit var btnOverlay: Button
    private lateinit var btnNotification: Button
    private lateinit var btnBattery: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSetWallpaper = findViewById(R.id.btnSetWallpaper)
        btnFullScreenIntent = findViewById(R.id.btnFullScreenIntent)
        btnOverlay = findViewById(R.id.btnOverlayPermission)
        btnNotification = findViewById(R.id.btnNotificationPermission)
        btnBattery = findViewById(R.id.btnBatteryPermission)
        tvStatus = findViewById(R.id.tvStatus)

        btnSetWallpaper.setOnClickListener {
            openWallpaperPicker()
        }

        btnFullScreenIntent.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        btnNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        btnBattery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        startClockService()
    }

    private fun openWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, HarryPotterWallpaperService::class.java)
            )
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasFsi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            nm.canUseFullScreenIntent()
        } else true

        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val hasBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pm.isIgnoringBatteryOptimizations(packageName) else true

        btnFullScreenIntent.text = if (hasFsi) "Allow Full-Screen Intents: ALLOWED ✓" else "⚡ Enable Full-Screen Lock Takeover (Hides Moto Clock)"
        btnOverlay.text = if (hasOverlay) "1. Overlay Permission: GRANTED ✓" else "1. Allow 'Display Over Other Apps'"
        btnNotification.text = if (hasNotif) "2. Notification Permission: GRANTED ✓" else "2. Allow Notification Permission"
        btnBattery.text = if (hasBattery) "3. Battery Restriction: UNRESTRICTED ✓" else "3. Disable Battery Restrictions"

        if (hasFsi) {
            tvStatus.text = "⚡ Full-screen takeover enabled! Default Moto clock will be 100% hidden."
        } else {
            tvStatus.text = "Tap 'Enable Full-Screen Lock Takeover' to hide Moto's 7:07 clock!"
        }
    }

    private fun startClockService() {
        val serviceIntent = Intent(this, ScreenWatcherService::class.java)
        try {
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
