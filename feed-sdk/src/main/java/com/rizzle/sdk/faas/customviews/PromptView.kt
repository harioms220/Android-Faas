package com.rizzle.sdk.faas.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.LayoutFeedDetailsBinding
import com.rizzle.sdk.faas.feed.FeedListener
import com.rizzle.sdk.faas.helpers.*
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.uistylers.setStyleType
import com.rizzle.sdk.faas.utils.InternalUtils.color
import com.rizzle.sdk.faas.utils.InternalUtils.string
import com.rizzle.sdk.faas.utils.Resolution

/**
 * Custom view class responsible for handling prompt view and its animations on feed
 * */
class PromptView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAttr) {

    private val TAG = javaClass.simpleName
    private var _binding: LayoutFeedDetailsBinding? = null
    private val binding get() = _binding!!
    val showCTA: Boolean get() = post?.showCollapsedCtaCard ?: false
    var stateChangedListener: StateChangedListener? = null
    var feedListener: FeedListener? = null
    var post: Post? = null


    init {
        _binding = LayoutFeedDetailsBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun setUpClickListeners() {
        binding.apply {
            musicCardIcShowDetail.click { togglePrompt() }
            musicTrackLottieLayout.click {
                post?.let { feedListener?.onTrackClicked(it.track?.id!!, it) }
            }
            expandedPromptTitle.click { togglePrompt() }
            icHideDetails.click { togglePrompt() }
            tvWatchNow.click {
                post?.let { feedListener?.onWatchNowClicked(it) }
            }
        }
    }

    fun togglePrompt() {
        if (showCTA) {
            binding.expandedPromptLayout.toggle(true)
            binding.promptTrackCardContainer.toggle(true)
        } else {
            binding.icHideDetails.toggleArrowRotate(binding.expandableDetails.isExpanded)
            binding.expandableDetails.toggle(true)
        }
    }

    fun initalState() {
        setupExpansionStateListeners()
        binding.apply {
            promptTrackCardContainer.collapse(isAnimate = false, triggerExpandCallback = false)
            expandedPromptLayout.expand(isAnimate = false, triggerExpandCallback = false)
            expandableDetails.expand(isAnimate = false, triggerExpandCallback = false)
            icHideDetails.rotation = 0f
        }

        setUpClickListeners()
    }

    private fun setupExpansionStateListeners() {

        if (showCTA.not()) {
            binding.expandableDetails.listener = object : ExpandableLayout.OnExpansionChangeListener {
                override fun onExpansionChanged(expansion: Float, state: ExpandableLayout.State) {
                    when (state) {
                        ExpandableLayout.State.EXPANDED -> {
                            stateChangedListener?.onStateChanged(State.EXPANDED)
                        }
                        ExpandableLayout.State.COLLAPSED -> {
                            stateChangedListener?.onStateChanged(State.COLLAPSED)
                        }
                        else -> {}
                    }
                }
            }
        }


        if (showCTA) {
            binding.expandedPromptLayout.listener = object : ExpandableLayout.OnExpansionChangeListener {
                override fun onExpansionChanged(expansion: Float, state: ExpandableLayout.State) {
                    when (state) {
                        ExpandableLayout.State.EXPANDED -> {
                            stateChangedListener?.onStateChanged(State.EXPANDED)
                        }
                        ExpandableLayout.State.COLLAPSED -> {
                            stateChangedListener?.onStateChanged(State.COLLAPSED)
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    fun interface StateChangedListener {
        fun onStateChanged(newState: State)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.apply {

            musicTrackTitle.text = post?.cta?.title
            tvWatchNow.text = post?.cta?.buttonText
            Glide.with(context).load(post?.cta?.thumbnailUrl, size = Resolution(100, 178))
                .into(musicTrackThumbnail)
            expandedPromptTitle.text = post?.title
            if (!post?.description.isNullOrEmpty()) {
                promptDescription.show()
                promptDescription.text = post?.description
            }
            post?.isTrending?.let {
                trendLayout.showIf(it)
            }
            post?.isFeatured?.let {
                featuredLayout.showIf(it)
            }
            totalViewsEx.text = post?.viewCount?.formatCool()

            if (post?.track?.id?.isBlank()?.not() == true && post?.track?.title.isNullOrBlank().not() || post?.track?.albumTitle.isNullOrBlank().not()) {
                musicTrackLottieLayout.show()
                lottieTrack.apply {
                    setAnimation("lottie_files/track_anim.json")
                    playAnimation()
                    repeatCount = LottieDrawable.INFINITE
                }
                feedTrackName.isSelected = true
                feedTrackName.text =
                    if (!post?.track?.title.isNullOrBlank() && !post?.track?.albumTitle.isNullOrBlank()) string(R.string.flair_title_text).format(post?.track?.title, post?.track?.albumTitle)
                    else if (!post?.track?.title.isNullOrBlank()) post?.track?.title
                    else post?.track?.albumTitle
            } else musicTrackLottieLayout.gone()

            post?.let { postObj ->
                postObj.hashtags?.let { hashtagsList ->
                    if (hashtagsList.isNotEmpty()) {
                        tvHashtags.apply {
                            show()
                            setPropertyAndText(postObj.getHashTags(), color(R.color.white))
                            setStyleType()
                            onAutoLinkClick { item ->
                                feedListener?.onHashtagClicked(hashtagsList.first { item.originalText == it.name?.hashify() }, postObj)
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.lottieTrack.pauseAnimation()
    }

    private fun View.toggleArrowRotate(isExpanded: Boolean) = animate().setDuration(300).rotation(if (isExpanded) 180f else 0f)

    fun resumeTrackAnimation() = binding.lottieTrack.resumeAnimation()
    fun pauseTrackAnimation() = binding.lottieTrack.pauseAnimation()
}

enum class State {
    COLLAPSED,
    EXPANDED
}
