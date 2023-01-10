package com.rizzle.sdk.faas.utils

import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.rizzle.sdk.faas.utils.InternalUtils.getCacheDir
import java.io.File

internal object VideoCache {
    private var sDownloadCache: SimpleCache? = null
    private var CACHE_SIZE = 100 * 1024 * 1024  // first number in MB

    @get:Synchronized
    val instance: SimpleCache
        get() {
            if (sDownloadCache == null) {
                val provider = StandaloneDatabaseProvider(InternalUtils.application)
                sDownloadCache = SimpleCache(File(getCacheDir(), "exoCache"), LeastRecentlyUsedCacheEvictor((CACHE_SIZE).toLong()), provider)
            }
            return sDownloadCache!!
        }
}