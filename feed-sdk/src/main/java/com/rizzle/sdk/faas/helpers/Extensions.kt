package com.rizzle.sdk.faas.helpers

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.rizzle.sdk.faas.R
import com.rizzle.sdk.faas.autolink.AutoLinkTextView
import com.rizzle.sdk.faas.autolink.MODE_HASHTAG
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.faas.utils.InternalUtils.color
import com.rizzle.sdk.faas.utils.Resolution
import com.rizzle.sdk.faas.views.baseViews.BaseAdapter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

enum class TouchType {
    TEXT,
    BUTTON,
    ICON
}

fun View.click(debounceTime: Long = 300L, action: (view: View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action(v)
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}


fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.dramaticHide() {
    this.animate()?.scaleX(2f)?.scaleY(2f)?.alpha(0f)?.duration = 200
}

fun View.dramaticShow() {
    this.show()
    this.animate()?.scaleX(.8f)?.scaleY(.8f)?.alpha(1f)?.duration = 200
}


/**
 * Helper function to get scaled image from server,adding width and height to url
 */
fun String?.getScaledImage(resolution: Resolution): String {
    if (this == null) return ""
    if (this.trim().isEmpty()) return ""
    return this + "?d=" + resolution.width + "x" + resolution.height

}


fun <E> MutableList<E>.clearAndAddAll(elements: Collection<E>) {
    this.clear()
    this.addAll(elements)
}

fun RequestOptions.setSize(resolution: Resolution): RequestOptions {
    return this.override(resolution.width, resolution.height)
}


/**
 * Extension function for avoiding missing out on giving size parameter for image url and request options
 */
fun RequestManager.load(url: String?, options: RequestOptions = RequestOptions(), size: Resolution): RequestBuilder<Drawable> {
    return load(url.getScaledImage(size))
        .apply(options.setSize(size))
}

fun View.hapticFeedback() {
    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}

fun LottieAnimationView.animationEnd(action: () -> Unit) {
    addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            action.invoke()
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    })
}

@SuppressLint("ClickableViewAccessibility")
@Suppress("UNUSED_VALUE")
//todo handle alpha values correctly. If I change alpha manually in code and use touch, alpha gets manipulated (undesired)
fun View.touch(viewType: TouchType = TouchType.BUTTON, debounceTime: Long = 300L, action: (view: View) -> Unit) {
    val viewTypee: TouchType = when (this) {
        is TextView -> {
            if (this.background == null)
                TouchType.TEXT
            else
                TouchType.BUTTON
        }
        else -> viewType
    }

    val scale: Float
    val alpha: Float
    val scaleDownDur: Long
    val scaleUpDur: Long

    when (viewTypee) {
        TouchType.TEXT -> {
            scale = .96f
            alpha = .7f
            scaleDownDur = 0L
            scaleUpDur = 10L
        }
        TouchType.ICON -> {
            scale = 0.90f
            alpha = 0.5f
            scaleDownDur = 5L
            scaleUpDur = 150L
        }
        else -> {
            scale = 0.98f
            alpha = 0.8f
            scaleDownDur = 5L
            scaleUpDur = 100L
        }
    }

    this.setOnTouchListener(object : View.OnTouchListener {
        private var lastClickTime: Long = 0

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            var rect = Rect()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(scale).scaleY(scale).alpha(alpha).setDuration(scaleDownDur).start()
                    rect = Rect(v.left, v.top, v.right, v.bottom)
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(scaleUpDur).start()

                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) {
                        Timber.tag("TouchListener").d("onTouch debounced touch")
                        return true
                    } else action(v)
                    lastClickTime = SystemClock.elapsedRealtime()
                }
                MotionEvent.ACTION_MOVE -> {
                    v.getHitRect(rect)
                    if (!rect.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        // User moved outside bounds
                        v.alpha = 1F
                        v.scaleY = 1F
                        v.scaleX = 1F
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1F
                    v.scaleY = 1F
                    v.scaleX = 1F
                }

            }
            return true
        }
    })
}

operator fun CompositeDisposable?.plusAssign(other: Disposable?): Unit = if (other != null) this?.add(other).let { return } else Unit

/**
 * Converts milliseconds to mm:ss format
 */
fun Long.msToMMSS(): String {
    val mins = TimeUnit.MILLISECONDS.toMinutes(this)
    val secs = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(mins)
    return String.format("%02d:%02d", mins, secs)
}

val Float.dpToPx
    get() = (this * (InternalUtils.application.resources.displayMetrics.densityDpi / 160f)).toInt()

@Suppress("unused")
val Float.pxToDp
    get() = (this * (160f / InternalUtils.application.resources.displayMetrics.densityDpi)).toInt()

val Float.pxToSp
    get() = this / InternalUtils.application.resources.displayMetrics.scaledDensity

val Float.spToPx
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, InternalUtils.application.resources.displayMetrics).toInt()

fun String.fileExists() = File(this).exists()

fun String.mkdirs() = File(this).mkdirs()

fun String.inputStream() = FileInputStream(this)

/**
 * read text from asset json file
 */
fun readJson(filePath: String) = File(filePath).bufferedReader().use { it.readText() }
fun String.deleteFile(): Boolean = File(this).deleteRecursively()

@kotlin.jvm.Throws(IOException::class)
fun readRawFileFromResources(@RawRes id: Int): String = InternalUtils.application.resources.openRawResource(id).bufferedReader().readText()
fun readRawFileFromResourcesAsync(@RawRes id: Int): Single<String> {
    return Single.fromCallable {
        InternalUtils.application.resources.openRawResource(id).bufferedReader().readText()
    }.subscribeOn(Schedulers.io())
}

fun readJsonFromFileAsync(filePath: String): Single<String> {
    return Single.fromCallable {
        readJson(filePath)
    }.subscribeOn(Schedulers.io())
}

/** Writes string to the file asynchronously */
internal fun File.writeStringAsync(stringData: String): Completable{
    return Completable.defer {
        try {
            val os = outputStream().use {
                it.write(stringData.toByteArray())
                it
            }
            os.close()
            Completable.complete()
        }catch (ex: Exception){
            Completable.error(ex)
        }
    }.subscribeOn(Schedulers.io())
}

fun <T> SharedPreferences.put(prefPair: Pair<String, T>, commit: Boolean = false) = set(prefPair.first, prefPair.second, commit)

private fun SharedPreferences.set(key: String, value: Any?, commit: Boolean = false) {
    when (value) {
        is String? -> edit(commit) { putString(key, value) }
        is Int -> edit(commit) { putInt(key, value.toInt()) }
        is Boolean -> edit(commit) { putBoolean(key, value) }
        is Float -> edit(commit) { putFloat(key, value.toFloat()) }
        is Long -> edit(commit) { putLong(key, value.toLong()) }
        is Set<*> -> edit(commit) { putStringSet(key, value as Set<String>) }
        else -> throw RuntimeException(Throwable("Unsupported Type: $value"))
    }
}

fun String?.isNullString(): Boolean {
    return this == null || this == "null"
}

/**
 * conditional visibility of view
 * @param condition condition which must be satisfied for this view to be visible
 * @param hide if true, view will be hidden in case condition fails, else view will be set to gone
 */
fun View.showIf(condition: Boolean?, hide: Boolean = false) {
    if (condition == true) show()
    else {
        if (hide) hide()
        else gone()
    }
}

fun RecyclerView.attachPagination(threshHoldCount: Int = 3, additionalCheck: Boolean = true, onPaginated: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                adapter?.let {
                    val position = when (val lm = recyclerView.layoutManager) {
                        is LinearLayoutManager -> lm.findLastVisibleItemPosition()
                        is GridLayoutManager -> lm.findLastVisibleItemPosition()
                        else -> throw NotImplementedError("Other layout managers are not supported for pagination, please implement")
                    }

                    if (position >= it.itemCount - threshHoldCount && adapter is BaseAdapter && (adapter as BaseAdapter).noMoreData.not() && additionalCheck) {
                        onPaginated()
                    }
                }
                    ?: throw  IllegalStateException("Adapter not found, please attach an adapter before attaching a paginator")
            }
        }
    })
}

/**
 * Format big numbers in cool format
 * https://stackoverflow.com/a/4753866
 */
private val suffixes = TreeMap<Long, String>().apply {
    this[1_000L] = "k"
    this[1_000_000L] = "M"
    this[1_000_000_000L] = "B"
    this[1_000_000_000_000L] = "T"
    this[1_000_000_000_000_000L] = "P"
    this[1_000_000_000_000_000_000L] = "E"
}

fun Long.formatCool(): String {
    //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
    if (this == java.lang.Long.MIN_VALUE) return (java.lang.Long.MIN_VALUE + 1).formatCool()
    if (this < 0) return "-" + (-this).formatCool()
    if (this < 10000) return this.toString() //deal with easy case

    val e = suffixes.floorEntry(this)
    val divideBy = e?.key
    val suffix = e?.value

    val truncated = this / (divideBy!! / 10) //the number part of the output times 10
    val hasDecimal = truncated < 10000 && truncated / 10.0 != (truncated / 10).toDouble()
    return if (hasDecimal) "${truncated / 10.0}$suffix" else "${truncated / 10}$suffix"
}

/**
 * sets hashtag
 * @param color color to use for hashtags
 */
fun AutoLinkTextView.setPropertyAndText(text: String?, color: Int) {
    text?.run {
        addAutoLinkMode(MODE_HASHTAG)
        hashTagModeColor = color
        pressedTextColor = color(R.color.white)
        this@setPropertyAndText.text = text
    }
}

fun String.hashify() = "#%s".format(this.replaceFirst("#", ""))