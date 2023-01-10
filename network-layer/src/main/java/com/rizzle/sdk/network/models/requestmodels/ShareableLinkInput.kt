package com.rizzle.sdk.network.models.requestmodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ShareableLinkInput(
    var objectId: String,
    var objectType: ShareObject,
    var platform: ShareType = ShareType.UNKNOWN,
    var timeOfActionInMillis: Int
)

@Parcelize
enum class ShareObject : Parcelable {
    POST, TRACK, HASHTAG
}

enum class ShareType{
    WHATSAPP,
    FACEBOOK,
    INSTAGRAM,
    INSTAGRAM_STORIES,
    SHORTS,
    SNAPCHAT,
    TELEGRAM,
    MESSENGER,
    TWITTER,
    MESSAGE,
    MORE,
    UNKNOWN;
}
