package com.rizzle.sdk.faas.viewModels

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Pair
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.rizzle.sdk.faas.helpers.ExoHelper
import com.rizzle.sdk.faas.helpers.RizzleLogging
import com.rizzle.sdk.faas.helpers.log.TAG.Companion.FEED
import com.rizzle.sdk.faas.helpers.plusAssign
import com.rizzle.sdk.faas.helpers.prepareGood
import com.rizzle.sdk.faas.models.*
import com.rizzle.sdk.faas.repos.FeedRepo
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.faas.views.baseViews.QueryState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

/**
 * View Model class scoped to the [FeedFragment]
 */
class FeedViewModel : BaseViewModel() {

    private val TAG = javaClass.simpleName
    private val DB_OPERATION = "DB_OPERATION"
    val error: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    val isLoading: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    private val primaryFeedQueryState by lazy { QueryState() }

    private val secondaryFeedQueryState by lazy { QueryState() }

    private var mAudioFocusRequest: AudioFocusRequest? = null
    private var mAudioManager: AudioManager? = null
    private var player: ExoPlayer? = null
    private val feedRepo = FeedRepo()
    private var secondaryFeedData: SecondaryFeedData? = null
    fun isSecondaryFeed() = secondaryFeedData != null
    var sharedPostId: String? = null
    private var sharedPostFetched = false

    /** Live data list which will be used to receive data from network and will be observed by [FeedAdapter] */
    val postsData: MutableLiveData<NewFeed> by lazy { MutableLiveData() }

    //map of cache writer instances currently active
    //instances are used to cancel pre-fetching
    private var cacheWriters: List<Pair<Post, CacheWriter>>? = null

    fun fetchFeed(isPageCall: Boolean, showLoading: Boolean = true){
        if (sharedPostId.isNullOrEmpty().not() && sharedPostFetched.not()) getSharedPost()
        else if (isSecondaryFeed().not()) getPrimaryFeed(isPageCall.not(), showLoading)
        else getSecondaryFeed(isPageCall)
    }

    fun getPrimaryFeed(isRefresh: Boolean = false, showLoading: Boolean = true) {
        // isRefresh and isPageCall are toggle states
        if (primaryFeedQueryState.inProgress || primaryFeedQueryState.isLastCursor()) return

        var needToRefresh = isRefresh
        subscriptions += getDBDataIfApplicable(isRefresh.not())  // fetch 3 posts from db only if it is not a page call.
            .doOnSuccess {
                if (it.isNotEmpty()) {
                    postsData.postValue(NewFeed(it, isRefresh))
                    needToRefresh = false
                    isLoading.postValue(false)
                }
            }
            .ignoreElement()
            .andThen(feedRepo.getYourFeed(primaryFeedQueryState.endCursor))
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, Feed::class.java)!!
            }
            .doOnSubscribe {
                primaryFeedQueryState.inProgress = true
                isLoading.value = showLoading
                error.postValue(false)
            }
            .doFinally {
                primaryFeedQueryState.inProgress = false
                isLoading.value = (showLoading && primaryFeedQueryState.inProgress)
            }
            .subscribe({
                postsData.value = NewFeed(it.posts?.nodes!!, needToRefresh)
                it.posts?.nodes?.let { it1 -> saveAllPostInDB(it1) }
                primaryFeedQueryState.endCursor = it.posts?.pageInfo?.endCursor
            }, {
                Timber.e("Error while fetching feed with message ${it.message}")
                if (it !is PaginationError) {
                    error.value = true
                }
            })
    }

    fun getSecondaryFeed(isPageCall: Boolean = true) {
        if (secondaryFeedQueryState.inProgress == true || secondaryFeedQueryState.isLastCursor()) return
        if (secondaryFeedData == null && isPageCall) return
        secondaryFeedData?.run {
            val observable = if (isPageCall.not()) {
                feedRepo.getPostsFromIds(postsIds)
            } else {
                secondaryFeedQueryState.preCheck {
                    when (launchedFrom) {
                        SecondaryFeedLaunchSource.HASHTAG -> feedRepo.getHashTagPosts(objectId, secondaryFeedQueryState.endCursor)
                        SecondaryFeedLaunchSource.TRACK -> feedRepo.getTrackPosts(objectId, secondaryFeedQueryState.endCursor)
                    }
                }
            }
            subscriptions +=
                observable
                    .flatMap { responseString ->
                        val commonPostListResponse =
                            InternalUtils.jsonSerializer.convertJsonToPojo(responseString, com.rizzle.sdk.faas.models.responseModels.CommonPostListResponse::class.java)
                        secondaryFeedQueryState.endCursor = commonPostListResponse?.posts?.pageInfo?.endCursor
                        return@flatMap Single.just(
                            commonPostListResponse?.posts?.nodes ?: mutableListOf()
                        )
                    }
                    .doOnSubscribe {
                        isLoading.postValue(true)
                        error.postValue(false)
                    }
                    .doFinally { isLoading.postValue(false) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        postsData.value = NewFeed(it, false, if (isPageCall.not()) currentPositionInFeed else 0)
                        saveAllPostInDB(it)
                    }, {
                        RizzleLogging.logError(it)
                        error.value = true
                    })
        }
    }

    private fun getSharedPost(){
        sharedPostId?.let{
            subscriptions += feedRepo.getPostsFromIds(listOf(it))
                .doOnSubscribe{ isLoading.postValue(true) }
                .doFinally { isLoading.postValue(false) }
                .flatMap { responseString ->
                        val commonPostListResponse =
                            InternalUtils.jsonSerializer.convertJsonToPojo(responseString, com.rizzle.sdk.faas.models.responseModels.CommonPostListResponse::class.java)
                        return@flatMap Single.just(
                            commonPostListResponse?.posts?.nodes ?: mutableListOf())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    postsData.postValue(NewFeed(list, true))
                    isLoading.postValue(false)
                    saveAllPostInDB(list)
                    // after shared post is fetched, fetch for primary feed and append the response
                    sharedPostFetched = true
                    fetchFeed(true, false)
                },{ throwable ->
                    RizzleLogging.logError(throwable)
                    error.value = true
                })
        }
    }


    private fun getDBDataIfApplicable(isPageCall: Boolean): Single<List<Post>> {
        return if (isPageCall) Single.just(emptyList()) else
            feedRepo.loadAllUnwatchedCachedPost().map {
                Timber.tag(FEED).d("addItemsToFeed from cache ${it.size} items added")
                it.forEachIndexed { index, post ->
                    Timber.tag(FEED).d("$index: ${post.id}")
                }
                it
            }
    }

    fun likePost(postId: String, likeCount: Long) {
        subscriptions += player?.let { player ->
            feedRepo.likePost(postId, player.currentPosition.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Like successful")
                    updatePostLikeStatusInDB(true, postId, likeCount)
                }, {
                    Timber.e("Error while Like $postId")
                })
        }
    }

    fun unlikePost(postId: String, likeCount: Long) {
        subscriptions += player?.let { player ->
            feedRepo.unlikePost(postId, player.currentPosition.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("unLike successful")
                    updatePostLikeStatusInDB(false, postId, likeCount)
                }, {
                    Timber.e("Error while unLike $postId")
                })
        }
    }

    private fun updatePostLikeStatusInDB(isLiked: Boolean, postId: String, likeCount: Long) {
        subscriptions += feedRepo.updatePostLikeStatus(isLiked, postId, likeCount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(DB_OPERATION).d("Update like status successful")
            }, {
                Timber.tag(DB_OPERATION).e("Error while updating like status ${it.message}")
            })
    }

    fun viewPost(postId: String, viewCount: Long) {
        subscriptions += player?.let { player ->
            feedRepo.viewPost(postId, player.currentPosition.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("ViewPost successful $viewCount")
                    setPostWatchedInDB(postId, viewCount)
                }, {
                    Timber.e("Error while ViewPost $postId")
                })
        }
    }

    private fun saveAllPostInDB(post: List<Post>) {
        subscriptions += feedRepo.saveAllPostsInDB(post)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(DB_OPERATION).d("Posts are successfully saved in DB")
            }, {
                Timber.tag(DB_OPERATION).e("Error while saving posts in DB with message ${it.message}")
            })
    }

    fun getPostsFromDB(start: Int, count: Int) {
        subscriptions += feedRepo.getPostsFromDB(start, count)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.forEach {
                    Timber.d("Posts from DB: ${it.id}")
                }
            }, {
                Timber.tag(DB_OPERATION).e("Error while getting posts from DB with message ${it.message}")
            })
    }

    fun loadAllUnwatchedCachedPost() {
        subscriptions += feedRepo.loadAllUnwatchedCachedPost()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.forEach {
                    Timber.tag(DB_OPERATION).d("Cached Post with id ${it.id}")
                }
            }, {
                Timber.tag(DB_OPERATION).e("Error while getting cached and not watched posts with message ${it.message}")
            })
    }

    private fun setPostCachedInDB(postId: String) {
        subscriptions += feedRepo.setPostCached(postId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(DB_OPERATION).d("Post is set to be cached in DB for id $postId")
            }, {
                Timber.tag(DB_OPERATION).e("Error while setting post cached in DB with message ${it.message}")
            })

    }

    private fun setPostWatchedInDB(postId: String, viewCount: Long) {
        subscriptions += feedRepo.setPostWatched(postId, viewCount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.tag(DB_OPERATION).d("Post is set to be watched in DB for id $postId")
            }, {
                Timber.tag(DB_OPERATION).e("Error while setting post watched in DB with message ${it.message}")
            })
    }

    fun cancelFetchFeed() {
        subscriptions.clear()
        primaryFeedQueryState.inProgress = false
        secondaryFeedQueryState.inProgress = false
    }

    override fun onCleared() {
        super.onCleared()
        mAudioManager = null
        player = null
        mAudioFocusRequest = null
    }

    @Suppress("DEPRECATION")
    fun getAudioFocusWhenVideoStartPlaying() {

        //if (BuildConfig.DEBUG) return //you don't want to get your music player geting disturbed while working right?

        //stop other apps audio
        mAudioManager = InternalUtils.application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { }

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mPlaybackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()

            mAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                .build()

            mAudioManager?.requestAudioFocus(mAudioFocusRequest!!)
        } else
            mAudioManager?.requestAudioFocus(
                mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )

        when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                Timber.tag(TAG).d("getAudioFocusWhenVideoStartPlaying focus granted")
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                Timber.tag(TAG).d("getAudioFocusWhenVideoStartPlaying focus denied")
            }
        }
    }

    fun prepare(videoSource: MediaSource?) = videoSource?.let { player?.prepareGood(it) }


    /**
     * Create exo player
     * Returns existing one if available, else creates fresh one
     * Listener is optional.
     */
    fun createPlayer(listener: Player.Listener? = null): ExoPlayer? {
        return if (player != null) {
            //add listener in case it wasn't set before
            //method does not add duplicate listener internally
            //if listener exists already, add request is ignored
            listener?.let { player?.addListener(it) }
            player
        } else {
            createExoInstance(listener = listener)
            player
        }
    }

    /**
     * Cancel pre-fetch as it might be affecting existing playback
     */
    fun cancelOngoingPrefetch() = cacheWriters?.forEach { it.second.cancel() }


    /**
     * Creates exo player instance and attaches listener provided to it
     */
    private fun createExoInstance(listener: Player.Listener?) {
        player = ExoHelper.getFastStartExoInstance()
            .apply { listener?.let { addListener(it) } }
    }

    /**
     * Start pre-fetching given list of feed items in sequential order
     */
    fun startPrefetch(posts: List<Post?>) {
        Timber.tag(TAG).d("Prefetch enqueued")
        posts.forEachIndexed { index, forYouFeed -> Timber.tag(TAG).d("$index--> $forYouFeed") }

        cancelOngoingPrefetch()
        cacheWriters = posts
            .filterNotNull()
            .map { post ->
                val spec = ExoHelper.getDataSpec(post.getUri(), ExoHelper.PREFETCH_SIZE)
                val progressListener = CacheWriter.ProgressListener { requestLength, bytesCached, _ ->
                    Timber.tag(TAG).d("Pre-fetch progress -> $bytesCached/$requestLength")
                }
                Pair(post, CacheWriter(ExoHelper.cacheDataSourceFactory.createDataSource(), spec, null, progressListener))
            }

        subscriptions += Observable.fromIterable(cacheWriters!!)
            .concatMapCompletable { writerPair ->
                Completable
                    .fromCallable { writerPair.second.cache() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()) //important because of managed object
                    .doOnComplete {
                        Timber.tag(TAG).d("Prefetch finished ${writerPair.first}")
                        setPostCachedInDB(writerPair.first.id)
                    }
                //.doOnError { e -> Timber.e(e) }
            }
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { Completable.complete() }
            .subscribe({
                Timber.tag(TAG).d("startPrefetch success")
            }, {})
    }

    fun isPlaying() = player?.playWhenReady == true

    fun startPlayback() {
        player?.playWhenReady = true
    }

    fun pausePlayback() {
        player?.playWhenReady = false
    }

    /**
     * Release and remove listener for exo player.
     * We do this as soon as player is not needed anymore.
     */
    fun releaseExoPlayer(listener: Player.Listener?) {
        listener?.let { player?.removeListener(it) }
        player?.stop()
        player?.release()
        player = null
    }


    fun setSecondaryFeedData(secondaryFeedData: SecondaryFeedData?) {
        this.secondaryFeedData = secondaryFeedData
        // setting up end cursor
        secondaryFeedData?.endCursor?.let { secondaryFeedQueryState.endCursor = it }
    }

    fun setSharedLinkPostId(postId: String?){
        this.sharedPostId = postId
    }

    fun resetPrimaryQuery() = primaryFeedQueryState.reset()
}