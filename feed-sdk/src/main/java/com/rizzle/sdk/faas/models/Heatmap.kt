package com.rizzle.sdk.faas.models

import com.rizzle.sdk.network.models.HeatmapActionEnum

data class Heatmap(
    var action: HeatmapActionEnum? = null,
    var startTime: Int? = null,
    var duration: Int? = null,
    var opacity: Int? = null,
    var iconUrl: String? = null,
    var hexColorCode: String? = null,
)