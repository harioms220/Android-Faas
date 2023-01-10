package com.rizzle.sdk.faas.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rizzle.sdk.faas.BuildConfig
import com.rizzle.sdk.faas.feed.FeedActivity
import com.rizzle.sdk.faas.uistylers.UiConfig
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.network.NetworkUtils
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import timber.log.Timber
import timber.log.Timber.Forest.plant


/**
 *  This class will be used by the client to initialise the SDK.
 */
object FeedSdkInitializer: DefaultLifecycleObserver{

    private val TAG = javaClass.simpleName

    const val SHARE_LINK_DATA = "SHARE_LINK_DATA"
    /**
     * This function will be used to store the application context in the library.
     * Also use this function to setup any other third party library which require application context
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    internal fun install(context: Context) {
        InternalUtils.setApplicationContext(context)
        NetworkUtils.setApplicationContext(context)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // initialise Timber
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }

        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) {
                // Merely log undeliverable exceptions
                Timber.tag(TAG).e(e)
            } else {
                // Forward all others to current thread's uncaught exception handler
                Thread.currentThread().also { thread ->
                    thread.uncaughtExceptionHandler?.uncaughtException(thread, e)
                }
            }
        }

        UiConfig.fetchRemoteUIConfig()
    }

    fun initializeWithShareLink(context: Context, shareLink: String){
        val intent = Intent(context, FeedActivity::class.java)
        intent.putExtra(SHARE_LINK_DATA, shareLink)
        context.startActivity(intent)
    }


    fun setDomainUrl(domainUrl: String){
        InternalUtils.domainUrl = domainUrl
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.tag(TAG).d("Application stopping")
        UiConfig.cancelDownloadingAssets()
    }

    fun isFaasUrI(uri: Uri): Boolean {
        return ShareLinkGenerator.isFaasShareUri(uri)
    }
}