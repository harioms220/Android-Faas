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
 * Custom ImageView used for toggling two icons based on [iconSelected] value.
 * If [iconSelected] is true then [IconType.IC_LIKE] will be used,
 * else [IconType.IC_UNLIKE] will be used.
 * If configuration is not available, then fallback icons will be used.
 * */
class LikeUnlikeImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attributeSet, defStyleAttr) {

    private val tag = javaClass.name

    var iconSelected = false
        set(value) {
            field = value
            setIconType(field)
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LikeUnlikeImageView)
        iconSelected = typedArray.getBoolean(R.styleable.LikeUnlikeImageView_iconSelected, false)
        typedArray.recycle()
    }

    private fun setIconType(isSelected: Boolean) {
        val iconConfig =
            if (isSelected) UiConfig.IconFactory.getIcon(IconType.IC_LIKE)
            else UiConfig.IconFactory.getIcon(IconType.IC_UNLIKE)
        iconConfig?.let { setIcon(it) }
            ?: kotlin.run {
                Timber.tag(tag).d("no configuration available yet, fallback icon will be used")
                val icon = if(isSelected) R.drawable.ic_heart_red else R.drawable.ic_heart_white
                this.setImageResource(icon)
            }
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