package com.rizzle.sdk.faas.shimmer

import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import com.faltenreich.skeletonlayout.createSkeleton
import com.faltenreich.skeletonlayout.mask.SkeletonShimmerDirection
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.utils.InternalUtils.color

/**
 * Helper class to create Shimmer/ Skeleton instance.
 */
object ShimmerEffect {

    private const val LIST_ITEM_COUNT_DEFAULT = 3
    private val DEFAULT_TYPE = Type.VIEWGROUP

    enum class Type {
        /**
         * Show lazy loading for a viewgroup
         */
        VIEWGROUP,

        /**
         * Show lazy loading for a recyclerview
         */
        RECYCLERVIEW,

        /**
         * Show lazy loading for a viewpager
         */
        VIEWPAGER
    }

    /**
     * A wrapper function to unify calls for skeleton/ shimmer effect.
     */
    private fun create(type: Type, viewGroup: ViewGroup, @LayoutRes listItemLayoutResId: Int, itemCount: Int = LIST_ITEM_COUNT_DEFAULT): Skeleton {

        return when (type) {
            Type.RECYCLERVIEW -> {
                (viewGroup as RecyclerView).applySkeleton(listItemLayoutResId, itemCount)
            }
            Type.VIEWPAGER -> {
                (viewGroup as ViewPager2).applySkeleton(listItemLayoutResId, itemCount)
            }
            Type.VIEWGROUP -> {
                (viewGroup).createSkeleton()
            }
        }
    }


    class SkeletonBuilder(
        private var type: Type,
        private var viewGroup: ViewGroup,
        @LayoutRes private var listItemLayoutResId: Int,
        private var itemCount: Int = LIST_ITEM_COUNT_DEFAULT
    ) {
        private var shimmerDirection: SkeletonShimmerDirection = SkeletonShimmerDirection.LEFT_TO_RIGHT
        private var shimmerDurationInMillis: Long = 800
        private var shimmerAngle: Int = 0
        private var maskColor: Int = color(R.color.shimmer_mask_color)
        private var shimmerColor: Int = color(R.color.gray_light_alt)
        private var maskCornerRadius = 0f

        fun shimmerDirection(shimmerDirection: SkeletonShimmerDirection) = apply { this.shimmerDirection = shimmerDirection }
        fun shimmerDurationInMillis(shimmerDurationInMills: Long) = apply { this.shimmerDurationInMillis = shimmerDurationInMills }
        fun shimmerAngle(shimmerAngle: Int) = apply { this.shimmerAngle = shimmerAngle }
        fun maskColor(@ColorRes maskColor: Int) = apply { this.maskColor = color(maskColor) }
        fun shimmerColor(@ColorRes shimmerColor: Int) = apply { this.shimmerColor = color(shimmerColor) }
        fun maskCornerRadius(maskCornerRadius: Float) = apply { this.maskCornerRadius = maskCornerRadius }

        fun build(): Skeleton {

            return create(type, viewGroup, listItemLayoutResId, itemCount).apply {
                this@SkeletonBuilder.let { builder ->
                    shimmerDirection = builder.shimmerDirection
                    shimmerDurationInMillis = builder.shimmerDurationInMillis
                    shimmerAngle = builder.shimmerAngle
                    maskColor = builder.maskColor
                    shimmerColor = builder.shimmerColor
                    maskCornerRadius = builder.maskCornerRadius
                }
            }
        }
    }

}