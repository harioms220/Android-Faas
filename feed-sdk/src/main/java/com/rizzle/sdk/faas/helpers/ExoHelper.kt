package com.rizzle.sdk.faas.helpers

import android.net.Uri
import android.os.Looper
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.faas.utils.VideoCache


/**
 * Singleton exo player helper class to avoid repeating same code again and again in codebase for
 * initializing exo player instance and media sources
 */
internal object ExoHelper {

    internal const val PREFETCH_SIZE = (0.3 * 1024 * 1024).toLong()  //300kb

    //to map cache data spec to uri uniquely
    private val cacheKeyFactory = CacheKeyFactory { dataSpec -> dataSpec.uri.toString() }

    var cacheDataSourceFactory: CacheDataSource.Factory

    private val defaultFactory by lazy { DefaultDataSource.Factory(InternalUtils.application) }

    private val httpDataSourceFactory by lazy { DefaultHttpDataSource.Factory() }


    private const val MIN_BUFFER_DUR_FEED = 500
    private const val MAX_BUFFER_DUR_FEED = 15000


    init {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(InternalUtils.application).build()

        // Build data source factory with cache enabled, if data is available in cache it will return immediately, otherwise it will open a new connection to get the data.
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Rizzle")
            .setTransferListener(bandwidthMeter)

        val cacheSinkFactory = CacheDataSink.Factory()
            .setCache(VideoCache.instance)
            .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(VideoCache.instance)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setCacheWriteDataSinkFactory(cacheSinkFactory)
            .setFlags(0)
            .setEventListener(null)
            .setCacheKeyFactory(cacheKeyFactory)
    }

    /**
     * @param skipAudio boolean
     * @param looper optional looper thread to run this exo instance on
     * @param loadControl optional load control to change behaviour of buffering
     */
    private fun createExoInstance(skipAudio: Boolean, looper: Looper? = null, loadControl: DefaultLoadControl? = null): ExoPlayer {
        val builder = ExoPlayer
            .Builder(InternalUtils.application)

        if (looper != null)
            builder.setLooper(looper)

        if (loadControl != null)
            builder.setLoadControl(loadControl)

        return builder.build()
            .apply {
                if (skipAudio)
                    trackSelectionParameters = trackSelectionParameters
                        .buildUpon()
                        .setDisabledTrackTypes(setOf(C.TRACK_TYPE_AUDIO))
                        .build()
            }
    }

    /**
     * returns exo instance for starting playback fast
     * this is achieved by tweaking load control parameters
     */
    fun getFastStartExoInstance(): ExoPlayer {
        val builder = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER_DUR_FEED,
                MAX_BUFFER_DUR_FEED,
                MIN_BUFFER_DUR_FEED,
                MIN_BUFFER_DUR_FEED
            )

        return createExoInstance(false, loadControl = builder.build())
    }

    /**
     * get cache data source for given uri
     * setCacheKey : set cache key while building data source, useful in pre-fetching
     */
    fun getCacheDataSource(uri: Uri?, setCacheKey: Boolean = false): BaseMediaSource? {
        if (uri == null) return null
        val factory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
        val mediaItemFactory = MediaItem.Builder().setUri(uri)
        if (setCacheKey) mediaItemFactory.setCustomCacheKey(uri.toString())
        return factory.createMediaSource(mediaItemFactory.build())
    }

    fun getDataSpec(uri: Uri, contentLength: Long) = DataSpec.Builder()
        .setUri(uri)
        .setPosition(0)
        .setLength(contentLength)
        .setKey(uri.toString())
        .build()


    /**
     * Get default exo instance with all default parameters and cache data factory
     * @param skipAudio optional parameter to skip playing audio for given media in exo instance
     */
    fun getDefaultExoInstance(skipAudio: Boolean = false) = createExoInstance(skipAudio)

    /**
     * Get data source for playing offline videos for given uri
     * @param uri uri of the media
     * @return MediaSource with given uri
     */
    fun getMediaFromUri(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(defaultFactory).createMediaSource(MediaItem.fromUri(uri))
    }

    /**
     * Get http data source for given url
     * @param sourceUrl url of the media
     * @return MediaSource with given uri
     */

    fun getHttpDataSource(sourceUrl: String): MediaSource {
        return ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(MediaItem.fromUri(sourceUrl))
    }


    }

//region exoplayer extensions

/**
 * [ExoPlayer.prepare] is deprecated
 * using this good boy to avoid replacing 2 lines everywhere in code
 * blocking method
 */
fun ExoPlayer.prepareGood(mediaSource: MediaSource) {
    setMediaSource(mediaSource)
    prepare()
}

//endregion

