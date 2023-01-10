package com.rizzle.sdk.network

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.rizzle.sdk.network.graphql.BaseNetworkClient
import com.rizzle.sdk.network.graphql.OkHttpNetworkClient
import com.rizzle.sdk.network.serialzers.JsonPojoConverter
import com.rizzle.sdk.network.serialzers.MoshiPojoConverter
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.io.OutputStream

object NetworkUtils {
    private val TAG = javaClass.name

    private const val FEED_CLIENT_KEY = "FEED_CLIENT_KEY"
    // a General json serializer to be used throughout the module to convert pojo to json and vice versa.
    val jsonConverter: JsonPojoConverter = MoshiPojoConverter()

    // a General network client to be used throughout the module
    val defaultNetworkClient: BaseNetworkClient by lazy { OkHttpNetworkClient(BuildConfig.BASE_URL) }

    private var _application: Application? = null
    val application: Application
        get() = _application!!

    fun setApplicationContext(cont: Context) {
        _application = cont.applicationContext as Application
    }

    /* Scoped Storage for Android 10 and above
 * link = https://commonsware.com/blog/2019/12/21/scoped-storage-stories-storing-mediastore.html
 **/
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getScopedStorageStreamForVideo(preparedFile: File, context: Context?): OutputStream? {
        val newVideoDetails = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, preparedFile.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Media")
        }
        val insertedUri = addFileToMediaStore(preparedFile, newVideoDetails, context)

        return context?.contentResolver?.openOutputStream(insertedUri)
    }

    /**
     * Add File to mediaStore and return Uri
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun addFileToMediaStore(file: File, contentValues: ContentValues, context: Context?): Uri {
        val resolver = context?.contentResolver

        //delete if this already exists in mediaStore
        resolver?.delete(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Video.Media.DISPLAY_NAME} = '${file.name}'",
            null
        )

        return resolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Unknown Exception occurred when inserting into MediaStore")
    }


    private fun getFileNameFromUrl(uriString: String): String {
        var fileName = uriString.substringAfterLast("/")
        if (uriString.contains(".mp4")) fileName = fileName.substringBefore(".mp4") + ".mp4"
        if (uriString.contains("h264-share")) fileName = fileName.replace(".mp4", "_share.mp4")
        Timber.tag(TAG).d("getFileNameFromUrl $fileName")
        return fileName
    }

    val isReleaseMode = BuildConfig.BUILD_TYPE.contentEquals("release")
    val isDebugMode = BuildConfig.BUILD_TYPE.contentEquals("debug")
    val isQaMode = BuildConfig.BUILD_TYPE.contentEquals("qa")

    fun getVersionName(): String {
        val packageManager = application.packageManager
        val packInfo: PackageInfo?
        var version: String? = null
        try {
            packInfo = packageManager.getPackageInfo(application.packageName, 0)
            version = packInfo?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version ?: ""
    }

    @Suppress("DEPRECATION")
    fun getVersionCode(): Long {
        val packageManager = application.packageManager
        val packInfo: PackageInfo?
        var version = 0L
        try {
            packInfo = packageManager.getPackageInfo(application.packageName, 0)
            version =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    packInfo.longVersionCode
                else
                    packInfo.versionCode.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }

    @Suppress("DEPRECATION")
    fun getClientApiKey(): String {
        try {
            val metaData: Bundle? = application.packageManager
                .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA).metaData
            return metaData?.getString(FEED_CLIENT_KEY).toString() ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}

/**
 * To cancel any queued and running call with the given tag.
 * @param tag : String which was added as tag while creating request body
 */
internal fun OkHttpClient.cancelCallWithTag(tag: String?) {
    dispatcher.queuedCalls().filter { it.request().tag() == tag }.forEach { it.cancel() }
    dispatcher.runningCalls().filter { it.request().tag() == tag }.forEach { it.cancel() }
}