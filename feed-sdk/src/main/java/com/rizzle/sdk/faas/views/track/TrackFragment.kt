package com.rizzle.sdk.faas.views.track

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.faltenreich.skeletonlayout.Skeleton
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.databinding.FragmentTrackBinding
import com.rizzle.sdk.faas.helpers.*
import com.rizzle.sdk.faas.models.*
import com.rizzle.sdk.faas.navigation.NavigationFragment
import com.rizzle.sdk.faas.navigation.models.TrackFragmentArgs
import com.rizzle.sdk.faas.shimmer.ShimmerEffect
import com.rizzle.sdk.faas.utils.InternalUtils.string
import com.rizzle.sdk.faas.viewModels.TrackViewModel
import com.rizzle.sdk.faas.views.baseViews.BaseFragment
import com.rizzle.sdk.faas.views.hashtag.PostGridAdapter
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

/**
 * Fragment used for Track screen.
 */
internal class TrackFragment : BaseFragment<FragmentTrackBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTrackBinding = FragmentTrackBinding::inflate
    private var skeleton: Skeleton? = null

    /** view model scoped to this fragment */
    private val model: TrackViewModel by viewModels()
    private val parent: NavigationFragment by lazy { parentFragment as NavigationFragment }
    private lateinit var postGridAdapter: PostGridAdapter
    private var postId: String? = null
    private var trackId: String? = null
    private var manuallyPaused = false

    private var musicTrack: Track? = null
    private fun toggleExoPlayer() {
        if (player?.isPlaying == false) model.playExoPlayer()
        else {
            model.pauseExoPlayer()
            manuallyPaused = true
        }
    }

    companion object {
        fun newInstance(bundle: Bundle): TrackFragment {
            val fragment = TrackFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    val handler = Handler(Looper.getMainLooper())

    private var exoTimeRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val player = player ?: return

            val currentPosition = player.currentPosition
            binding.seekBar.progress = currentPosition.toInt()
            binding.trackTimeTv.text = currentPosition.msToMMSS()

            //repeat runnable if duration hasn't reached end
            if (currentPosition < player.duration) handler.postDelayed(this, 100)
        }
    }

    /** single player used for all tracks */
    private var player: ExoPlayer? = null

    /** playback listener used for [player] */
    private val exoListener: Player.Listener by lazy {
        object : Player.Listener {

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                Timber.tag(TAG).d("PlayWhenReady: $playWhenReady with reason : $reason")
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (playWhenReady) {
                    handler.post(exoTimeRunnable)
                    binding.playIcon.setImageResource(R.drawable.ic_pause)
                } else {
                    handler.removeCallbacks(exoTimeRunnable)
                    binding.playIcon.setImageResource(R.drawable.ic_play)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Timber.tag(TAG).d("Playback State changed : $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (player?.playWhenReady == true) {
                            handler.post(exoTimeRunnable)
                            binding.playIcon.setImageResource(R.drawable.ic_pause)
                        } else {
                            handler.removeCallbacks(exoTimeRunnable)
                            binding.playIcon.setImageResource(R.drawable.ic_play)
                        }
                    }

                    Player.STATE_ENDED -> {
                        binding.seekBar.progress = 0
                        binding.playIcon.setImageResource(R.drawable.ic_play)
                        player?.apply { seekTo(0); pause() }
                    }
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = TrackFragmentArgs.fromBundle(requireArguments())
        postId = args.postId
        trackId = args.trackId
        postGridAdapter = PostGridAdapter(postId) { posts, position ->
            parent.model.navigateToSecondaryFeed(SecondaryFeedData(posts.map { it.id }, posts[position].id, SecondaryFeedLaunchSource.HASHTAG, position, model.sectionsQueryState.endCursor))
        }
    }

    override fun setup() {
        binding.apply {
            postsRecyclerView.adapter = postGridAdapter
            musicTrackThumbnailImageView.setImageResource(R.drawable.ic_trending)
            postsRecyclerView.adapter = postGridAdapter
            postsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            postsRecyclerView.attachPagination { fetchPosts(true) }

            headerRow.apply {
                genericHeaderCenteredLeftIcon.touch { requireActivity().onBackPressedDispatcher.onBackPressed() }
                genericHeaderCenteredRight.touch {
                    parent.model.openShareSheet(musicTrack?.id!!, player?.currentPosition ?: 0, ShareObject.TRACK)
                }
            }
            playIcon.click { toggleExoPlayer() }

            skeleton = ShimmerEffect.SkeletonBuilder(ShimmerEffect.Type.RECYCLERVIEW, postsRecyclerView, R.layout.skeleton_grid_layout, 9).build()
            skeleton?.showSkeleton()
            showLoading(true, "Loading")
        }

        binding.errorLayout.errorActionBtn.touch {
            fetchData()
        }

        fetchData()
    }

    private fun fetchData() {
        fetchTrackDetails()
    }

    private fun fetchTrackDetails() {
        subscriptions += model
            .getTrackInfo(trackId!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(TAG).d("$it")
                showLoading(false, "Loading")
                setTrackDetails(it)
                fetchPosts(false)
            }, {
                setState(ViewState.ERROR)
            })
    }

    private fun fetchPosts(pageCall: Boolean = false) {
        var needToAppend = pageCall

        // fetch the justWatchedPost in feed if this screen is launched from feed screen and pageCall is false.
        val justWatchedPostCompletable = postId?.let {
            if (pageCall.not()) {
                model.getPostsByIds(listOf(it))
                    .doOnSuccess { posts ->
                        postGridAdapter.setData(posts, needToAppend)
                        needToAppend = true
                    }.ignoreElement()
            } else Completable.complete()
        } ?: Completable.complete()

        justWatchedPostCompletable.andThen(model.getTrackPosts(trackId!!))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { if (!pageCall) setState(ViewState.LOADING) }
            .subscribe({ list ->
                if (!pageCall) if (list.isEmpty()) setState(ViewState.EMPTY_LIST) else setState(
                    ViewState.LOADED
                )
                postGridAdapter.setData(list.filter { it.id != postId }, needToAppend)
                postGridAdapter.noMoreData = model.sectionsQueryState.isLastCursor()
            }, {
                when (it) {
                    is QueryAlreadyInProgress, is PaginationError -> Unit
                    else -> setState(ViewState.ERROR)
                }
            })
    }

    private fun setTrackDetails(track: Track) {
        musicTrack = track
        binding.apply {
            //here we set seek bar change listener
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, userDragged: Boolean) {
                    if (userDragged) {
                        player?.seekTo(progress.toLong())
                        binding.seekBar.progress = progress
                        seekBar.progress = player?.currentPosition?.toInt() ?: 0
                        binding.trackTimeTv.text = track.audio?.duration?.toLong()?.msToMMSS()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            seekBar.max = track.audio?.duration!!

            musicTrackTitle.text = track.title
            binding.musicTrackArtist.text = track.artist
            viewCount.text = string(R.string.text_total_views).format(track.viewCount?.toLong()?.formatCool())
            Glide.with(this@TrackFragment)
                .load(musicTrack?.audio?.trackImageUrl)
                .into(musicTrackThumbnailImageView)
        }

        setUpExoPlayer()
    }

    private fun setState(state: ViewState) {
        when (state) {
            ViewState.LOADED -> {
                skeleton?.showOriginal()
                binding.apply {
                    errorLayout.root.hide()
                    postsRecyclerView.show()
                }
            }
            ViewState.ERROR -> {
                skeleton?.showOriginal()
                binding.errorLayout.root.show()
                showLoading(false, "Loading")
                model.pauseExoPlayer()
            }
            ViewState.EMPTY_LIST -> {
                skeleton?.showOriginal()
                binding.apply {
                    postsRecyclerView.hide()
                    noTracksAvailable.show()
                    errorLayout.root.hide()
                }
            }
            ViewState.LOADING -> {
                skeleton?.showSkeleton()
                binding.errorLayout.root.hide()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.tag(TAG).d("onStart called")
        musicTrack?.let { setUpExoPlayer() }
    }


    private fun setUpExoPlayer() {
        if (player == null) player = model.createPlayer()
        player?.apply {
            addListener(exoListener)
            musicTrack?.audio?.trackUrl?.let { prepareGood(ExoHelper.getHttpDataSource(it)) }
            if (manuallyPaused) {
                reset()
            } else model.playExoPlayer()

        }
    }

    private fun reset() {
        model.pauseExoPlayer()
        Timber.tag(TAG).d("resetting track")
        binding.seekBar.progress = 0
        binding.trackTimeTv.text = player?.currentPosition?.msToMMSS()
    }


    override fun onResume() {
        super.onResume()
        if (manuallyPaused.not()) model.playExoPlayer()
    }


    override fun onPause() {
        super.onPause()
        model.pauseExoPlayer()
    }


    override fun onStop() {
        super.onStop()
        Timber.tag(TAG).d("onStop called")
        model.releaseExoPlayer(exoListener)
        player = null
    }

    override fun onVisibilityChanged(visibility: Int) {
        Timber.tag(TAG).d("visibility changed")
        when (visibility) {
            View.VISIBLE -> musicTrack?.let { setUpExoPlayer() }
            View.INVISIBLE -> player?.stop()
            View.GONE -> player?.stop()
        }
    }
}