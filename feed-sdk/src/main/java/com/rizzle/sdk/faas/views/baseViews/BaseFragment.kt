package com.rizzle.sdk.faas.views.baseViews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.rizzle.sdk.faas.feed.LoadingFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

abstract class BaseFragment<VB : ViewBinding> : Fragment(){

    val TAG = javaClass.simpleName
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    private val loadingDialog by lazy { LoadingFragment() }
    val subscriptions = CompositeDisposable()

    fun showLoading(show: Boolean, tag: String) {
        if (show) loadingDialog.show(childFragmentManager, tag)
        else {
            if(loadingDialog.isAdded) {
                loadingDialog.dismissNow()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).d("fragment lifecycle: creating fragment with tag: ${this.tag}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.tag(TAG).d("fragment lifecycle: creating view")
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("fragment lifecycle: created")
        super.onViewCreated(view, savedInstanceState)
        setup()
    }


    abstract fun setup()

    override fun onDestroyView() {
        Timber.tag(TAG).d("fragment lifecycle: view destroyed")
        super.onDestroyView()
        subscriptions.clear()
        _binding = null
    }

    override fun onDestroy() {
        Timber.tag(TAG).d("fragment lifecycle: fragment destroyed")
        subscriptions.dispose()
        super.onDestroy()
    }


    protected enum class ViewState {
        LOADING, LOADED, ERROR, EMPTY_LIST
    }

    open fun onVisibilityChanged(visibility: Int){}
}