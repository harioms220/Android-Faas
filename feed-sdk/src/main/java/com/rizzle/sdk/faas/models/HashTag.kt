package com.rizzle.sdk.faas.models

import androidx.room.ColumnInfo

data class HashTag(
    @ColumnInfo(name = "hashtagDescription")
    var description: String? = null,
    @ColumnInfo(name = "hashtagId")
    var id: String? = null,
    @ColumnInfo(name = "hashtagName")
    var name: String? = null,
    @ColumnInfo(name = "hashtagViewCount")
    var viewCount: String? = null
)