package com.yourname.harrypotterclock

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class HarryPotterClockView(context: Context) : View(context) {

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()

    private var cx = 0f
    private var cy = 0f

    // Draw loop handler (~60fps)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val drawRunnable = object : Runnable {
        override fun run() {
            invalidate()
            mainHandler.postDelayed(this, 16L)
        }
    }

    // Raw & Scaled PNG Bitmaps
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

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        textPaint.apply {
            color = Color.parseColor("#E8D5A3")
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(8f, 2f, 4f, Color.BLACK)
        }

        loadRawAssets()
    }

    private fun loadRawAssets() {
        try {
            val res = resources
            val pkg = context.packageName

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        cx = w / 2f
        cy = h * 0.40f

        scaleAssetsForScreen(w, h)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mainHandler.post(drawRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mainHandler.removeCallbacks(drawRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (scaledDial != null) {
            canvas.drawBitmap(scaledDial!!, 0f, 0f, mainPaint)
        } else {
            drawFallbackBackground(canvas)
        }

        val cal = Calendar.getInstance()
        val ms = cal.get(Calendar.MILLISECOND)
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE)
        val hr = cal.get(Calendar.HOUR)

        val secAngle = (sec + ms / 1000f) * 6f
        val minAngle = (min + sec / 60f) * 6f
        val hourAngle = (hr + min / 60f) * 30f

        if (scaledHour != null) {
            drawRotatedHand(canvas, scaledHour!!, hourAngle, cx, cy)
        } else {
            drawFallbackHourHand(canvas, hourAngle)
        }

        if (scaledMin != null) {
            drawRotatedHand(canvas, scaledMin!!, minAngle, cx, cy)
        } else {
            drawFallbackMinuteHand(canvas, minAngle)
        }

        if (scaledSec != null) {
            drawRotatedHand(canvas, scaledSec!!, secAngle, cx, cy)
        } else {
            drawFallbackSecondHand(canvas, secAngle)
        }

        if (scaledCap != null) {
            canvas.drawBitmap(
                scaledCap!!,
                cx - scaledCap!!.width / 2f,
                cy - scaledCap!!.height / 2f,
                mainPaint
            )
        } else {
            drawFallbackCap(canvas)
        }

        val dateStr = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
        canvas.drawText(dateStr, cx, height * 0.83f, textPaint)
    }

    private fun drawRotatedHand(canvas: Canvas, bmp: Bitmap, angleDeg: Float, pivotX: Float, pivotY: Float) {
        matrix.reset()
        matrix.postTranslate(-bmp.width / 2f, -bmp.height * 0.85f)
        matrix.postRotate(angleDeg)
        matrix.postTranslate(pivotX, pivotY)
        canvas.drawBitmap(bmp, matrix, mainPaint)
    }

    private fun drawFallbackBackground(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0B0813"))
        mainPaint.style = Paint.Style.STROKE
        mainPaint.strokeWidth = 6f
        mainPaint.color = Color.parseColor("#C5A059")
        canvas.drawCircle(cx, cy, width * 0.40f, mainPaint)
    }

    private fun drawFallbackHourHand(canvas: Canvas, angle: Float) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        mainPaint.color = Color.parseColor("#C5A059")
        mainPaint.style = Paint.Style.FILL
        canvas.drawRect(cx - 8f, cy - width * 0.22f, cx + 8f, cy + 20f, mainPaint)
        canvas.restore()
    }

    private fun drawFallbackMinuteHand(canvas: Canvas, angle: Float) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        mainPaint.color = Color.parseColor("#FFD700")
        mainPaint.style = Paint.Style.FILL
        canvas.drawRect(cx - 5f, cy - width * 0.32f, cx + 5f, cy + 25f, mainPaint)
        canvas.restore()
    }

    private fun drawFallbackSecondHand(canvas: Canvas, angle: Float) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        mainPaint.color = Color.parseColor("#FF4500")
        mainPaint.style = Paint.Style.FILL
        canvas.drawRect(cx - 2f, cy - width * 0.36f, cx + 2f, cy + 30f, mainPaint)
        canvas.restore()
    }

    private fun drawFallbackCap(canvas: Canvas) {
        mainPaint.color = Color.parseColor("#FFD700")
        mainPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, 24f, mainPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (context is HarryPotterLockActivity && event.action == MotionEvent.ACTION_UP) {
            (context as HarryPotterLockActivity).unlockDevice()
        }
        return false
    }
}
