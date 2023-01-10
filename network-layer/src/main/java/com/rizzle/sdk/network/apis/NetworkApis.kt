package com.rizzle.sdk.network.apis

import com.rizzle.sdk.network.models.ReportOptionsEnum
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareType
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File

/** Interface declaring all the api related calls*/
interface NetworkApis {
    fun downloadFile(url: String, file: File): Completable
    fun getTrackPosts(trackId: String, endCursor: String?): Single<String>
    fun getTrackInfo(trackId: String): Single<String>
    fun getHashTagPosts(hashTagName: String, endCursor: String?): Single<String>
    fun getHashTagInfo(hashTagName: String): Single<String>
    fun getRemoteUIconfig(): Single<String>
    fun reportPost(postId: String, reportType: ReportOptionsEnum): Single<String>
    fun getFeed(after: String?): Single<String>
    fun likePost(postId: String, timeOfActionInMillis: Int): Single<String>
    fun unlikePost(postId: String, timeOfActionInMillis: Int): Single<String>
    fun viewPost(postId: String, duration: Int): Single<String>
    fun getShareableLink(input: ShareableLinkInput): Single<String>
    fun logShareEvent(objectId: String, objectType: ShareObject, platform: ShareType): Single<String>
    fun getPostsByIds(postsIds: List<String>): Single<String>
    fun reportTrack(trackId: String, reportType: ReportOptionsEnum): Single<String>
    fun reportHashtag(hashtag: String, reportType: ReportOptionsEnum): Single<String>
}