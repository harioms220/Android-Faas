package com.rizzle.sdk.faas.feed

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.annotation.RestrictTo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.rizzle.sdk.faas.customviews.PromptView
import com.rizzle.sdk.faas.customviews.State
import com.rizzle.sdk.faas.databinding.ItemFeedBinding
import com.rizzle.sdk.faas.databinding.ItemLastBinding
import com.rizzle.sdk.faas.helpers.*
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.utils.InternalUtils.application
import com.rizzle.sdk.faas.utils.InternalUtils.isHevcDecoderSupportingPostResolution
import com.rizzle.sdk.faas.utils.Resolution
import com.rizzle.sdk.faas.utils.getAnimateForLikeBtn
import com.rizzle.sdk.faas.viewModels.FeedViewModel
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min


private const val TYPE_FEED = 555
private const val TYPE_LAST_ITEM = 554
private const val DISTANCE_FROM_LAST_ITEM_FOR_NEW_FEED = 3

/**
 * Adapter class to be used in [FeedFragment] to show the feed
 *
 * @param model ViewModel associated with the [FeedFragment]
 * @param orientation Orientation of the feed specified in [FeedContainer]
 * @param listener The listener to listen feed related callbacks defined in [FeedListener]
 * */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class FeedAdapter(
    private var model: FeedViewModel,
    @FeedContainer.Orientation private var orientation: Int,
    private var listener: FeedListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DefaultLifecycleObserver {

    private val TAG = javaClass.simpleName
    private val AUTO_COLLAPSE_INTERVAL = 4000L

    private var rv: RecyclerView? = null

    private var posts = mutableListOf<Post>()

    // single player used for all items in adapter
    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    // width of the item in adapter
    var itemWidth: Int = 0

    // height of the item in adapter
    var itemHeight: Int = 0

    // true if recyclerView visibility is VISIBLE, else false
    var isVisible: Boolean = true
    private var startedPlayback = false

    /*
     * state and current playing view related variables
     */

    /** Current Player view on which media is to be played */
    private var currentPlayerView: StyledPlayerView? = null

    /** Current adapter position on which media is being played */
    private var currentPlayPosition = RecyclerView.NO_POSITION

    /** Current viewHolder on which media is being played */
    private var currentVisibleViewHolder: FeedViewHolder? = null

    /** Binding associated with [currentVisibleViewHolder] */
    private val currentVisibleBinding: ItemFeedBinding?
        get() = currentVisibleViewHolder?.binding

    /** Current [Post] containing the item data */
    private val currentVisiblePost: Post?
        get() {
            if (isValidPosition(currentPlayPosition).not()) return null
            return posts[currentPlayPosition]
        }

    /** Current State of the media in adapter */
    private var currentState: VIEW_STATE = VIEW_STATE.STATE_IDLE

    /** Current Scrolling direction forward or backward */
    private val currentScrollDirection: ScrollDirection
        get() = scrollListener.scrollingDirection


    private val collapsedPromptViewRunnable = Runnable { currentVisibleBinding?.promptView?.togglePrompt() }

    companion object {
        private const val MIN_DIRECT_SCROLL = 10 // reference index to jump from current position in top to scroll
    }

    // region listeners and gestures
    /**
     * scroll listener to the recycler view detecting the scroll direction
     * */
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        // default scrolling is considered in FORWARD DIRECTION
        var scrollingDirection: ScrollDirection = ScrollDirection.FORWARD

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val directionDisplacement = if (orientation == FeedContainer.VERTICAL) dy else dx
            scrollingDirection = if (directionDisplacement >= 0) ScrollDirection.FORWARD else ScrollDirection.BACKWARD
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> feedSettled()
                RecyclerView.SCROLL_STATE_DRAGGING -> onFeedDragged(recyclerView)
            }
        }

        private fun onFeedDragged(recyclerView: RecyclerView) {
            val currentRecyclerViewItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (currentRecyclerViewItemPosition == RecyclerView.NO_POSITION) return

            //get distance from last item
            val distanceFromEnd = itemCount.minus(currentRecyclerViewItemPosition + 1)

            if (distanceFromEnd <= DISTANCE_FROM_LAST_ITEM_FOR_NEW_FEED) {
                model.fetchFeed(true)
            }
        }
    }


    private var gestureDetector: GestureDetector = GestureDetector(application, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
            Timber.tag(TAG).d("onDoubleTap")
            doubleTapHappened()
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            singleTapHappened()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            onLongPressHappened()
        }
    })

    private fun singleTapHappened() {
        Timber.tag(TAG).d("onSingleTapConfirmed")
        when (currentState) {
            VIEW_STATE.STATE_PLAYING -> pausePlayback()
            VIEW_STATE.STATE_PAUSED -> startPlayback()
            else -> Unit
        }
    }

    private fun onLongPressHappened() {}

    private fun doubleTapHappened() {
        currentVisibleBinding?.let { likeUnlikePost(it, true) }
    }

    /**
     * Exo player listener
     */
    @Suppress("ControlFlowWithEmptyBody")
    private val exoListener: Player.Listener by lazy {
        object : Player.Listener {

            // when exo player encounters unrecoverable error, we try to re-initialize and play
            // after 500ms
            private var retriedPlayback = false


            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                Timber.tag(TAG).d("PlayWhenReady: $playWhenReady with reason : $reason")
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if(player?.playbackState == Player.STATE_READY){
                    if (playWhenReady) setState(VIEW_STATE.STATE_PLAYING, currentVisibleBinding)
                    else setState(VIEW_STATE.STATE_PAUSED, currentVisibleBinding)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Timber.tag(TAG).d("Playback State changed : $playbackState")
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        setState(VIEW_STATE.STATE_BUFFERING, currentVisibleBinding)

                        //stop ongoing prefetch if any as video has started to buffer
                        model.cancelOngoingPrefetch()
                    }

                    Player.STATE_ENDED -> setState(VIEW_STATE.STATE_ENDED, currentVisibleBinding)
                    Player.STATE_IDLE -> setState(VIEW_STATE.STATE_IDLE, currentVisibleBinding)

                    Player.STATE_READY -> {
                        if (player?.playWhenReady == true) {
                            setState(VIEW_STATE.STATE_PLAYING, currentVisibleBinding)

                            if (retriedPlayback) retriedPlayback = false

                            //start pre-fetching next 2 videos as video has already started playing
                            model.startPrefetch(getPrefetchList())
                        } else setState(VIEW_STATE.STATE_PAUSED, currentVisibleBinding)
                    }

                    else -> Timber.tag(TAG).d("This should not have happened!")
                }
            }


            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                when (reason) {
                    Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> {
                        //resetting animation index to show animations again in case of video being looped
                        currentVisibleBinding?.engagementHeatmapViewFeed?.resetAnimationIndex()
                    }
                    else -> Timber.tag(TAG).d("onPositionDiscontinuity with reason $reason")
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onPlayerError(error: PlaybackException) {
                error as ExoPlaybackException //downcast to ExoPlaybackException, allowed
                if (error.type == ExoPlaybackException.TYPE_UNEXPECTED
                    && (error.unexpectedException is IllegalStateException)
                    && !isHevcDecoderSupportingPostResolution(Resolution(itemWidth, itemHeight))
                ) {

                    //issue happens when device has hevc decoder but it does not support our resolution
                    //i.e. itemWidth, itemHeight
                    //there is no fix for that, we are falling back to avc

                    startedPlayback = false
                    notifyDataSetChanged()
                } else if (error.cause is HttpDataSource.HttpDataSourceException) {

                } else if (!retriedPlayback) {
                    Timber.tag(TAG).d("onPlayerError retrying playback in 500 ms")
                    handler.postDelayed({
                        retriedPlayback = true
                        scrolledToPosition(currentPlayPosition)
                    }, 1000)
                } else {
                    if (error.cause is DataSourceException) {
//                    RizzleEvent.playback_failed.log(Bundle().apply { putString("failedPostId", currentVisiblePost?.id) })
                    } else {

                    }
                }
            }
        }

    }

    // endregion

    // region adapter functions
    @SuppressLint("NotifyDataSetChanged")
    fun setData(posts: List<Post>) {
        this.posts.clearAndAddAll(posts)
        notifyDataSetChanged()
    }

    private fun updateEndItemView() = notifyItemChanged(itemCount - 1)

    fun addMoreItems(posts: List<Post>, isRefresh: Boolean = false, scrollToPosition: Int? = null) {

        if (isRefresh) startedPlayback = false
        if (hasData().not() || isRefresh) {
            setData(posts)
        } else {
            this.posts.addAll(posts)
            notifyItemRangeInserted(itemCount, posts.size)
        }
        updateEndItemView()
        scrollToPosition?.let { rv?.scrollToPosition(it) }
    }

    fun hasData(): Boolean {
        return posts.isNotEmpty()
    }

    fun removeCurrentPost(){
        if(posts.size == 1) return
        posts.removeAt(currentPlayPosition)
        startedPlayback = false
        notifyItemRemoved(currentPlayPosition)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> TYPE_LAST_ITEM
            else -> TYPE_FEED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LAST_ITEM -> LastItemViewHolder(ItemLastBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> FeedViewHolder(ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.apply {
            if (this is FeedViewHolder) onBind()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachListeners(holder: FeedViewHolder) {
        val post = posts[holder.bindingAdapterPosition]
        holder.binding.apply {
            videoView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }

            llLike.touch {
                likeUnlikePost(this)
            }
            clShare.touch {
                player?.currentPosition?.let { it1 -> listener?.onShareClicked(currentVisiblePost!!, ShareObject.POST, it1) }
            }

            promptView.stateChangedListener = PromptView.StateChangedListener { newState ->
                if (newState == State.EXPANDED) {
                    this@FeedAdapter.handler.removeCallbacks(collapsedPromptViewRunnable)
                    this@FeedAdapter.handler.postDelayed(collapsedPromptViewRunnable, AUTO_COLLAPSE_INTERVAL)
                    currentTrackCard.hide()
                } else {
                    this@FeedAdapter.handler.removeCallbacks(collapsedPromptViewRunnable)
                    if (post.showCollapsedCtaCard.not() && post.areCtaFieldsPresent) {
                        currentTrackCard.show()
                        tvWatchNowBtn.apply {
                            text = post.cta?.buttonText
                            click { listener?.onWatchNowClicked(post) }
                        }
                        Glide.with(currentTrackCard).load(post.cta?.thumbnailUrl, size = Resolution(100, 178))
                            .into(currentTrackImage)
                    }
                }
            }
            /** Setting the prompt view to initial state(expanded details), when its this holder attached to window
             *  Will add the collapse handler once the recycler view scrolling settles down.
             **/
            promptView.initalState()
        }
    }

    private fun likeUnlikePost(itemFeedBinding: ItemFeedBinding, isDoubleTab: Boolean = false) {
        val post = posts[currentVisibleViewHolder?.bindingAdapterPosition ?: 0]

        itemFeedBinding.apply {
            val animateSet = getAnimateForLikeBtn(ivLike)
            if (post.context?.isLiked?.not() == true || (isDoubleTab)) {
                if(isDoubleTab) lottieDoubleTapLike.apply { setAnimation("lottie_files/double_tap_like.json"); playAnimation() }
                else lottieLike.apply { setAnimation("lottie_files/like_normal.json"); playAnimation() }
                ivLike.iconSelected = true
                if (post.context?.isLiked?.not() == true) {
                    llLike.hapticFeedback()
                    post.context?.isLiked = true
                    post.likeCount?.let { likeCount ->
                        post.likeCount = likeCount.plus(1)
                    }
                    likeCount.text = post.likeCount?.formatCool()
                    post.id.let { it1 -> post.likeCount?.let { it2 -> model.likePost(it1, it2) } }
                }
            } else {
                llLike.hapticFeedback()
                post.context?.isLiked = false
                ivLike.iconSelected = false
                post.likeCount?.let { likeCount ->
                    post.likeCount = likeCount.minus(1)
                }
                likeCount.text = post.likeCount?.formatCool()
                post.id.let { it1 -> post.likeCount?.let { it2 -> model.unlikePost(it1, it2) } }
            }
            animateSet.start()
        }
    }

    private fun updatePausePlay(start: Boolean) {
        if (start) {
            handler.post(progressVideoRunnable)
            currentVisibleBinding?.promptView?.resumeTrackAnimation()
        } else {
            handler.removeCallbacks(progressVideoRunnable)
            currentVisibleBinding?.promptView?.pauseTrackAnimation()
        }
    }

    private val progressVideoRunnable = object : Runnable {
        override fun run() {
            //set progress for heatmap seek-line
            player?.currentPosition?.let { currentVisibleBinding?.engagementHeatmapViewFeed?.setProgress(it.toFloat()) }
            handler.postDelayed(this, 30L)
        }
    }

    override fun getItemCount() = if (posts.isEmpty()) 0 else posts.size + 1

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        Timber.tag(TAG).d("view attached: binding position ${holder.bindingAdapterPosition}")
        if (holder is FeedViewHolder) {
            attachListeners(holder)
            initVideoPlayer(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == TYPE_LAST_ITEM) return
        Timber.tag(TAG).d("view detached: binding position ${holder.bindingAdapterPosition}")
        if (currentPlayPosition != RecyclerView.NO_POSITION && currentPlayPosition == holder.bindingAdapterPosition) {
            model.pausePlayback()
            (holder as FeedViewHolder).binding.apply {
                engagementHeatmapViewFeed.reset()
                lottieLike.apply {
                    cancelAnimation()
                    removeAllAnimatorListeners()
                    progress = 1f
                }
                lottieDoubleTapLike.apply {
                    cancelAnimation()
                    removeAllAnimatorListeners()
                    progress = 1f
                }
                currentTrackCard.hide()
            }
            cancelPendingProcessesOnCurrentViewHolder()
        }
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeOnScrollListener(scrollListener)
        rv = null
    }

    // endregion


    // region adapter and video state
    private fun updateVisibleItem() {
        //added to resolve firebase crash, more of a patch, more analysis needs to be done
        if (currentPlayPosition < 0) {
            return
        }

        rv?.findViewHolderForAdapterPosition(currentPlayPosition)?.let {
            initVideoPlayer(it as FeedViewHolder)
        }
    }

    /**
     * When we move to a next item in feed,
     * we should cancel all the pending operations that are still running or that will become irrelevant
     * */
    private fun cancelPendingProcessesOnCurrentViewHolder() {
        handler.removeCallbacks(progressVideoRunnable)
        handler.removeCallbacks(collapsedPromptViewRunnable)
    }

    fun setState(state: VIEW_STATE, currentVisibleBinding: ItemFeedBinding?) {
        currentState = state
        Timber.tag(TAG).d("current state: $state")
        when (state) {
            VIEW_STATE.STATE_IDLE -> {}
            VIEW_STATE.STATE_BUFFERING -> updatePausePlay(false)
            VIEW_STATE.STATE_PLAYING -> {
                updatePausePlay(true)
                currentVisibleBinding?.thumbnail?.hide()
                updatePlayButton(false)
            }
            VIEW_STATE.STATE_PAUSED -> {
                updatePausePlay(false)
                updatePlayButton(true)
            }
            VIEW_STATE.STATE_ENDED -> updatePausePlay(false)
        }

    }

    /**
     * Called when recycler view comes to idle state after scrolling
     * if new item appears then player instance is switched to new item,
     * else player keeps running on [currentPlayerView]
     * */
    private fun feedSettled() {
        val lm = rv?.layoutManager as? LinearLayoutManager ?: return
        val position = lm.findFirstCompletelyVisibleItemPosition()

        if (position == RecyclerView.NO_POSITION || position == currentPlayPosition) return
        if(position == itemCount - 1){
            handler.post { rv?.smoothScrollToPosition(position - 1) }
            return
        }

        currentVisibleBinding?.thumbnail?.show()
        updatePlayButton(false)
        scrolledToPosition(position)
    }

    private fun updatePlayButton(show: Boolean) {
        currentVisibleBinding?.playPauseButton?.apply {
            if (show) dramaticShow() else dramaticHide()
        }
    }

    /**
     * initialize player view for given position
     */
    private fun scrolledToPosition(position: Int) {
        //added check to ensure valid item position
        //added check to make sure item exists in adapter
        if (isValidPosition(position)) {
            markCurrentPostViewed()

            currentPlayPosition = position
            val holder = rv?.findViewHolderForAdapterPosition(position) as FeedViewHolder
            currentVisibleViewHolder = holder
            initializeVideoView(holder)
        }
    }

    fun markCurrentPostViewed() {
        currentVisiblePost?.viewCount = currentVisiblePost?.viewCount?.plus(1)
        currentVisiblePost?.id?.let { currentVisiblePost?.viewCount?.let { it1 -> model.viewPost(it, it1) } }
    }

    /**
     * check is given position in valid
     */
    private fun isValidPosition(position: Int): Boolean {
        return position != RecyclerView.NO_POSITION && position >= 0 && position < posts.size
    }

    private fun getPrefetchList(): List<Post?> {
        if (currentPlayPosition == posts.size - 1) return mutableListOf()   //no more videos to prefetch
        //Always try to prefetch 2 posts before or 2 posts after
        //But also be safe about (0 to posts.size-1)
        val range = if (currentScrollDirection == ScrollDirection.BACKWARD) currentPlayPosition - 1 downTo max(0, currentPlayPosition - 2)         //Prefetch previous 2 videos
        else currentPlayPosition + 1..min(posts.size - 1, currentPlayPosition + 2)         //Prefetch next 2 videos

        val list = mutableListOf<Post?>()
        Timber.tag(TAG).d("prefetchNextVideos for currentPosition $currentPlayPosition --> ${range.toList()}")
        for (position in range) list.add(posts[position])
        return list
    }


    private fun initVideoPlayer(holder: FeedViewHolder) {
        if (!startedPlayback) {
            startedPlayback = true
            currentPlayPosition = holder.bindingAdapterPosition
            currentVisibleViewHolder = holder
            initializeVideoView(holder)
        }
    }


    private fun initializeVideoView(holder: FeedViewHolder) {

        with(holder.binding) {
            videoView.useController = false

            //if old player view exist, switch
            if (currentPlayerView == null) videoView.player = player
            else StyledPlayerView.switchTargetView(player as Player, currentPlayerView, videoView)

            currentPlayerView = videoView
        }
        handler.postDelayed(collapsedPromptViewRunnable, AUTO_COLLAPSE_INTERVAL)
        listener?.onPostAppeared(posts[currentPlayPosition])

        player?.repeatMode = Player.REPEAT_MODE_ONE
        model.prepare(holder.videoSource)
        startPlayback()
        Timber.tag(TAG).d("initializeVideoView $currentVisiblePost")
    }

    // endregion


    // region player utilities

    /**
     * resume the playback on adapter's [currentPlayPosition]
     */
    private fun startPlayback() {
        model.getAudioFocusWhenVideoStartPlaying()

        if (currentPlayPosition == RecyclerView.NO_POSITION) return

        //added check to make sure player instance is connected to current visible item before starting playback
        //in one particular scenario where we remove items of blocked users from feed, item might be removed already from the adapter
        //but running this would start playback to un-asttached player instance
        if (currentVisibleBinding?.videoView?.player == null) return

        model.startPlayback()  //start playback of global player instance
        handler.post(progressVideoRunnable)

        listener?.onResumed(posts[currentPlayPosition])


        Timber.tag(TAG).d("startPlayback")
    }

    /**
     * no media is released, just current playing media is paused
     */
    private fun pausePlayback() {
        if (!model.isPlaying()) return
        if (currentPlayPosition == RecyclerView.NO_POSITION) return
        model.pausePlayback()  //pause playback of global player instance
        listener?.onPaused(posts[currentPlayPosition])

        Timber.tag(TAG).d("pausePlayback")
    }

    private fun initialisePlayer() {
        player = model.createPlayer(exoListener)
    }

    /**
     * The player will release the loaded media and resources required for playback.
     * */
    private fun hideItSelf() {
        Timber.tag(TAG).d("releasing the media sources from exoplayer")
        model.cancelFetchFeed()
        player?.stop() // release the media sources
        currentPlayerView?.let {
            it.player = null
        }
        currentPlayerView = null
        isVisible = false
    }

    /**
     * This method will check if adapter has data to show
     * if it has then it will display, otherwise fetch the feed
     */
    private fun resumeFeed() {
        isVisible = true
        if (hasData()) {
            resume()
        }
    }

    /**
     * Will re-prepare the playback media and resume the playback on [currentPlayPosition] of adapter
     * */
    private fun resume() {
        scrolledToPosition(currentPlayPosition)
    }

    /**
     * Will release player, media sources, player cannot be used without re-initializing once this method is called
     * */
    fun release() {
        Timber.tag(TAG).d("releasing exoplayer")
        model.releaseExoPlayer(exoListener)
        currentPlayerView?.let {
            it.player = null
        }
        currentPlayerView = null
        startedPlayback = false
    }

    //endregion


    // region Lifecycle callbacks

    override fun onCreate(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: onCreate called")
        super.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: onStart called")
        initialisePlayer()
    }

    override fun onResume(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: onResume called")
        if (isVisible) {
            updateVisibleItem()
            startPlayback()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: onPause called")
        if (!model.isPlaying()) return

        //added safety check, found crash in crashlytics
        if (currentPlayPosition == RecyclerView.NO_POSITION) return

        model.pausePlayback()  //pause playback of global player instance
    }

    override fun onStop(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: onStop called")
        super.onStop(owner)
        markCurrentPostViewed()
        // cancel network call if there is any pending
        release()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.tag(TAG).d("lifecycle: on Destroy called")
        super.onDestroy(owner)
    }

    fun notifyVisibility(visibility: Int) {
        Timber.tag(TAG).d("visibility changed")
        when (visibility) {
            View.VISIBLE -> resumeFeed()
            View.INVISIBLE -> hideItSelf()
            View.GONE -> hideItSelf()
        }
    }

    // endregion


    @Suppress("ClassName")
    enum class VIEW_STATE {
        STATE_IDLE,           // nothing yet
        STATE_BUFFERING,      //buffering in between playback
        STATE_PLAYING,        //playing again, this state will toggle with buffering in case of shaky connection
        STATE_PAUSED,         //paused manually
        STATE_ENDED           //ended source playback
    }

    inner class FeedViewHolder(var binding: ItemFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        var videoSource: BaseMediaSource? = null

        fun onBind() {
            val post = posts[bindingAdapterPosition]
            val uri = post.getUri()
            videoSource = ExoHelper.getCacheDataSource(uri, setCacheKey = true)

            binding.apply {
                thumbnail.apply {
                    Glide.with(context).load(post.video?.thumbnailUrl, size = Resolution(itemWidth, itemHeight)).into(this)
                }
                ivLike.iconSelected = post.context?.isLiked == true
                likeCount.text = post.likeCount?.formatCool()
                engagementHeatmapViewFeed.setData(post)
                promptView.apply {
                    this.post = post
                    this.feedListener = this@FeedAdapter.listener
                }

                icBack.showIf(model.isSecondaryFeed())
                icBack.setOnClickListener {
                    listener?.onFeedBackPressed()
                }

                topPane.click {
                    if (bindingAdapterPosition > 0) scrollToTop(bindingAdapterPosition)
                }
            }
        }
    }


    /**
     * source link for code: https://stackoverflow.com/a/65474200/18701423
     * scroll to certain position, then smooth scroll to 0
     * */
    private fun scrollToTop(size: Int) {
        rv?.apply {
            if (size > MIN_DIRECT_SCROLL) {
                //scroll directly to certain position
                scrollToPosition(MIN_DIRECT_SCROLL - 1)
            }
            //smooth scroll to top
            smoothScrollToPosition(0)
        }
    }


    inner class LastItemViewHolder(var binding: ItemLastBinding) : RecyclerView.ViewHolder(binding.root)

    enum class ScrollDirection {
        /** Next direction (to reach item on right in case of horizontal scroll, item below in case of vertical) */
        FORWARD,

        /** Previous direction (to reach item on left in case of horizontal scroll), up in case of vertical) */
        BACKWARD
    }

}