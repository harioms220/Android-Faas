package com.rizzle.sdk.faas.customviews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.uistylers.ColorType
import com.rizzle.sdk.faas.uistylers.StyleType
import com.rizzle.sdk.faas.uistylers.UiConfig
import com.rizzle.sdk.faas.uistylers.models.TextStyle
import timber.log.Timber

/**
 * Custom TextView used for dynamic styling support
 * */
class StyledTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attributeSet, defStyleAttr) {

    private val tag = javaClass.name

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.StyledTextView)
        val styleIndex = typedArray.getInteger(R.styleable.StyledTextView_styleType, -1)
        val colorIndex = typedArray.getInteger(R.styleable.StyledTextView_colorType, -1)
        typedArray.recycle()
        setStyleType(styleIndex)
        setTextColorFromColorType(colorIndex)
    }

    private fun setTextColorFromColorType(colorIndex: Int) {
        if (colorIndex < 0) {
            Timber.tag(tag).d("no colorType provided, default will be used")
            return
        }
        UiConfig.ColorFactory.getColor(ColorType.values()[colorIndex])?.let { setTextColor(it) }
    }

    private fun setStyleType(styleIndex: Int) {
        if (styleIndex < 0) {
            Timber.tag(tag).d("no configuration available yet, fallback color will be used")
            return
        }
        UiConfig.TextStyleFactory.getTextStyle(StyleType.values()[styleIndex])?.let { setStyle(it) }
            ?: Timber.tag(tag).d("no configuration available yet, fallback style will be used")
    }

    private fun setStyle(style: TextStyle?) {
        try {
            style?.run {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                typeface = Typeface.createFromFile(fontConfig?.absoluteFilePath)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}