package com.rizzle.sdk.faas.models.responseModels

data class PaginatedResponse<T>(
    val nodes: MutableList<T> = mutableListOf(),
    val pageInfo: PageInfo? = null
)