package com.rizzle.sdk.network.models

import com.rizzle.sdk.network.graphql.ApiError
import org.json.JSONObject

data class MultiQueryResponse(
    var data: JSONObject? = null,
    var errors: List<ApiError>? = null
)