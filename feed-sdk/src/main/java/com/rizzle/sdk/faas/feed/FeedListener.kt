package com.rizzle.sdk.faas.feed

import com.rizzle.sdk.faas.models.HashTag
import com.rizzle.sdk.faas.models.Post
import com.rizzle.sdk.network.models.requestmodels.ShareObject


/**
 * Interface definition the events that can occur on the [FeedAdapter]
 */
interface FeedListener {


    /** Invoked when new [Post] appears on the [FeedAdapter] */
    fun onPostAppeared(post: Post)

    /** Invoked when currently playing post on the [FeedAdapter] is paused */
    fun onPaused(post: Post)

    /** Invoked when currently playing post on the [FeedAdapter] is resumed*/
    fun onResumed(post: Post)

    /** Invoked when share button on currently playing post on the [FeedAdapter] is clicked*/
    fun onShareClicked(post: Post, shareObject: ShareObject, timeOfActionInMillis: Long)

    /** Invoked when profile button on currently playing post on the [FeedAdapter] is clicked*/
    fun onProfileClicked(post: Post)

    /** Invoked when hashtag on currently playing post on the [FeedAdapter] is clicked*/
    fun onHashtagClicked(hashTag: HashTag, post: Post)

    /** Invoked when track name on currently playing post on the [FeedAdapter] is clicked*/
    fun onTrackClicked(trackId: String, post: Post)

    /** Invoked when track card watch now on currently playing post on the [FeedAdapter] is clicked*/
    fun onWatchNowClicked(post: Post)

    /** Invoked when back button is pressed on [FeedAdapter]*/
    fun onFeedBackPressed()
}