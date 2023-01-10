package com.rizzle.sdk.faas.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView


val linearInterpolator = LinearInterpolator()
val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()


fun getAnimateForLikeBtn(iv: ImageView): AnimatorSet {
    val animXIV = ObjectAnimator.ofFloat(iv, "scaleX", 1f, 1.10f, 1.20f, 1.10f, 1f)
    animXIV.interpolator = accelerateDecelerateInterpolator
    animXIV.duration = 200
    val animYIV = ObjectAnimator.ofFloat(iv, "scaleY", 1f, 1.10f, 1.20f, 1.10f, 1f)
    animYIV.interpolator = accelerateDecelerateInterpolator
    animYIV.duration = 200

    return AnimatorSet().apply {
        play(animXIV).with(animYIV)
    }
}