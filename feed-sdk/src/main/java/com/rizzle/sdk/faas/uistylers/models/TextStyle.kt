package com.rizzle.sdk.faas.uistylers.models

import com.rizzle.sdk.faas.customviews.StyledTextView
import com.rizzle.sdk.faas.uistylers.ClientOnly

/**
 * This class holds the style attributes to be used internally in the SDK for [StyledTextView].
 */
data class TextStyle(

    /**
     * name of the style
     */
    var name: String = "",

    /**
     * size of the text in sp
     */
    var size: Float = 14f,

    /**
     * font name  for this style
     */
    var font: String = ""
) {
    /**
     * font configuration of this [TextStyle]
     */
    @ClientOnly
    var fontConfig: FontConfig? = null
}