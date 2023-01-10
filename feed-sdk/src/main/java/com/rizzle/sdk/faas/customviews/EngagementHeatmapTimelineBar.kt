package com.rizzle.sdk.faas.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.helpers.dpToPx
import com.rizzle.sdk.faas.models.Heatmap
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.utils.InternalUtils.color
import kotlin.math.min

/**
 * View used as timeline bar along with lottie animations shown at bottom on feed.
 * */
class EngagementHeatmapTimelineBar(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private val seekLineWidth: Float = 2f.dpToPx.toFloat()
    private val progressBarWidth: Float = 5f.dpToPx.toFloat()

    private var seekLineColor = Color.WHITE
    private var barBaseColor = color(R.color.gray_dark_alt)

    private val seekLinePaint = Paint()
    private val progressBarPaint = Paint()

    private var mViewStart: Float = 0f
    private var mViewEnd: Float = 0f
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    //Stores the shader object for the heatmap timeline bar
    private var heatmapShader: LinearGradient? = null

    //Stores list of heatmap actions present for the current post
    private var heatmapActions: MutableList<Heatmap> = mutableListOf()

    //Stores the total duration of the current visible post
    private var currentPostDuration: Long? = null

    //Stores the value for amount of pixels per millisecond, used for calculating drawing area
    private val pixelPerMilliSec: Float?
        get() = currentPostDuration?.let { mViewWidth.div(it.toFloat()) }

    //List of colours to be plotted on the heatmap timeline bar
    private val colors = mutableListOf<Int>()

    //List of positions where the colours are to plotted.
    private val positions = mutableListOf<Float>()

    //Holds the latest position of the seek line
    private var seekLinePosition = 0f
        set(value) {
            field = value  //place line as per percentage value
            invalidate()
        }

    init {
        //paint object for seek line
        with(seekLinePaint) {
            color = seekLineColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = seekLineWidth
            isAntiAlias = true
        }

        //paint object for the heatmap timeline bar
        with(progressBarPaint) {
            color = barBaseColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = progressBarWidth
            isAntiAlias = true
        }
    }

    /**
     * Here, the scale value of the seekLine will be converted to position in terms of pixels
     */
    fun setProgress(position: Float) {
        seekLinePosition = min(position.div(currentPostDuration?.toFloat() ?: 1F), 1F) * mViewEnd + mViewStart
    }

    /**
     * Set data for heatmap view as per current post
     */
    fun setData(post: Post) {
        currentPostDuration = post.video?.duration?.toLong()
        heatmapActions.clear()
        post.heatmap?.let {
            heatmapActions.addAll(it)
            heatmapActions.sortBy { heatmap -> heatmap.startTime }
        }
        heatmapShader = null
        invalidate()

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        mViewEnd = mViewWidth.toFloat() - mViewStart
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawProgressBar(canvas)
        drawSeekLine(canvas)
    }


    private fun drawProgressBar(canvas: Canvas) {
        progressBarPaint.shader = null

        //draw base rect for complete width of custom view
        progressBarPaint.color = color(R.color.gray_dark_alt)
        canvas.drawRect(0F, mViewHeight.toFloat() - 0.5F.dpToPx.toFloat(), mViewEnd, 0.5F.dpToPx.toFloat(), progressBarPaint)

        //index of the current heatmap action
        var index = 0

        colors.clear()
        positions.clear()

        while (index < heatmapActions.size) {
            val heatmap = heatmapActions[index]

            if (heatmap.duration == null || heatmap.startTime == null || pixelPerMilliSec == null ||
                (index - 1 > 0 && (heatmapActions[index - 1].startTime == null || heatmapActions[index - 1].duration == null))
            )
            else {
                val rectStart = pixelPerMilliSec!! * heatmap.startTime!!

                when {
                    index == 0 && heatmap.startTime!! > 0 -> {
                        //having no action color before adding the first heatmap action color for cases when start-time is after the start of the view
                        addShaderColor(color(R.color.transparent), 0F)
                        addShaderColor(color(R.color.transparent), rectStart.div(mViewWidth))
                        addShaderColor(Color.parseColor(getColor(heatmap)), (rectStart + (pixelPerMilliSec!! * heatmap.duration!!) / 2).div(mViewWidth))
                    }

                    (index - 1 >= 0 && heatmap.startTime!!.minus(heatmapActions[index - 1].startTime!! + heatmapActions[index - 1].duration!!) > 0) -> {
                        //having no action color before adding the heatmap action color for cases when start-time of current heatmap is not right after the end of the previous
                        addShaderColor(
                            color(R.color.transparent),
                            ((pixelPerMilliSec!! * heatmapActions[index - 1].startTime!!) + (pixelPerMilliSec!! * heatmapActions[index - 1].duration!!)).div(mViewWidth)
                        )
                        addShaderColor(color(R.color.transparent), rectStart.div(mViewWidth))
                        addShaderColor(Color.parseColor(getColor(heatmap)), (rectStart + (pixelPerMilliSec!! * heatmap.duration!!) / 2).div(mViewWidth))
                    }
                    else -> {
                        //adding only the heatmap action color for cases when there are consecutive heatmap actions
                        addShaderColor(Color.parseColor(getColor(heatmap)), rectStart.div(mViewWidth))
                    }
                }
            }
            index += 1
        }

        //adding base color at the end of the heatmap timeline bar when the heatmap actions end before the total width of the view
        if (heatmapActions.size > 0) {
            val heatmapActionsEnd = (pixelPerMilliSec!! * heatmapActions[heatmapActions.size - 1].startTime!!) + (pixelPerMilliSec!! * heatmapActions[heatmapActions.size - 1].duration!!)
            if (heatmapActionsEnd < mViewEnd)
                addShaderColor(color(R.color.transparent), heatmapActionsEnd.div(mViewWidth))
        }

        if (colors.isNotEmpty() && positions.isNotEmpty()) {
            progressBarPaint.setShader()
            canvas.drawRect(0F, mViewHeight.toFloat() - 0.5F.dpToPx.toFloat(), mViewEnd, 0.5F.dpToPx.toFloat(), progressBarPaint)
        }
    }


    /**
     * Setting paint shader to [heatmapShader] if already created else creating as per the [heatmapActions] and assigning to [heatmapShader] for reusing.
     */
    private fun Paint.setShader() {
        if (heatmapShader == null)
            heatmapShader = LinearGradient(
                0F, mViewHeight.toFloat() - 0.5F.dpToPx.toFloat(), mViewEnd, 0.5F.dpToPx.toFloat(), colors.toIntArray(),
                positions.toFloatArray(), Shader.TileMode.MIRROR
            )
        shader = heatmapShader
    }

    /**
     * Getting the colour for the current heatmap action based on the [Heatmap.hexColorCode] and [Heatmap.opacity]
     * */
    private fun getColor(heatmap: Heatmap): String {
        return "#${Integer.toHexString(heatmap.opacity?.div(100F)?.times(255)?.toInt() ?: 1).padStart(2, '0')}${heatmap.hexColorCode}"
    }

    /**
     * Add color and position to lists to be used in creating the shader object
     * */
    private fun addShaderColor(color: Int, position: Float) {
        colors.add(color)
        positions.add(position)
    }

    private fun drawSeekLine(canvas: Canvas) {
        canvas.drawLine(
            seekLinePosition, mViewStart, seekLinePosition, mViewHeight.toFloat(), seekLinePaint
        )
    }


}