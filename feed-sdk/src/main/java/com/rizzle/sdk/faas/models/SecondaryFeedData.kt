package com.rizzle.sdk.faas.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SecondaryFeedData(
    var postsIds: List<String>,
    var objectId: String,
    var launchedFrom: SecondaryFeedLaunchSource,
    var currentPositionInFeed: Int,
    var endCursor: String?
): Parcelable

@Parcelize
enum class SecondaryFeedLaunchSource : Parcelable {
    HASHTAG, TRACK
}