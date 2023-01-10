package com.rizzle.sdk.faas.autolink

import android.text.TextPaint
import android.text.style.TypefaceSpan

/**
 * Type face span with color changing capability
 *
 * Author: dummy-amit
 */
class ColoredTypeFaceSpan(family: String, var color: Int) : TypefaceSpan(family) {
    override fun updateDrawState(ds: TextPaint) {
        if (color != -1) ds.color = color
        super.updateDrawState(ds)
    }
}