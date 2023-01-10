package com.rizzle.sdk.faas.models.responseModels

import com.rizzle.sdk.network.models.BooleanResponse
import com.rizzle.sdk.network.models.ReportOptionsEnum
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.utils.InternalUtils
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

open class BaseRepo {


    fun reportPost(postId: String, reportType: ReportOptionsEnum): Single<BooleanResponse> {
        return InternalUtils.networkApis.reportPost(postId, reportType)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun reportTrack(postId: String, reportType: ReportOptionsEnum): Single<BooleanResponse> {
        return InternalUtils.networkApis.reportTrack(postId, reportType)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun reportHashtag(hashtagName: String, reportType: ReportOptionsEnum): Single<BooleanResponse> {
        return InternalUtils.networkApis.reportHashtag(hashtagName, reportType)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun getPostsFromIds(postsIds: List<String>): Single<List<Post>> {
        return InternalUtils.networkApis.getPostsByIds(postsIds)
            .subscribeOn(Schedulers.io())
            .map {
                val commonPostListResponse = InternalUtils.jsonSerializer.convertJsonToPojo(it, CommonPostListResponse::class.java)
                commonPostListResponse?.posts?.nodes ?: emptyList()
            }
    }

    open fun clear() {
    }
}