package com.rizzle.sdk.network.apis

import com.rizzle.sdk.network.NetworkUtils
import com.rizzle.sdk.network.graphql.BaseNetworkClient
import com.rizzle.sdk.network.models.ReportOptionsEnum
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareType
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.json.JSONArray
import java.io.File

class NetworkApisImpl(): NetworkApis {

    private var networkClient: BaseNetworkClient = NetworkUtils.defaultNetworkClient

    override fun downloadFile(url: String, file: File): Completable {
        return networkClient.downloadFile(url, file)
    }

    override fun getTrackPosts(trackId: String, endCursor: String?): Single<String> = networkClient.makeQuery(Queries.getTrackPosts(trackId, endCursor))

    override fun getTrackInfo(trackId: String): Single<String> = networkClient.makeQuery(Queries.getTrackInfo(trackId))

    override fun getHashTagPosts(hashTagName: String, endCursor: String?): Single<String> = networkClient.makeQuery(Queries.getHashTagPosts(hashTagName, endCursor))

    override fun getHashTagInfo(hashTagName: String): Single<String> = networkClient.makeQuery(Queries.getHashTagInfo(hashTagName))

    override fun reportPost(postId: String, reportType: ReportOptionsEnum): Single<String> =  networkClient.makeQuery(Queries.reportPost(postId, reportType))

    override fun getRemoteUIconfig(): Single<String> = networkClient.makeQuery(Queries.fetchRemoteUIConfiguration())

    override fun getFeed(after: String?): Single<String> = networkClient.makeQuery(Queries.getFeed(after))

    override fun likePost(postId: String, timeOfActionInMillis: Int): Single<String> = networkClient.makeQuery(Queries.likePost(postId, timeOfActionInMillis))

    override fun unlikePost(postId: String, timeOfActionInMillis: Int): Single<String> = networkClient.makeQuery(Queries.unlikePost(postId, timeOfActionInMillis))

    override fun viewPost(postId: String, duration: Int): Single<String> = networkClient.makeQuery(Queries.viewPost(postId, duration))

    override fun getPostsByIds(postsIds: List<String>): Single<String>  = networkClient.makeQuery(Queries.getPostsById(JSONArray(postsIds)))

    override fun reportTrack(trackId: String, reportType: ReportOptionsEnum): Single<String> = networkClient.makeQuery(Queries.reportTrack(trackId, reportType))
    override fun getShareableLink(input: ShareableLinkInput): Single<String> = networkClient.makeQuery(Queries.getShareableLink(input))

    override fun logShareEvent(objectId: String, objectType: ShareObject, platform: ShareType): Single<String> = networkClient.makeQuery(Queries.logShareEvent(objectId, objectType, platform))

    override fun reportHashtag(hashtag: String, reportType: ReportOptionsEnum): Single<String> = networkClient.makeQuery(Queries.reportHashtag(hashtag,reportType))
}