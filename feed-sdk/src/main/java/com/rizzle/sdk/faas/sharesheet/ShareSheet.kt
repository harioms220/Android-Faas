package com.rizzle.sdk.faas.sharesheet

import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.helpers.Constants.EMPTY_STRING
import com.rizzle.sdk.network.models.requestmodels.ShareType


interface AppMeta {
    val packageName: String
        get() = EMPTY_STRING

    val iconRes: Int
        get() = R.drawable.ic_whatsapp

    val displayName: Int
        get() = R.string.text_whatsapp

    val shareType: ShareType
}

enum class ShareTypeDetails: AppMeta {
    WHATSAPP {
        override val packageName = "com.whatsapp"
        override val iconRes: Int = R.drawable.ic_whatsapp
        override val displayName: Int = R.string.text_whatsapp
        override val shareType: ShareType
            get() = ShareType.WHATSAPP
    },
    FACEBOOK {
        override val packageName = "com.facebook.katana"
        override val iconRes: Int = R.drawable.ic_facebook
        override val displayName: Int = R.string.text_facebook
        override val shareType: ShareType
            get() = ShareType.FACEBOOK
    },
    INSTAGRAM {
        override val packageName = "com.instagram.android"
        override val iconRes: Int = R.drawable.ic_insta
        override val displayName: Int = R.string.text_instagram
        override val shareType: ShareType
            get() = ShareType.INSTAGRAM
    },
    SNAPCHAT {
        override val packageName = "com.snapchat.android"
        override val iconRes: Int = R.drawable.ic_snapchat
        override val displayName: Int = R.string.text_snapchat
        override val shareType: ShareType
            get() = ShareType.SNAPCHAT
    },
    TELEGRAM {
        override val packageName = "org.telegram.messenger"
        override val iconRes: Int = R.drawable.ic_telegram
        override val displayName: Int = R.string.text_telegram
        override val shareType: ShareType
            get() = ShareType.TELEGRAM
    },
    MESSENGER {
        override val packageName = "com.facebook.orca"
        override val iconRes: Int = R.drawable.ic_messenger
        override val displayName: Int = R.string.text_messenger
        override val shareType: ShareType
            get() = ShareType.MESSENGER
    },
    TWITTER {
        override val packageName = "com.twitter.android"
        override val iconRes: Int = R.drawable.ic_twitter
        override val displayName: Int = R.string.text_twitter
        override val shareType: ShareType
            get() = ShareType.TWITTER
    },
    MESSAGE {
        override val iconRes: Int = R.drawable.ic_message
        override val displayName: Int = R.string.text_message
        override val shareType: ShareType
            get() = ShareType.MESSAGE
    },

    COPY_LINK{
        override val iconRes: Int = R.drawable.ic_copy_link
        override val displayName: Int = R.string.text_copy_link
        override val shareType: ShareType
            get() = ShareType.MESSAGE
    },

    SHARE_LINK{
        override val iconRes: Int = R.drawable.ic_message
        override val displayName: Int = R.string.text_message
        override val shareType: ShareType
            get() = ShareType.MESSAGE
    },

    MORE {
        override val iconRes: Int = R.drawable.ic_more
        override val displayName: Int = R.string.text_more
        override val shareType: ShareType
            get() = ShareType.MORE
    };

    companion object{
        fun getListOfSocialApps() = mutableListOf(WHATSAPP, INSTAGRAM, FACEBOOK, TELEGRAM, MESSENGER, TWITTER, SNAPCHAT)
    }
}