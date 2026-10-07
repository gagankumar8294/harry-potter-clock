package com.yourname.harrypotterclock

import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.text.SimpleDateFormat
import java.util.*

class HarryPotterWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ClockWallpaperEngine()

    inner class ClockWallpaperEngine : Engine() {

        private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val matrix = Matrix()

        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false

        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (isVisible) {
                    handler.postDelayed(this, 16L) // ~60 FPS
                }
            }
        }

        private var cx = 0f
        private var cy = 0f
        private var width = 0
        private var height = 0

        // Bitmaps
        private var rawDial: Bitmap? = null
        private var rawHour: Bitmap? = null
        private var rawMin: Bitmap? = null
        private var rawSec: Bitmap? = null
        private var rawCap: Bitmap? = null

        private var scaledDial: Bitmap? = null
        private var scaledHour: Bitmap? = null
        private var scaledMin: Bitmap? = null
        private var scaledSec: Bitmap? = null
        private var scaledCap: Bitmap? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            // High contrast & luminescence color matrix
            val cm = ColorMatrix().apply {
                set(floatArrayOf(
                    1.35f, 0f, 0f, 0f, 25f,
                    0f, 1.35f, 0f, 0f, 25f,
                    0f, 0f, 1.35f, 0f, 25f,
                    0f, 0f, 0f, 1.0f, 0f
                ))
            }
            mainPaint.colorFilter = ColorMatrixColorFilter(cm)

            textPaint.apply {
                color = Color.parseColor("#FFF2A8")
                textSize = 50f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                setShadowLayer(14f, 0f, 0f, Color.parseColor("#FFD700"))
            }

            loadRawAssets()
        }

        private fun loadRawAssets() {
            try {
                val res = resources
                val pkg = packageName

                val dialId = res.getIdentifier("dial_background", "drawable", pkg)
                val hourId = res.getIdentifier("hand_hour", "drawable", pkg)
                val minId = res.getIdentifier("hand_minute", "drawable", pkg)
                val secId = res.getIdentifier("hand_second", "drawable", pkg)
                val capId = res.getIdentifier("center_cap", "drawable", pkg)

                if (dialId != 0) rawDial = BitmapFactory.decodeResource(res, dialId)
                if (hourId != 0) rawHour = BitmapFactory.decodeResource(res, hourId)
                if (minId != 0) rawMin = BitmapFactory.decodeResource(res, minId)
                if (secId != 0) rawSec = BitmapFactory.decodeResource(res, secId)
                if (capId != 0) rawCap = BitmapFactory.decodeResource(res, capId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w
            height = h
            cx = w / 2f
            cy = h * 0.48f // Positioned in lower-middle section to guarantee zero overlap with top system clock

            scaleAssetsForScreen(w, h)
            drawFrame()
        }

        private fun scaleAssetsForScreen(w: Int, h: Int) {
            rawDial?.let { bmp ->
                scaledDial = Bitmap.createScaledBitmap(bmp, w, h, true)
            }
            rawHour?.let { bmp ->
                val targetH = (w * 0.44f).toInt()
                val targetW = ((bmp.width.toFloat() / bmp.height) * targetH).toInt().coerceAtLeast(10)
                scaledHour = Bitmap.createScaledBitmap(bmp, targetW, targetH, true)
            }
            rawMin?.let { bmp ->
                val targetH = (w * 0.62f).toInt()
                val targetW = ((bmp.width.toFloat() / bmp.height) * targetH).toInt().coerceAtLeast(10)
                scaledMin = Bitmap.createScaledBitmap(bmp, targetW, targetH, true)
            }
            rawSec?.let { bmp ->
                val targetH = (w * 0.70f).toInt()
                val targetW = ((bmp.width.toFloat() / bmp.height) * targetH).toInt().coerceAtLeast(10)
                scaledSec = Bitmap.createScaledBitmap(bmp, targetW, targetH, true)
            }
            rawCap?.let { bmp ->
                val targetSize = (w * 0.16f).toInt()
                scaledCap = Bitmap.createScaledBitmap(bmp, targetSize, targetSize, true)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderClock(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun renderClock(canvas: Canvas) {
            if (scaledDial != null) {
                canvas.drawBitmap(scaledDial!!, 0f, 0f, mainPaint)
            } else {
                canvas.drawColor(Color.parseColor("#0B0813"))
            }

            val cal = Calendar.getInstance()
            val ms = cal.get(Calendar.MILLISECOND)
            val sec = cal.get(Calendar.SECOND)
            val min = cal.get(Calendar.MINUTE)
            val hr = cal.get(Calendar.HOUR)

            val secAngle = (sec + ms / 1000f) * 6f
            val minAngle = (min + sec / 60f) * 6f
            val hourAngle = (hr + min / 60f) * 30f

            scaledHour?.let { drawRotatedHand(canvas, it, hourAngle, cx, cy) }
            scaledMin?.let { drawRotatedHand(canvas, it, minAngle, cx, cy) }
            scaledSec?.let { drawRotatedHand(canvas, it, secAngle, cx, cy) }

            scaledCap?.let {
                canvas.drawBitmap(it, cx - it.width / 2f, cy - it.height / 2f, mainPaint)
            }

            val dateStr = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            canvas.drawText(dateStr, cx, height * 0.86f, textPaint)
        }

        private fun drawRotatedHand(canvas: Canvas, bmp: Bitmap, angleDeg: Float, pivotX: Float, pivotY: Float) {
            matrix.reset()
            matrix.postTranslate(-bmp.width / 2f, -bmp.height * 0.85f)
            matrix.postRotate(angleDeg)
            matrix.postTranslate(pivotX, pivotY)
            canvas.drawBitmap(bmp, matrix, mainPaint)
        }
    }
}
