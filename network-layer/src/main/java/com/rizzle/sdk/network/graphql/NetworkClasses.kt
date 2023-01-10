package com.rizzle.sdk.network.graphql

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * Request body of okhttp modified to give upload progress
 */
class ProgressRequestBody(private var mDelegate: RequestBody, private var mListener: Listener?) : RequestBody() {
    private lateinit var mCountingSink: CountingSink

    override fun contentType(): MediaType? {
        return mDelegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return mDelegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        mCountingSink = CountingSink(sink)
        val bufferedSink = mCountingSink.buffer()
        mDelegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            bytesWritten += byteCount
            mListener?.onProgress((100f * bytesWritten / contentLength()).toInt())
            super.write(source, byteCount)
        }
    }

    interface Listener {
        fun onProgress(progress: Int)
    }
}

class Progress(
    var progressPercent: Int = 0,
    var progressText: String? = null,
    var downloadedSize: Long = 0,
) {
    override fun toString(): String {
        return "Progress $progressPercent $progressText $downloadedSize"
    }
}

data class ApiError(
    val message: String = "",
    val displayMessage: String = "",
    val code: Int? = null,
    val name: String = "",
    val path: List<String> = listOf(),
    var query: String? = null
)

class ApiErrorException(error: ApiError) : Exception("Error $error")

class DownloadCancelled : Exception()