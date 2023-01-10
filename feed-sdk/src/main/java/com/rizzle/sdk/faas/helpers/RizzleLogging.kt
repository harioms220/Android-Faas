package com.rizzle.sdk.faas.helpers

import timber.log.Timber

/**
 * Class for logging errors and any useful information of sdk user to the
 * server to track any issues in future.
 * */
object RizzleLogging {

    private val TAG = javaClass.simpleName

    fun logError(ex: Throwable) {
        // Todo yet to implement
        Timber.tag(TAG).e(ex)
    }

}