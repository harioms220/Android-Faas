package com.rizzle.sdk.faas.customviews

import android.content.Context
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.uistylers.IconType
import com.rizzle.sdk.faas.uistylers.UiConfig
import com.rizzle.sdk.faas.uistylers.models.IconConfig
import timber.log.Timber
import java.io.File

/**
 * Custom ImageView used for dynamically icon changing support.
 * Icon must belong to one of these [IconType].
 * If configuration is not available, then fallback icons will be used.
 * */
class StyledImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attributeSet, defStyleAttr) {

    private val tag = javaClass.name

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.StyledImageView)
        if (typedArray.hasValue(0).not()) {
            throw java.lang.RuntimeException()
        }
        val iconIndex = typedArray.getInteger(R.styleable.StyledImageView_iconType, -1)
        setIconType(iconIndex)
        typedArray.recycle()
    }

    private fun setIconType(iconIndex: Int) {
        if (iconIndex < 0) {
            Timber.tag(tag).d("no iconType provided, default will be used")
            return
        }
        UiConfig.IconFactory.getIcon(IconType.values()[iconIndex])?.let { setIcon(it)}
            ?: Timber.tag(tag).d("no configuration available yet, fallback icon will be used")
    }

    private fun setIcon(iconConfig: IconConfig?) {
        try {
            loadImage(File("${UiConfig.iconsPath}/${iconConfig?.filePath}"))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * function handling svg and png type files and displaying it in imageview
     */
    private fun loadImage(file: File) {
        try {
            when (file.extension) {
                "svg" -> {
                    val svg = SVG.getFromInputStream(file.inputStream())
                    val picture: Picture = svg.renderToPicture()
                    val drawable = PictureDrawable(picture)
                    setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
                    setImageDrawable(drawable)
                }

                "png" -> Glide.with(context).load(file).into(this)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}