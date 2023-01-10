package com.rizzle.sdk.faas.uistylers.models

import com.rizzle.sdk.faas.uistylers.ClientOnly

data class IconConfig(
    var name: String = "",
    var iconUrl: String = "",
    var type: String = "png"
) {
    @ClientOnly
    val filePath: String
        get() = "$name.$type"
}