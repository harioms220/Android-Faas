package com.rizzle.sdk.faas.repos

import com.rizzle.sdk.faas.db.AppDatabase.Companion.getDBInstance
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.faas.utils.InternalUtils
import com.rizzle.sdk.network.models.BooleanResponse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FeedRepo {

    private val dbInstance = getDBInstance(InternalUtils.application)

    fun getYourFeed(after: String?): Single<String> {
        return InternalUtils.networkApis.getFeed(after)
            .subscribeOn(Schedulers.io())
    }

    fun likePost(postId: String, timeOfActionInMillis: Int): Single<BooleanResponse> {
        return InternalUtils.networkApis.likePost(postId, timeOfActionInMillis)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun unlikePost(postId: String, timeOfActionInMillis: Int): Single<BooleanResponse> {
        return InternalUtils.networkApis.unlikePost(postId, timeOfActionInMillis)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun viewPost(postId: String, duration: Int): Single<BooleanResponse> {
        return InternalUtils.networkApis.viewPost(postId, duration)
            .subscribeOn(Schedulers.io())
            .map {
                InternalUtils.jsonSerializer.convertJsonToPojo(it, BooleanResponse::class.java)!!
            }
    }

    fun getPostsFromDB(start: Int, count: Int): Single<List<Post>> {
        return dbInstance.postDao().getPosts(start, count)
            .subscribeOn(Schedulers.io())
    }

    fun loadAllUnwatchedCachedPost(): Single<List<Post>> {
        return dbInstance.postDao().getAllUnWatchedCachedPost()
            .subscribeOn(Schedulers.io())
    }

    fun setPostCached(id: String): Completable {
        return dbInstance.postDao().setCached(id = id)
            .subscribeOn(Schedulers.io())
    }

    fun setPostWatched(id: String, viewCount: Long): Completable {
        return dbInstance.postDao().setWatched(id = id, viewCount = viewCount)
            .subscribeOn(Schedulers.io())
    }

    fun saveAllPostsInDB(post: List<Post>): Completable {
        return dbInstance.postDao().insert(post)
            .subscribeOn(Schedulers.io())
    }

    fun updatePostLikeStatus(isLiked: Boolean, postId: String, likeCount: Long): Completable {
        return dbInstance.postDao().setLiked(isLiked, likeCount, postId)
            .subscribeOn(Schedulers.io())
    }

    fun getHashTagPosts(hashTagName: String, endCursor: String?): Single<String> {
        return InternalUtils.networkApis.getHashTagPosts(hashTagName, endCursor)
            .subscribeOn(Schedulers.io())
    }

    fun getTrackPosts(trackId: String, endCursor: String?): Single<String> {
        return InternalUtils.networkApis.getTrackPosts(trackId, endCursor)
            .subscribeOn(Schedulers.io())
    }

    fun getPostsFromIds(postsIds: List<String>): Single<String> {
        return InternalUtils.networkApis.getPostsByIds(postsIds)
            .subscribeOn(Schedulers.io())
    }
}