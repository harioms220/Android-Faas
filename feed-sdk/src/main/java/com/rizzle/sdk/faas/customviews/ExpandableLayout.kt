package com.rizzle.sdk.faas.customviews

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.rizzle.sdk.faas.R
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.roundToInt

/**
 * This is an expandable view group.
 * It can be expanded or collapsed including its child views.
 *
 * Reference: https://github.com/cachapa/ExpandableLayout/blob/master/lib/src/main/java/net/cachapa/expandablelayout/ExpandableLayout.java
 */
class ExpandableLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    /**
     * Different states while expanding or collapsing the layout.
     */
    enum class State {
        /**
         * Viewgroup is not fully collapsed but about to.
         */
        COLLAPSING,

        /**
         * Viewgroup is collapsed fully.
         */
        COLLAPSED,

        /**
         * Viewgroup is not fully collapsed but about to.
         */
        EXPANDING,

        /**
         * View is expanded fully.
         */
        EXPANDED
    }

    /**
     * Duration for expand/collapse anim in millis.
     */
    private var duration: Int? = null

    /**
     * Parallax effect for child views ranging from 0.0f - 1.0f
     */
    private var parallax: Float? = null

    /**
     * Detects whether layout is expanded or collapsed using EXPANDED or COLLAPSED constants.
     */
    private var expansion: Float? = null

    /**
     * Orientation on which expand/collapse anim will perform. Horizontal or Vertical.
     */
    private var orientation: Int? = null

    /**
     * Determines the direction for expand/collapse. LEFT_DIRECTION or RIGHT_DIRECTION.
     */
    private var direction: Int? = null

    var state: State? = null

    //Default interpolator for easing.
    private var interpolator: Interpolator = FastOutSlowInInterpolator()

    /**
     * Main animator for this layout to perform expand/ collapse anims.
     */
    private var mainAnimator: ValueAnimator? = null

    var listener: OnExpansionChangeListener? = null

    /**
     * Flag to detect whether layout is expanded or collapsed.
     */
    val isExpanded get() = state == State.EXPANDING || state == State.EXPANDED

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout)
        duration = typedArray.getInt(R.styleable.ExpandableLayout_duration, DEFAULT_DURATION)
        parallax = typedArray.getFloat(R.styleable.ExpandableLayout_parallax, DEFAULT_PARALLAX)
        expansion = if (typedArray.getBoolean(R.styleable.ExpandableLayout_expanded, DEFAULT_EXPANDED)) EXPANDED else COLLAPSED
        orientation = typedArray.getInt(R.styleable.ExpandableLayout_android_orientation, DEFAULT_ORIENTATION)
        direction = typedArray.getInt(R.styleable.ExpandableLayout_direction, DEFAULT_DIRECTION)
        typedArray.recycle()

        state = if (expansion == EXPANDED) State.EXPANDED else State.COLLAPSED
        parallax?.let { setParallax(it) }
    }

    /**
     * Sets the parallax amount.
     *
     * @param parallax amount ranging from 0.0 - 1.0.
     */
    fun setParallax(parallax: Float) = apply { this.parallax = min(EXPANDED, max(COLLAPSED, parallax)) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        val height = measuredHeight

        val size = if (orientation == LinearLayout.HORIZONTAL) width else height

        visibility = if (expansion == 0f && size == 0) View.INVISIBLE else View.VISIBLE

        expansion?.let {
            val expansionDelta = size - (size * it).roundToInt()
            parallax?.let { parallaxAmount ->
                if (parallaxAmount > 0) {
                    val parallaxDelta = expansionDelta * parallaxAmount
                    for (index in 0 until childCount) {
                        val child = getChildAt(index)
                        if (orientation == HORIZONTAL) {
                            var slidingDirection = 1
                            if (direction == LEFT_DIRECTION) slidingDirection = -1
                            child.translationX = slidingDirection * parallaxDelta
                        } else child.translationY = -parallaxDelta
                    }
                }
            }

            if (orientation == HORIZONTAL)
                setMeasuredDimension(width - expansionDelta, height)
            else
                setMeasuredDimension(width, height - expansionDelta)
        }

    }

    private fun setExpand(isExpandRequested: Boolean, isAnimate: Boolean, triggerExpandCallback: Boolean = true, doOnStart: (() -> Unit)?, doOnEnd: (() -> Unit)?) {
        if (isExpandRequested == this.isExpanded) return

        val expansion = if (isExpandRequested) EXPANDED else COLLAPSED

        if (isAnimate) animateExpansion(expansion, doOnStart, doOnEnd) else setExpansion(expansion,triggerExpandCallback, doOnStart, doOnEnd)
    }

    /**
     * This function sets the expansion without any animation.
     */
    private fun setExpansion(expansion: Float, triggerExpandCallback: Boolean = true, doOnStart: (() -> Unit)? = null, doOnEnd: (() -> Unit)? = null) {
        if (this.expansion == expansion) return

        this.expansion?.let {
            //without animation
            //invoking at same time before requesting the layout
            doOnStart?.invoke()
            doOnEnd?.invoke()

            val delta = expansion - it

            when {
                expansion == COLLAPSED -> state = State.COLLAPSED
                expansion == EXPANDED -> state = State.EXPANDED
                delta < 0 -> state = State.COLLAPSING
                delta > 0 -> state = State.EXPANDING
            }

//            visibility = if (state == State.COLLAPSED) View.INVISIBLE else View.VISIBLE

            this.expansion = expansion

            requestLayout()

            if(triggerExpandCallback) state?.let { newState -> listener?.onExpansionChanged(expansion, newState) }
        }
    }

    /**
     * This function animate the expansion in smoother way.
     */
    private fun animateExpansion(expansion: Float, doOnStart: (() -> Unit)?, doOnEnd: (() -> Unit)?) {
        mainAnimator?.cancel()
        mainAnimator = null

        this.expansion?.let {
            mainAnimator = ValueAnimator.ofFloat(it, expansion).apply {
                interpolator = this@ExpandableLayout.interpolator
                duration = this@ExpandableLayout.duration?.toLong() ?: DEFAULT_DURATION.toLong()

                addUpdateListener { valueAnimator ->
                    setExpansion(valueAnimator.animatedValue as Float)
                }

                doOnStart { doOnStart?.invoke() }
                doOnEnd { doOnEnd?.invoke() }

                addListener(ExpansionAnimationListener(expansion))
                start()
            }
        }

    }

    /**
     * To define orientation programmatically.
     *
     * @param orientation HORIZONTAL or VERTICAL.
     */
    fun setOrientation(orientation: Int) : Boolean {
        if(orientation < HORIZONTAL || orientation > VERTICAL) return false
        this.orientation  = orientation
        return true
    }

    /**
     * To expand the layout.
     *
     * @param isAnimate true if we want to expand while animating else false.
     * @param doOnStart perform piece of action before expanding starts.
     * @param doOnEnd perform piece of action after the layout is expanded.
     */
    fun expand(isAnimate: Boolean, triggerExpandCallback: Boolean = true, doOnStart:(() -> Unit)? = null, doOnEnd:(() -> Unit)? = null) = setExpand(true, isAnimate, triggerExpandCallback, doOnStart, doOnEnd)

    /**
     * To collapse the layout.
     *
     * @param isAnimate true if we want to expand while animating else false.
     * @param doOnStart perform piece of action before collapsing starts.
     * @param doOnEnd perform piece of action after the layout is collapsed.
     */
    fun collapse(isAnimate: Boolean, triggerExpandCallback: Boolean = true, doOnStart:(() -> Unit)? = null, doOnEnd:(() -> Unit)? = null) = setExpand(false, isAnimate, triggerExpandCallback, doOnStart, doOnEnd)

    /**
     * It toggles the state of expandable layout.
     *
     * @param isAnimate true if animate(smoother) while toggling.
     */
    fun toggle(isAnimate: Boolean, triggerExpandCallback: Boolean = true) {
        if (isExpanded) collapse(isAnimate, triggerExpandCallback) else expand(isAnimate, triggerExpandCallback)
    }

    /**
     * Default animation listener for the expandable layout.
     * If have to manage any callbacks of the sliding animation. Refer to this Listener.
     *
     * @param expansion whether it's for EXPANDED or COLLAPSED.
     */
    inner class ExpansionAnimationListener(private val expansion: Float) : Animator.AnimatorListener {
        private var isCancelled = false

        override fun onAnimationRepeat(animator: Animator) {}

        override fun onAnimationEnd(animator: Animator) {
            if (!isCancelled)
                state = if (expansion == COLLAPSED) State.COLLAPSED else State.EXPANDED
        }

        override fun onAnimationCancel(animator: Animator) { isCancelled = true }

        override fun onAnimationStart(animator: Animator) {
            state = if (expansion == COLLAPSED) State.COLLAPSING else State.EXPANDING
        }
    }

    //region handle config changes
    override fun onSaveInstanceState(): Parcelable {
        expansion = if (isExpanded) EXPANDED else COLLAPSED
        return Bundle().apply {
            putParcelable(ARG_SUPER_STATE, super.onSaveInstanceState())
            expansion?.let { putFloat(ARG_EXPANSION, it) }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (state is Bundle) {
            newState = state.getParcelable(ARG_SUPER_STATE)
            expansion = state.getFloat(ARG_EXPANSION)
        }
        super.onRestoreInstanceState(newState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        mainAnimator?.cancel()
        super.onConfigurationChanged(newConfig)
    }

    //endregion

    interface OnExpansionChangeListener {
        fun onExpansionChanged(expansion: Float, state: State)
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        const val LEFT_DIRECTION = 0
        const val RIGHT_DIRECTION = 1

        private const val DEFAULT_DURATION = 500
        private const val DEFAULT_PARALLAX = 1F
        private const val DEFAULT_EXPANDED = false
        private const val DEFAULT_ORIENTATION = VERTICAL
        private const val DEFAULT_DIRECTION = LEFT_DIRECTION

        private const val EXPANDED = 1F
        private const val COLLAPSED = 0F

        private const val ARG_SUPER_STATE = "super_state"
        private const val ARG_EXPANSION = "expansion"
    }

}