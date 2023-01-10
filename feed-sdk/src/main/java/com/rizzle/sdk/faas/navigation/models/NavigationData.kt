package com.rizzle.sdk.faas.navigation.models

import android.os.Bundle

data class NavigationData(
    val data: Bundle,
    val action: NavigationAction
)

enum class NavigationAction{
    ACTION_HASHTAG_SCREEN,
    ACTION_FEED_SCREEN,
    ACTION_TRACK_SCREEN,
    ACTION_SHARE_SHEET,
    ACTION_REPORT_BOTTOM_SHEET,
    ACTION_REPORT_DIALOG,
}