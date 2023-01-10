package com.rizzle.sdk.faas.models

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class Track(
    @ColumnInfo(name = "trackId")
    var id: String? = null,
    @ColumnInfo(name = "trackTitle")
    var title: String? = null,
    @ColumnInfo(name = "trackViewCount")
    var viewCount: String? = null,
    @ColumnInfo(name = "trackPostCount")
    var postCount: String? = null,
    @Embedded
    var audio: TrackAudio? = null,
    var artist: String? = null,
    var albumTitle: String? = null
)

data class TrackAudio(
    @ColumnInfo(name = "trackAudioDuration")
    var duration: Int? = null,
    var trackUrl: String? = null,
    var trackImageUrl: String? = null
)