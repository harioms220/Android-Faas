package com.rizzle.sdk.faas.uistylers

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.autolink.AutoLinkTextView
import com.rizzle.sdk.faas.utils.InternalUtils.color
import timber.log.Timber


private const val TAG = "StylingExtensions"

fun CardView.setBackgroundColor(colorType: ColorType) {
    UiConfig.ColorFactory.getColor(colorType)?.let { setCardBackgroundColor(it) }
}

fun ViewGroup.setBackground(colorType: ColorType) {
    UiConfig.ColorFactory.getColor(colorType)?.let { setBackgroundColor(it) }
}

fun TextView.setTextColor(colorType: ColorType) {
    UiConfig.ColorFactory.getColor(colorType)?.let { setTextColor(it) }
}

fun AutoLinkTextView.setStyleType() {
    val style = UiConfig.TextStyleFactory.getTextStyle(StyleType.BODY_1_SEMI_BOLD)
        ?: return
    try {
        style.run {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            typeface = Typeface.createFromFile(fontConfig?.absoluteFilePath)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun CardView.setCardCornerRadius(cornerRadius: Float) {

    if (cornerRadius < 0f) {
        Timber.tag(TAG).d("No Remote Configuration is available using default corner radius")
        return
    }

    try {
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadius, context.resources.displayMetrics)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun TextView.setCapsuleBackground(cornerRadius: Float, borderWidth: Int = 0, colorType: ColorType = ColorType.GRAY_500) {
    try {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)
            setStroke(borderWidth, UiConfig.ColorFactory.getColor(colorType) ?: color(R.color.gray_100))
        }
        background = drawable
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

