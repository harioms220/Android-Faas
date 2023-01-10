package com.rizzle.sdk.faas.models.responseModels

import com.rizzle.sdk.faas.helpers.isNullString

data class PageInfo(var endCursor: String? = null) {
    val noMoreData
        get() = endCursor.isNullString()
}