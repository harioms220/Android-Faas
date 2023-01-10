package com.rizzle.sdk.faas.viewModels

import androidx.lifecycle.MutableLiveData
import com.rizzle.sdk.faas.models.HashTag
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.repos.HashTagRepo
import com.rizzle.sdk.faas.views.baseViews.QueryState
import io.reactivex.rxjava3.core.Single

class HashTagViewModel : BaseViewModel() {
    private val TAG = javaClass.simpleName
    private val repo = HashTagRepo()
    var infoQueryInProgress = MutableLiveData<Boolean>()
    var hashtagPostsQueryState = QueryState()

    fun getHashTagPosts(hashTagName: String): Single<MutableList<Post>> {
        return hashtagPostsQueryState.preCheck {
            repo.getHashTagPosts(hashTagName, hashtagPostsQueryState.endCursor)
                .map {
                    hashtagPostsQueryState.endCursor = it.posts?.pageInfo?.endCursor
                    it.posts?.nodes ?: mutableListOf()
                }
        }
    }


    fun getHashTagInfo(hashTagName: String): Single<HashTag> {
        return repo.getHashTagInfo(hashTagName)
            .doOnSubscribe { infoQueryInProgress.postValue(true) }
            .doFinally { infoQueryInProgress.postValue(false) }
    }

    override fun onCleared() {
        super.onCleared()
        repo.clear()
        hashtagPostsQueryState.reset()
    }
}