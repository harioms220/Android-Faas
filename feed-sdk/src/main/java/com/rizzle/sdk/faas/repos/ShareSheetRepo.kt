package com.rizzle.sdk.faas.repos

import com.rizzle.sdk.faas.helpers.ShareLinkGenerator
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.network.models.BooleanResponse
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareType
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class ShareSheetRepo {
    fun getShareableLink(shareableLinkInput: ShareableLinkInput): Single<String>{
        // currently using the client side share link generation logic
        return ShareLinkGenerator.generateShareLink(shareableLinkInput)
//        return InternalUtils.networkApis.getShareableLink(shareableLinkInput).map {
//                InternalUtils.jsonSerializer.convertJsonToPojo(it, String::class.java)!!
//            }.subscribeOn(Schedulers.io())
    }

    fun logShareEvent(objectId: String, objectType: ShareObject, platform: ShareType): Single<BooleanResponse> {
        return InternalUtils.networkApis.logShareEvent(objectId, objectType, platform).subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }
}