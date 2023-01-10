package com.rizzle.sdk.faas.models

data class NewFeed(
    var posts: List<Post>,
    var isRefresh: Boolean,
    var scrollToPosition: Int? = null
)