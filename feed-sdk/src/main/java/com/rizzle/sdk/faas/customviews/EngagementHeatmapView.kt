package com.rizzle.sdk.faas.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.helpers.animationEnd
import com.rizzle.sdk.faas.models.Heatmap
import com.rizzle.sdk.faas.models.Post
import kotlin.math.min


class EngagementHeatmapView : ConstraintLayout {

    private var animationsContainer: FrameLayout? = null
    private var heatmapTimelineBar: EngagementHeatmapTimelineBar? = null

    //Stores the index of which the heatmap animation is to be shown
    private var animationIndex: Int = 0

    //Stores list of heatmap actions present for the current post
    private val heatmapActions: MutableList<Heatmap> = mutableListOf()

    //Stores the total duration of the current visible post
    private var currentPostDuration: Long? = null

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    companion object {
        private const val ANIMATION_POSITION_ADJUSTMENT = 90
    }

    private fun initialize() {
        val root = View.inflate(context, R.layout.layout_engagement_heatmap, this)
        heatmapTimelineBar = root.findViewById(R.id.engagement_heatmap_timeline_bar)
        animationsContainer = root.findViewById(R.id.animations_container)
    }

    fun setProgress(position: Float) {
        //Starting animation for a heatmap value if current progress is >= to the heatmap.startTime of the current animationIndex
        if (animationIndex < heatmapActions.size)
            heatmapActions[animationIndex].startTime?.let {
                if (position >= it) {
                    startEngagementIconAnimation(position, heatmapActions[animationIndex])
                    animationIndex++
                }
            }
        heatmapTimelineBar?.setProgress(position)
    }

    fun setData(post: Post) {
        currentPostDuration = post.video?.duration?.toLong()
        heatmapActions.clear()
        post.heatmap?.let {
            heatmapActions.addAll(it)
            heatmapActions.sortBy { heatmap -> heatmap.startTime }
        }
        heatmapTimelineBar?.setData(post)
    }


    /**
     * Starts the icon animation according to the [heatmap] action as per the [position]
     * */
    private fun startEngagementIconAnimation(position: Float, heatmap: Heatmap) {
        if (heatmap.iconUrl != null) {
            val engagementLottie = LottieAnimationView(context).apply {
                //to avoid crash on some parse error, attach failure listener
                setFailureListener { it.printStackTrace() }
                setAnimationFromUrl(heatmap.iconUrl, heatmap.action.toString())
                animationEnd { animationsContainer?.removeView(this) }
                repeatCount = 0
            }

            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

            //Getting the width for the frame layout and positioning every animation based on its start time by setting the params
            val metrics: DisplayMetrics? = rootView?.resources?.displayMetrics
            if (metrics != null) {
                params.leftMargin = (min(position.div(currentPostDuration?.toFloat() ?: 1F), 1F) * metrics.widthPixels).toInt() - ANIMATION_POSITION_ADJUSTMENT
                params.width = 126
                params.height = 93
            }
            animationsContainer?.addView(engagementLottie, params)
            engagementLottie.playAnimation()
        }
    }

    /**
     * Called to clear the data for the heatmap view of the current itemview.
     * */
    fun reset() {
        animationsContainer?.removeAllViews()
        setProgress(0F)
        resetAnimationIndex()
    }

    /**
     * Called to reset the animation index to show animations from the start
     * */
    fun resetAnimationIndex() {
        animationIndex = 0
    }
}