package com.rizzle.sdk.faas.navigation.models

import android.os.Build
import android.os.Bundle
import com.rizzle.sdk.faas.models.SecondaryFeedData
import com.rizzle.sdk.faas.navigation.NavigationConstants
import com.rizzle.sdk.network.models.requestmodels.ShareObject

data class HashTagFragmentArgs(
    var postId: String? = null,
    var hashtagName: String,
    var hashTagId: String?
) {
    companion object {
        fun fromBundle(bundle: Bundle): HashTagFragmentArgs {
            val postId = bundle.getString(NavigationConstants.POST_ID, "")
            val hashTagName = bundle.getString(NavigationConstants.HASHTAG_NAME, "")
            val hashTagId = bundle.getString(NavigationConstants.HASHTAG_ID, null)
            return HashTagFragmentArgs(postId, hashTagName, hashTagId)
        }
    }
}

data class TrackFragmentArgs(
    var postId: String? = null,
    var trackId: String
) {
    companion object {
        fun fromBundle(bundle: Bundle): TrackFragmentArgs {
            val postId = bundle.getString(NavigationConstants.POST_ID, "")
            val trackId = bundle.getString(NavigationConstants.TRACK_ID, "")
            return TrackFragmentArgs(postId, trackId)
        }
    }
}

data class FeedFragmentArgs(
    var secondaryFeedData: SecondaryFeedData?,
    var sharedLinkPostId: String?
) {
    companion object {
        @Suppress("DEPRECATION")
        fun fromBundle(bundle: Bundle?): FeedFragmentArgs? {
            return bundle?.run {
                val secondaryFeedData =
                    if (Build.VERSION.SDK_INT >= 33) {
                        getParcelable(NavigationConstants.SECONDARY_FEED_DATA, SecondaryFeedData::class.java)
                    } else {
                        getParcelable(NavigationConstants.SECONDARY_FEED_DATA)
                    }

                val sharedLinkPostId = getString(NavigationConstants.POST_ID)
                FeedFragmentArgs(secondaryFeedData, sharedLinkPostId)
            }
        }
    }
}

data class ShareSheetDialogArgs(
    var id: String,
    var timeOfActionInMillis: Long,
    var shareObject: ShareObject
) {
    companion object {
        @Suppress("DEPRECATION")
        fun fromBundle(bundle: Bundle): ShareSheetDialogArgs {
            val id = bundle.getString(NavigationConstants.OBJECT_ID, "")
            val timeOfActionInMillis = bundle.getLong(NavigationConstants.TIME_OF_ACTION, 0)
            val shareObject = if (Build.VERSION.SDK_INT >= 33) {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT, ShareObject::class.java)!!
            } else {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT)!!
            }
            return ShareSheetDialogArgs(id, timeOfActionInMillis, shareObject)
        }
    }
}

data class ReportBottomSheetDialogArgs(
    var postId: String,
    var shareObject: ShareObject
){
    companion object {
        @Suppress("DEPRECATION")
        fun fromBundle(bundle: Bundle): ReportBottomSheetDialogArgs {
            val postId = bundle.getString(NavigationConstants.OBJECT_ID, "")
            val shareObject = if (Build.VERSION.SDK_INT >= 33) {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT, ShareObject::class.java)!!
            } else {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT)!!
            }

            return ReportBottomSheetDialogArgs(postId, shareObject)
        }
    }
}

data class AlertDialogArgs(
    var shareObject: ShareObject? = null,
    var dialogTitle: String? = null,
    var dialogMessage: String? = null
){
    companion object {
        @Suppress("DEPRECATION")
        fun fromBundle(bundle: Bundle): AlertDialogArgs {
            val dialogTitle = bundle.getString(NavigationConstants.DIALOG_TITLE)
            val dialogMessage = bundle.getString(NavigationConstants.DIALOG_MESSAGE)
            val shareObject: ShareObject? = if (Build.VERSION.SDK_INT >= 33) {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT, ShareObject::class.java)
            } else {
                bundle.getParcelable(NavigationConstants.SHARE_OBJECT)
            }
            return AlertDialogArgs(shareObject, dialogTitle, dialogMessage)
        }
    }
}