package it.jbalzano.graphics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
import kotlin.random.Random

/**
 * Created by Joseph on 10/05/2021.
 * Copyright (c) 2021 Balzano Joseph All rights reserved.
 */
private const val ATTRS_COLORS = "#00ff00,#ffff00,#ff0000"
private const val ATTRS_TICKS = ""
private const val ATTRS_ICONS = ""
private const val ATTRS_DELIMETER = ","

class DonutProgress(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val startAngle = -90

    private var strokeWidth = 4f
    private var min = 0
    private var max = 100

    var colors = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED)
        set(value) {
            field = value
            this.invalidate()
        }

    var ticks = floatArrayOf()
        set(value) {
            field = value
            this.invalidate()
        }

    var icons = mutableListOf<Bitmap?>()

    var overlap = false
        set(value) {
            field = value
            this.invalidate()
        }

    var tickIconSize = 48f

    val arcsParam = mutableListOf<ArcParams>()
    private val segment = mutableListOf<Triple<Float, Float, Paint>>()

    private var backColor = Color.GRAY
    private var rect: RectF = RectF()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }


    private fun init(context: Context, attrs: AttributeSet) =
        with(
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressDonut,
                0, 0
            )
        ) {
            strokeWidth = getDimension(
                R.styleable.ProgressDonut_thickness,
                strokeWidth
            )
            colors = (getString(R.styleable.ProgressDonut_colors) ?: ATTRS_COLORS)
                .split(ATTRS_DELIMETER)
                .map { Color.parseColor(it) }
                .toIntArray()

            ticks = (getString(R.styleable.ProgressDonut_ticks) ?: ATTRS_TICKS)
                .split(ATTRS_DELIMETER)
                .map { it.toFloat() }
                .toFloatArray()

            icons = (getString(R.styleable.ProgressDonut_icons) ?: ATTRS_ICONS)
                .takeIf { it != "" }
                ?.split(ATTRS_DELIMETER)
                ?.map { resources.getIdentifier(it, "drawable", context.packageName) }
                ?.map { it.createBitmap() }
                ?.toMutableList() ?: mutableListOf()

            backColor = getInt(R.styleable.ProgressDonut_bgcolor, backColor)
            min = getInt(R.styleable.ProgressDonut_min, min)
            max = getInt(R.styleable.ProgressDonut_max, max)
            overlap = getBoolean(R.styleable.ProgressDonut_overlapLast, overlap)

            configureBackgroundPaint()
            generateSegment()
            generateArch()

            recycle()
        }

    /**
     * Generate segment with paint and angle from tick
     */
    private fun generateSegment() =
        ticks.forEachIndexed { index, it ->
            segment.add(
                Triple(it, it.getAngle(),
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeWidth = this@DonutProgress.strokeWidth
                        color =
                            if (index <= colors.size - 1) colors[index]
                            else randomColor()
                    })
            )
        }

    /**
     * Generate params to draw arcs
     */
    private fun generateArch() =
        segment.forEachIndexed { index, triple ->
            val start =
                if (index == 0) startAngle.toFloat()
                else startAngle.toFloat() + segment[index - 1].second

            val end =
                if (index == 0) triple.second
                else triple.second - segment[index - 1].second

            arcsParam.add(ArcParams(start, end, triple.third))

            if (overlap && (index == segment.size - 1 && triple.first >= max)) {
                val onePoint = (max / 10).toFloat().getAngle()
                arcsParam.add(
                    0,
                    ArcParams(start + onePoint, end - onePoint, triple.third)
                )
            }
        }

    /**
     * Get point (x;y) for draw bitmap
     */
    private fun getIconPoint(value: Int): Pair<Float, Float> {
        val tick = 100 * ((value.toFloat() - min) / (max - min).toFloat())

        val rEndLine = ((rect.width() / 2))

        val a = (tick / 50f) * PI
        val m = tan((a + (PI / 2)))

        val b = sqrt((rEndLine * rEndLine) / ((m * m) + 1))

        return if (tick.toInt() == 0)
            Pair(
                (rect.centerX()),
                (rect.centerY() - rEndLine)
            )
        else if (0 < tick && tick < 50)
            Pair(
                (rect.centerX() + b).toFloat(),
                (rect.centerY() + (m * b)).toFloat()
            )
        else if (tick.toInt() == 50)
            Pair(
                (rect.centerX()),
                (rect.centerY() + rEndLine)
            )
        else
            Pair(
                (rect.centerX() - b).toFloat(),
                (rect.centerY() - (m * b)).toFloat()
            )
    }

    private fun configureBackgroundPaint() =
        backgroundPaint.apply {
            strokeWidth = this@DonutProgress.strokeWidth
            color = backColor
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height =
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width =
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        val min = min(width, height)

        setMeasuredDimension(min, min)
        rect[0 + strokeWidth / 2,
                0 + strokeWidth / 2,
                min - strokeWidth / 2] = min - strokeWidth / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawOval(rect, backgroundPaint)

        // Draw segments reversed
        arcsParam.reversed().forEach {
            canvas.drawArc(rect, it.startAngle, it.sweepAngle, false, it.paint)
        }

        // Draw bitmap into segment
        if (icons.size > 0)
            ticks.forEachIndexed { index, tick ->
                val startSegment =
                    if (index == 0) 0f
                    else ticks[index - 1]

                icons[index]?.let {
                    getIconPoint(((startSegment + tick) / 2).toInt())
                        .let { point ->
                            canvas.drawBitmap(
                                it,
                                point.first - (tickIconSize / 2),
                                point.second - (tickIconSize / 2),
                                null
                            )
                        }
                }
            }
    }

    private fun randomColor() = Color.argb(
        255,
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )

    // region Extension
    /**
     * Generate angle from float value
     * This function handle max
     */
    private fun Float.getAngle() =
        if (this > max) 360f
        else 360 * this / max

    /**
     * Correct alpha value for Integer ColorId
     */
    private fun Int.adjustAlpha(factor: Float): Int =
        Color.argb(
            (Color.alpha(this) * factor).roundToInt(),
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )

    /**
     * Generate Bitmap from Integer ResId
     */
    private fun Int.createBitmap(): Bitmap? =
        resources.getDrawable(this)?.let {
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(
                it.intrinsicWidth,
                it.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            canvas.setBitmap(bitmap)
            it.setBounds(0, 0, tickIconSize.toInt(), tickIconSize.toInt())
            it.draw(canvas)
            bitmap
        }
    // endregion

    // region Private Data Classes
    data class ArcParams(
        val startAngle: Float,
        val sweepAngle: Float,
        val paint: Paint
    )
    // endregion

    init {
        init(context, attrs)
    }
}