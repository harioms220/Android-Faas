package com.rizzle.sdk.faas.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rizzle.sdk.faas.models.Post
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: List<Post>): Completable

    @Query("SELECT * FROM posts ORDER By id LIMIT :start,:count")
    fun getPosts(start: Int = 0, count: Int = 20): Single<List<Post>>

    @Query("SELECT * FROM posts WHERE isCached = :isCached AND isWatched = :isWatched LIMIT :limit")
    fun getAllUnWatchedCachedPost(isWatched: Boolean = false, isCached: Boolean = true, limit: Int = 3): Single<List<Post>>

    @Query("UPDATE posts SET isLiked = :isLiked, likeCount = :likeCount WHERE id = :postId")
    fun setLiked(isLiked: Boolean, likeCount: Long, postId: String): Completable

    @Query("UPDATE posts SET isCached = :isCached WHERE id = :id")
    fun setCached(isCached: Boolean = true, id: String): Completable

    @Query("UPDATE posts SET isWatched = :isWatched, viewCount = :viewCount WHERE id = :id")
    fun setWatched(isWatched: Boolean = true, viewCount: Long, id: String): Completable
}