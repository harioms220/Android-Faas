package com.rizzle.sdk.network.graphql

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.rizzle.sdk.network.DeltaFile
import com.rizzle.sdk.network.NetworkUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * this abstract class is used for making various network calls
 * purpose of this class is to define the structure of methods below and
 * write concrete implementations in the class which implements current class, for better readability and maintenance purpose
 * various remote actions like fetching data, files download and upload will be achieved
 * @property BASE_URL is used for setting API url end point from client using this module
 * @property graphQuery is used for making single graphQL queries
 * @property downloadFile is used for making files download from given URL
 */
abstract class BaseNetworkClient {

    private val TAG = javaClass.simpleName
    abstract var BASE_URL: String
    abstract var isReleaseMode: Boolean

    // client used for making queries
    abstract var graphClient: OkHttpClient

    // client used for downloading purpose
    protected val okHttpClient = OkHttpClient()

    private val EMPTY_STRING = ""
    open val TIME_OUT = 10L //value in seconds

    protected abstract fun graphQuery(
        query: Pair<String, String>,
        headers: Map<String, String>? = null,
        timeOut: Long? = null
    ): Single<String>

    internal fun makeQuery(
        query: Pair<String, String>,
        headers: Map<String, String>? = null,
        timeOut: Long? = null
    ): Single<String> {
        val commonHeaders = getCommonHeaders(NetworkUtils.application)
        headers?.let { commonHeaders.putAll(it) }
        return graphQuery(query, commonHeaders, timeOut)
    }

    abstract fun downloadFile(url: String, file: File): Completable

     /**
     * Private utilities
     */
    protected fun getGeneralOkhttpBuilder(forGraphQL: Boolean = false): OkHttpClient.Builder {
        //client for general put requests in application
        val builder: OkHttpClient.Builder = okHttpClient.newBuilder()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = when {
            isReleaseMode -> HttpLoggingInterceptor.Level.NONE
            else -> if (forGraphQL) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(loggingInterceptor)

        if (forGraphQL) {
            DeltaFile.getChuck()?.let { builder.addInterceptor(it) }
            // TIME_OUT for graphql client
            builder.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        } else {
            //default time out for okhttp client is 10 seconds instead of unlimited
            //THEY SHOULD PUT THAT ON A FUCKING BOX
            //setting it to hour now to make sure to give enough time to upload for video
            builder.connectTimeout(30, TimeUnit.SECONDS)
            builder.readTimeout(1, TimeUnit.HOURS)
            builder.writeTimeout(1, TimeUnit.HOURS)
        }

        return builder
    }


    @SuppressLint("HardwareIds")
    protected fun getCommonHeaders(context: Context?): MutableMap<String, String> {
        val deviceId =
            Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)

        val map = mutableMapOf(
            "clientinfo-ostype" to "ANDROID",
            "clientinfo-osversion" to Build.VERSION.RELEASE,
            "clientinfo-deviceid" to deviceId,
            "clientinfo-devicemodel" to "${Build.MANUFACTURER}:${Build.MODEL}", //deprecated, do not use anymore
            "clientinfo-manufacturer" to Build.MANUFACTURER,
            "clientinfo-model" to Build.MODEL,
            "clientinfo-hardware" to Build.HARDWARE,
        )

        NetworkUtils.getVersionCode().takeIf { it != 0L }?.let { map["clientinfo-appversioncode"] = it.toString() }
        NetworkUtils.getVersionName().takeIf { it.isNotEmpty() }?.let { map["clientinfo-appversion"] = it }
        NetworkUtils.getClientApiKey().takeIf { it.isNotEmpty() }?.let { map["clientinfo-apikey"] = it }

        return map
    }
}