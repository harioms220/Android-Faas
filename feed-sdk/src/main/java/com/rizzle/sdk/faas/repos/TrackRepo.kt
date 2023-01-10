package com.rizzle.sdk.faas.repos

import com.rizzle.sdk.faas.models.Track
import com.rizzle.sdk.faas.models.responseModels.BaseRepo
import com.rizzle.sdk.faas.models.responseModels.CommonPostListResponse
import com.rizzle.sdk.faas.models.responseModels.TrackDetails
import com.rizzle.sdk.faas.utils.InternalUtils
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class TrackRepo : BaseRepo() {

    fun getTrackPosts(trackId: String, endCursor: String?): Single<CommonPostListResponse> {
        return InternalUtils.networkApis.getTrackPosts(trackId, endCursor)
            .map { InternalUtils.jsonSerializer.convertJsonToPojo(it, CommonPostListResponse::class.java)!! }
            .subscribeOn(Schedulers.io())
    }

    fun getTrackInfo(trackId: String): Single<Track> {
        return InternalUtils.networkApis.getTrackInfo(trackId)
            .map { InternalUtils.jsonSerializer.convertJsonToPojo(it, TrackDetails::class.java)?.track!! }
            .subscribeOn(Schedulers.io())
    }

}