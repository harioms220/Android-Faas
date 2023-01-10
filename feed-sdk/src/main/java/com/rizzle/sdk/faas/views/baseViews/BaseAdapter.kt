package com.rizzle.sdk.faas.views.baseViews

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

/**
 * Base adapter class used to have common functionalities of adapters in one place to avoid code repetition.
 *
 * @param lifecycleOwner The owner activity or the fragment using this adapter. If provided, this adapter will observe lifecycle events of this [LifecycleOwner].
 */
abstract class BaseAdapter<T : RecyclerView.ViewHolder?>(private var lifecycleOwner: LifecycleOwner? = null) :
    RecyclerView.Adapter<T>(), DefaultLifecycleObserver {

    protected var rv: RecyclerView? = null

    companion object{
        //common functionality
        const val TYPE_LAST_ITEM = 999
    }

    open var noMoreData: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(itemCount - 1)
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        rv = null
    }
}