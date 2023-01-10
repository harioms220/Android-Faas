package com.rizzle.sdk.network.graphql

import com.rizzle.sdk.network.NetworkUtils
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Completable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import java.util.concurrent.TimeUnit

class OkHttpNetworkClient(appBaseUrl: String? = null) : BaseNetworkClient() {

    private val TAG = javaClass.simpleName
    private val GRAPHQL_API: String by lazy { "${BASE_URL}/graphql" }

    private val JSON = jsonMediaType.toMediaTypeOrNull()

    companion object {
        //media type for graphql request body
        private const val jsonMediaType = "application/json; charset=utf-8"
    }

    /**
     * Just a regular function to perform an OkHttp Request using a GraphQL Query
     * Tries to create a request for a given query pair and executes the query.
     *
     * @see Queries
     * @return response in string format
     * */
    private fun baseQuery(
        query: Pair<String, String>,
        headers: Map<String, String>? = null,
        timeOut: Long? = null
    ): Response {
        val requestBody = query.second.toRequestBody(JSON)
        val requestBuilder = Request.Builder()
            .url(GRAPHQL_API)
            .post(requestBody)

        headers?.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        val client = if (timeOut != null) {
            graphClient.newBuilder()
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .build()
        } else {
            graphClient
        }

        //https://github.com/square/okhttp/issues/1240
        //string() method can only be called once, if tried calling again, exception is thrown, app crashes
        return client.newCall(request).execute()
    }

    override var BASE_URL: String = appBaseUrl ?: ""
    override var isReleaseMode: Boolean = false
    override var graphClient = getGeneralOkhttpBuilder(forGraphQL = true).build()

    override fun graphQuery(
        query: Pair<String, String>,
        headers: Map<String, String>?,
        timeOut: Long?
    ): Single<String> {
        Timber.tag(TAG).d("headers: $headers")
        return Single.create {
            var response: Response? = null
            try {
                response = baseQuery(query, headers, timeOut)

                if (it.isDisposed) return@create

                try {
                    val responseJson = JSONObject(response.body?.string() ?: "")
                    when {
                        //Has data block available in response
                        responseJson.hasAndChildrenNotNull("data") -> {
                            it.onSuccess(
                                responseJson.getJSONObject("data").get(query.first).toString()
                            )
                        }
                        //if any error
                        responseJson.has("errors") -> {
                            val e = extractError(responseJson).apply {
                                this?.query = query.first
                            }?.let { it1 ->
                                ApiErrorException(
                                    it1
                                )
                            }
                            e?.let { it1 -> it.onError(it1) }
                        }
                        else -> it.onError(Exception("Unknown error in request")) //Shouldn't happen
                    }
                } catch (e: Exception) {
                    it.onError(e)
                }
            } catch (e: IOException) {
                Timber.tag(TAG).e(e)
                if (!it.isDisposed) {
                    it.onError(e)
                }
            } finally {
                response?.close()
            }
        }
    }
    /**
     * This function will check if the provided key
     * - Is present
     * - Is not null
     * - Not every child is null (only immediate, not recursive)
     * */
    private fun JSONObject.hasAndChildrenNotNull(key: String): Boolean {
        if (has(key) && optJSONObject(key) != null) {
            val dataBlock = optJSONObject(key)
            dataBlock?.keys()?.forEach {
                val value = dataBlock.optJSONObject(it)
                if (value != null) {
                    return true
                }
            }
        }
        return false
    }

    override fun downloadFile(url: String, file: File): Completable {
        return Completable.defer {
            var completableToReturn: Completable
            var input: BufferedInputStream? = null
            val output: OutputStream = file.outputStream()
            var response: Response? = null
            try {
                val call = okHttpClient.newCall(Request.Builder().url(url).build())

                response = call.execute()

                input = BufferedInputStream(response.body?.byteStream())

                val data = ByteArray(1024)

                val contentLength = response.body?.contentLength() ?: 0L

                var totalDownloaded: Long = 0

                var count: Int //tmp variable

                while (true) {
                    count = input.read(data)
                    if (count == -1) break
                    totalDownloaded += count
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()
                response.close()
                completableToReturn = if (totalDownloaded == contentLength) {
                    Completable.complete()
                } else {
                    file.delete()
                    Completable.error(Exception("Downloading error: $url"))
                }
            } catch (e: Exception) {
                file.delete()
                completableToReturn = Completable.error(Exception("Downloading error : $url", e))
            } finally {
                output.flush()
                output.close()
                input?.close()
                response?.close()
            }

            completableToReturn
        }
    }

    private fun extractError(responseJson: JSONObject) = NetworkUtils.jsonConverter.convertJsonToPojo(responseJson.getJSONArray("errors").getJSONObject(0).toString(), ApiError::class.java)

}