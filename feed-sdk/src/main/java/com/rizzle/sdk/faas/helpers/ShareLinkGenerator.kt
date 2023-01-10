package com.rizzle.sdk.faas.helpers

import android.net.Uri
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput
import io.reactivex.rxjava3.core.Single

object ShareLinkGenerator {

    private const val SHARE_KEY_POST = "#faasplayer="
    private const val SHARE_KEY_HASHTAG = "#faashashtag="
    private const val SHARE_KEY_TRACK = "#faastrack="

    fun generateShareLink(shareableLinkInput: ShareableLinkInput): Single<String> {
        return Single.fromCallable {
            val urlBuilder: StringBuilder = java.lang.StringBuilder()
            val baseUrl = InternalUtils.domainUrl
            urlBuilder.apply {
                append(baseUrl?.removeSuffix("/"))
                append("/?")
                when (shareableLinkInput.objectType) {
                    ShareObject.POST -> append(SHARE_KEY_POST)
                    ShareObject.TRACK -> append(SHARE_KEY_TRACK)
                    ShareObject.HASHTAG -> append(SHARE_KEY_HASHTAG)
                }

                append(encode(shareableLinkInput.objectId))
            }

            urlBuilder.toString()
        }
    }

    fun getIdAndType(sharedString: String): Pair<ShareObject, String> {
        val decodedString = decode(sharedString.split("#faasplayer=", "#faashashtag=", "#faastrack=")[1])

        return when {
            sharedString.startsWith(SHARE_KEY_POST) -> {
                Pair(ShareObject.POST, decodedString)
            }
            sharedString.startsWith(SHARE_KEY_HASHTAG) -> {
                Pair(ShareObject.HASHTAG, decodedString)
            }
            else -> {
                Pair(ShareObject.TRACK, decodedString)
            }
        }
    }

    fun encode(data: String): String {
        return data
    }

    fun decode(data: String): String {
        return data
    }

    fun isFaasShareUri(uri: Uri): Boolean {
        return uri.toString().let { it.contains(SHARE_KEY_POST) || it.contains(SHARE_KEY_HASHTAG) || it.contains(SHARE_KEY_TRACK) }
    }
}