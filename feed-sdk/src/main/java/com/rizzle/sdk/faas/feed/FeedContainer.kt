package com.rizzle.sdk.faas.feed

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.utils.InternalUtils.color
import timber.log.Timber


/**
 * Container responsible for handling feed, track and hashtag screens
 */
class FeedContainer @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr), DefaultLifecycleObserver {

    private var navigationHost: NavigationFragment? = null
    private val TAG = javaClass.simpleName


    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @IntDef(HORIZONTAL, VERTICAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Orientation

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL
        private const val DEFAULT_ORIENTATION = VERTICAL
        internal var orientation = DEFAULT_ORIENTATION
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.FeedContainer)
        orientation = typedArray.getInteger(R.styleable.FeedContainer_feedScrollType, DEFAULT_ORIENTATION)
        typedArray.recycle()
        setBackgroundColor(color(R.color.gray_main))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        navigationHost?.containerWidth = w
        navigationHost?.containerHeight = h
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView != this) return
        navigationHost?.notifyVisibilityChanged(visibility)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        navigationHost = NavigationFragment()
        navigationHost?.feedOrientation = orientation
        val fragmentContainingThisView: Fragment?
        fragmentContainingThisView = try {
            findFragment()
        } catch (ex: IllegalStateException) {
            Timber.tag(TAG).d("view not attached to any fragment")
            null
        }
        // if some fragment is containing this view then load our navigation host fragment as child fragment
        fragmentContainingThisView?.let {
            loadScreen(navigationHost!!, it.childFragmentManager)
        } ?: loadScreen(navigationHost!!, (context as FragmentActivity).supportFragmentManager)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        navigationHost = null
    }

    private fun loadScreen(targetScreen: Fragment, fragmentManager: FragmentManager) {
        val transaction = fragmentManager
            .beginTransaction()
            .replace(this.id, targetScreen)
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
        transaction.commitAllowingStateLoss()
    }

    internal var sharedData: Pair<ShareObject, String>? = null
        set(value) {
            field = value
            navigationHost?.sharedData = value
        }
}