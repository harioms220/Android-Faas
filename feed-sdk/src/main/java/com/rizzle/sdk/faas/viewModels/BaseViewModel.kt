package com.rizzle.sdk.faas.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rizzle.sdk.faas.helpers.plusAssign
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.models.responseModels.BaseRepo
import com.rizzle.sdk.network.models.ReportOptionsEnum
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

open class BaseViewModel : ViewModel() {
    val subscriptions = CompositeDisposable()
    private val TAG = javaClass.simpleName
    private val baseRepo = BaseRepo()

    val reported = MutableLiveData<Boolean>()

    fun reportPost(postId: String, reportType: ReportOptionsEnum) {
        subscriptions += baseRepo.reportPost(postId, reportType)
            .doFinally { reported.postValue(true) }
            .subscribe({
                Timber.tag(TAG).d("Reported post with id $postId successfully")
            }, {
                Timber.tag(TAG).d("Error while Reporting post with id: $postId having message ${it.message}")
            })
    }


    fun reportTrack(trackId: String, reportType: ReportOptionsEnum) {
        subscriptions += baseRepo.reportTrack(trackId, reportType)
            .doFinally { reported.postValue(true) }
            .subscribe({
                Timber.tag(TAG).d("Reported track with id $trackId successfully")
            }, {
                Timber.tag(TAG).d("Error while Reporting post with id: $trackId having message ${it.message}")
            })
    }

    override fun onCleared() {
        Timber.tag(TAG).d("viewModel lifecycle: cleared")
        super.onCleared()
        subscriptions.clear()
    }

    fun reportHashtag(hashtagId: String, reportType: ReportOptionsEnum) {
        subscriptions += baseRepo.reportHashtag(hashtagId, reportType)
            .doFinally { reported.postValue(true) }
            .subscribe({
                Timber.tag(TAG).d("Reported hashtag successfully")
            }, {
                Timber.tag(TAG).d("Error while Reported ${it.localizedMessage} ")
            })
    }

    fun getPostsByIds(postIds: List<String>): Single<List<Post>> {
        return baseRepo.getPostsFromIds(postIds)
    }

}