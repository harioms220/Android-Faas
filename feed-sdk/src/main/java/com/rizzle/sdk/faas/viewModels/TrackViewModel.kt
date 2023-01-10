package com.rizzle.sdk.faas.viewModels

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.rizzle.sdk.faas.helpers.ExoHelper
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.models.Track
import com.rizzle.sdk.faas.repos.TrackRepo
import com.rizzle.sdk.faas.views.baseViews.QueryState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

class TrackViewModel : BaseViewModel() {

    private val TAG = javaClass.simpleName
    private var player: ExoPlayer? = null

    private val repo = TrackRepo()
    var sectionsQueryState = QueryState()

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
            Timber.tag(TAG).d("old player instance used")
            player
        } else {
            player = ExoHelper.getDefaultExoInstance()
            Timber.tag(TAG).d("player instance created")
            player
        }
    }


    /**
     *Here we are setting release Exo Player
     */
    fun releaseExoPlayer(listener: Player.Listener?) {
        Timber.tag(TAG).d("releasing the exoplayer")
        pauseExoPlayer()
        player?.apply {
            stop()
            release()
            listener?.let { removeListener(it) }
        }
        player = null
    }

    fun pauseExoPlayer() {
        Timber.tag(TAG).d("pausing the player")
        player?.pause()
    }

    fun playExoPlayer() {
        Timber.tag(TAG).d("resuming the player")
        player?.play()
    }

    fun getTrackPosts(trackId: String): Single<MutableList<Post>> {
        return sectionsQueryState.preCheck {
            repo.getTrackPosts(trackId, sectionsQueryState.endCursor)
                .map { commonPostListResponse ->
                    sectionsQueryState.endCursor = commonPostListResponse.posts?.pageInfo?.endCursor
                    commonPostListResponse.posts?.nodes ?: mutableListOf()
                }
        }
    }

    fun getTrackInfo(trackId: String): Single<Track> {
        return repo.getTrackInfo(trackId).observeOn(AndroidSchedulers.mainThread())
    }


    override fun onCleared() {
        super.onCleared()
        repo.clear()
        sectionsQueryState.reset()
    }
}
