package com.rizzle.sdk.faas.uistylers.models

import androidx.annotation.RestrictTo

/**
 * Class containing remote ui configurations like fonts, textStyles, icons and colors.
 * */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal data class Config(
    var textStyles: List<TextStyle>,
    var fonts: List<FontConfig>,
    var colors: List<ColorConfig>,
    var icons: List<IconConfig>,
    val cardCornerRadius: Float,
    val pillCornerRadius: Float,
    var version: Int
)

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal data class AppConfig(
    var config: Config
)