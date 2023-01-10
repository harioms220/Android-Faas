package com.rizzle.sdk.faas.helpers

import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.rizzle.sdk.faas.BuildConfig
import com.rizzle.sdk.faas.sharesheet.ShareTypeDetails
import com.rizzle.sdk.faas.utils.InternalUtils
import timber.log.Timber
import java.io.File
import java.io.OutputStream

object SystemUtils {

    private val TAG = javaClass.simpleName

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getScopedStorageStream(preparedFile: File): OutputStream? {
        val newVideoDetails = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, preparedFile.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Rizzle")
        }
        val insertedUri = addFileToMediaStore(preparedFile, newVideoDetails)
        return InternalUtils.application.contentResolver.openOutputStream(insertedUri)
    }

    /**
     * Add File to mediaStore and return Uri
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun addFileToMediaStore(file: File, contentValues: ContentValues): Uri {
        val resolver = InternalUtils.application.contentResolver

        //delete if this already exists in mediaStore
        resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Video.Media.DISPLAY_NAME} = '${file.name}'", null)

        return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
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
        val packageManager = InternalUtils.application.packageManager
        val packInfo: PackageInfo?
        var version: String? = null
        try {
            packInfo = packageManager.getPackageInfo(InternalUtils.application.packageName, 0)
            version = packInfo?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version ?: ""
    }

    @Suppress("DEPRECATION")
    fun getVersionCode(): Long {
        val packageManager = InternalUtils.application.packageManager
        val packInfo: PackageInfo?
        var version = 0L
        try {
            packInfo = packageManager.getPackageInfo(InternalUtils.application.packageName, 0)
            version =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    packInfo?.longVersionCode ?: 0
                else
                    packInfo?.versionCode?.toLong() ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }

    /**
     * Gives a list of apps that are available on phone based on [ShareTypeDetails]
     * */
    @Suppress("DEPRECATION")
    fun getAvailableInstalledShareableApps(): List<ApplicationInfo> {
        val flags = PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES or PackageManager.GET_UNINSTALLED_PACKAGES
        val pm: PackageManager = InternalUtils.application.packageManager
        return try {
            val installedAppsList = if (Build.VERSION.SDK_INT >= 33) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(flags.toLong()))
            } else {
                pm.getInstalledApplications(flags)
            }
            installedAppsList.filter { it.packageName in ShareTypeDetails.values().map { it.packageName } }
        } catch (e: Exception) {
            //android.os.DeadSystemException nothing we can do about this, literally
            listOf()
        }
    }
}