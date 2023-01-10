package com.rizzle.sdk.faas.uistylers.models

import com.rizzle.sdk.faas.uistylers.ClientOnly
import com.rizzle.sdk.faas.uistylers.UiConfig

data class FontConfig(
    var name: String,
    var fontUrl: String,
    var extension: String
) {
    /**
     * relative path of the font file (.ttf file or other font file)
     */
    @ClientOnly
    val relativeFilePath: String
        get() = "/$name.$extension"

    val absoluteFilePath get() = "${UiConfig.fontsPath}$relativeFilePath"
}