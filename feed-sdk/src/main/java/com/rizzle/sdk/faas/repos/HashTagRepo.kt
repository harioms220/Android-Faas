package com.rizzle.sdk.faas.repos

import com.rizzle.sdk.faas.models.HashTag
import com.rizzle.sdk.faas.models.responseModels.BaseRepo
import com.rizzle.sdk.faas.models.responseModels.CommonPostListResponse
import com.rizzle.sdk.faas.models.responseModels.HashTagDetails
import com.rizzle.sdk.faas.utils.InternalUtils
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class HashTagRepo : BaseRepo() {

    fun getHashTagPosts(hashTagName: String, endCursor: String?): Single<CommonPostListResponse> {
        return InternalUtils.networkApis.getHashTagPosts(hashTagName, endCursor)
            .map { responseString ->
                InternalUtils.jsonSerializer.convertJsonToPojo(responseString, CommonPostListResponse::class.java)!!
            }
            .subscribeOn(Schedulers.io())
    }

    fun getHashTagInfo(hashTagName: String): Single<HashTag> {
        return InternalUtils.networkApis.getHashTagInfo(hashTagName)
            .map { InternalUtils.jsonSerializer.convertJsonToPojo(it, HashTagDetails::class.java)?.hashtag!! }.subscribeOn(Schedulers.io())
    }

}