package com.rizzle.sdk.faas.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.MediaCodecList
import android.media.MediaFormat
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import com.rizzle.sdk.network.apis.NetworkApis
import com.rizzle.sdk.network.apis.NetworkApisImpl
import com.rizzle.sdk.network.serialzers.JsonPojoConverter
import com.rizzle.sdk.network.serialzers.MoshiPojoConverter
import timber.log.Timber
import java.io.File


@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object InternalUtils {
    private val TAG = javaClass.simpleName
    private var _application: Application? = null
    val application: Application
        get() = _application!!

    var domainUrl: String? = null

    fun setApplicationContext(cont: Context) {
        _application = cont.applicationContext as Application
    }

    /**
     * Use this method to get all the activities present in the application project
     * including the activities of library.
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getActivityList(): Array<ActivityInfo?>? {
        val pm: PackageManager = application.packageManager
        val info: PackageInfo = pm.getPackageInfo(application.packageName, PackageManager.GET_ACTIVITIES)
        return info.activities
    }

    /** Use this to serialize/deserialize the data */
    internal val jsonSerializer: JsonPojoConverter = MoshiPojoConverter()

    /** Use this to query, post, download any network related thing*/
    internal val networkApis: NetworkApis by lazy { NetworkApisImpl() }

    /**
     * get cache directory for application
     */
    fun getCacheDir(): File = application.cacheDir

    /**
     * get files directory for application
     */
    fun getFilesDir(): File = application.filesDir


    // get the files directory in data/data/application_package_name/files
    fun filesDir(): File = application.filesDir

    /*
    Some utility functions which reduces lot of boilerplate from our codebase to access resources
     */
    fun string(@StringRes id: Int): String = application.resources.getString(id)
    fun stringQty(@PluralsRes id: Int, qty: Int): String = application.resources.getQuantityString(id, qty)

    fun color(@ColorRes id: Int): Int = ResourcesCompat.getColor(application.resources, id, null)
    fun color(hexadecimalColor: String?): Int = Color.parseColor(hexadecimalColor)

    fun font(@FontRes id: Int): Typeface {
        var typeface: Typeface = Typeface.DEFAULT
        try {
            typeface = ResourcesCompat.getFont(application, id) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
        return typeface
    }

    fun dimen(@DimenRes id: Int): Float = application.resources.getDimension(id)
    fun integer(@IntegerRes id: Int): Int = application.resources.getInteger(id)
    fun drawable(@DrawableRes id: Int): Drawable? = ResourcesCompat.getDrawable(application.resources, id, null)

    /**
     * Returns true if at least one hevc decoder which supports passed resolutions is found
     */
    fun isHevcDecoderSupportingPostResolution(resolutions: Resolution): Boolean {
        // get the list of available codecs
        try {
            val list = MediaCodecList(MediaCodecList.ALL_CODECS)
            val codecInfos = list.codecInfos
            val numCodecs = codecInfos.size
            for (i in 0 until numCodecs) {
                val codecInfo = codecInfos[i]
                if (codecInfo.isEncoder) { // skipp encoders
                    continue
                }
                // select first codec that match a specific MIME type and color format
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (types[j].equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)) {
                        val cap = codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_HEVC)
                        val cap1 = cap.videoCapabilities
                        val range = cap1.getSupportedHeightsFor(resolutions.width)
                        if (range.upper >= resolutions.height) {
                            Timber.tag(TAG).d("isHevcDecoderSupporting$resolutions Hevc decoder supports 720*1280")
                            return true
                        }
                    }
                }
            }
            Timber.tag(TAG).d("isHevcDecoderSupporting720x1280 Hevc decoder does not support 720*1280")
            return false
        } catch (e: Exception) {
            return false
        }
    }

    val densityDpi by lazy { application.resources.displayMetrics.densityDpi }

    val pref: SharedPreferences by lazy { application.getSharedPreferences("rizzle-library", Context.MODE_PRIVATE) }

}