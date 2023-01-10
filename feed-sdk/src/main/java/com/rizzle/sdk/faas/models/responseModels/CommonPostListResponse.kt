package com.rizzle.sdk.faas.models.responseModels

import com.rizzle.sdk.faas.models.HashTag
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.models.Track

data class CommonPostListResponse(val posts: PaginatedResponse<Post>?)

data class HashTagDetails(val hashtag:HashTag)

data class TrackDetails(val track:Track)