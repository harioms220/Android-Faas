package com.rizzle.sdk.network.apis

import com.rizzle.sdk.network.models.ReportOptionsEnum
import org.json.JSONArray
import com.rizzle.sdk.network.models.requestmodels.ShareObject
import com.rizzle.sdk.network.models.requestmodels.ShareType
import com.rizzle.sdk.network.models.requestmodels.ShareableLinkInput

object Queries {

    private const val POST_DETAILS = "fragment PostDetails on Post{description heatmap{action startTime duration opacity iconUrl hexColorCode}id isFeatured cta{buttonText deepLink title thumbnailUrl}isTrending likeCount viewCount isWatched hashtags{id name description postCount}title track{id title albumTitle artist audio{duration trackUrl trackImageUrl}postCount viewCount}video{duration height thumbnailUrl url webpUrl width}context{isLiked}}"

    fun getTrackPosts( trackId: String, endCursor: String?): Pair<String, String> {
        return Pair(
            "trackPosts",
            "{\"query\":\"query{trackPosts(input:{trackId:\\\"$trackId\\\",paging:{after:\\\"$endCursor\\\"}}){posts{nodes{...PostDetails} pageInfo{endCursor}}}} $POST_DETAILS\",\"variables\":{}}"
        )
    }

    fun getTrackInfo(trackId: String): Pair<String, String> {
        return Pair(
            "track",
            "{\"query\":\"query{track(input:{trackId:\\\"$trackId\\\"}){track{id title albumTitle artist audio{duration trackUrl trackImageUrl}postCount viewCount}}}\",\"variables\":{}}"
        )
    }

    fun getHashTagPosts(hashTagName: String?, endCursor: String?): Pair<String, String> {
        return Pair(
            "hashtagPosts",
            "{\"query\":\"query{hashtagPosts(input:{hashtag:\\\"$hashTagName\\\",paging:{after:\\\"$endCursor\\\"}}){posts{nodes{...PostDetails} pageInfo{endCursor}}}} $POST_DETAILS \",\"variables\":{}}"
        )
    }

    fun getHashTagInfo(hashTagName: String?): Pair<String, String> {
        return Pair(
            "hashtag",
            "{\"query\":\"query{hashtag(input:{name:\\\"$hashTagName\\\"}){hashtag{id name description viewCount}}}\",\"variables\":{}}"
        )
    }


    fun reportPost(postId: String, reportType: ReportOptionsEnum): Pair<String, String> {
        return Pair(
            "reportPost",
            "{\"query\":\"mutation{reportPost(input:{postId:\\\"$postId\\\", reportType:$reportType}){success}}\",\"variables\":{}}"
        )
    }

    fun viewPost(postId: String, duration: Int): Pair<String, String> {
        return Pair(
            "viewPost",
            "{\"query\":\"mutation{viewPost(input:{postId:\\\"$postId\\\", duration:$duration}){success}}\",\"variables\":{}}"
        )
    }

    fun likePost(postId: String, timeOfActionInMillis: Int): Pair<String, String> {
        return Pair(
            "likePost",
            "{\"query\":\"mutation{likePost(input:{postId:\\\"$postId\\\",timeOfActionInMillis:$timeOfActionInMillis}){success}}\",\"variables\":{}}"
        )
    }

    fun unlikePost(postId: String, timeOfActionInMillis: Int): Pair<String, String> {
        return Pair(
            "unlikePost",
            "{\"query\":\"mutation{unlikePost(input:{postId:\\\"$postId\\\",timeOfActionInMillis:$timeOfActionInMillis}){success}}\",\"variables\":{}}"
        )
    }

    fun getFeed(after: String? = ""): Pair<String, String> {
        return Pair(
            "feed",
            "{\"query\":\"query{feed(input:{paging:{after:\\\"$after\\\"}}){posts{nodes{description heatmap{action startTime duration opacity iconUrl hexColorCode}id isFeatured cta{buttonText deepLink title thumbnailUrl}isTrending likeCount viewCount isWatched hashtags{id name description postCount}title track{id title albumTitle artist audio{duration trackUrl trackImageUrl}postCount viewCount}video{duration height thumbnailUrl url width}context{isLiked}}pageInfo{endCursor}}}}\",\"variables\":{}}"
        )
    }

    fun fetchRemoteUIConfiguration(): Pair<String, String>{
        return Pair(
            "appConfig",
            "{\"query\": \"query{ appConfig{ config{ textStyles{ name font size } fonts{ name fontUrl extension} icons{ name iconUrl type } colors{ name hexCode } version cardCornerRadius pillCornerRadius } } }\" }"
        )
    }

    fun getPostsById(postIds: JSONArray): Pair<String, String> {
        return Pair(
            "postsByIds",
            "{\"query\":\"query(\$input:PostsByIdsInput!){postsByIds(input:\$input){posts{nodes{...PostDetails}}}} $POST_DETAILS \",\"variables\":{\"input\":{\"postIds\":$postIds}}}"
        )
    }


    fun reportTrack(postId: String, reportType: ReportOptionsEnum): Pair<String, String> {
        return Pair(
            "reportTrack",
            "{\"query\":\"mutation{reportTrack(input:{trackId:\\\"$postId\\\", reportType:$reportType}){success}}\",\"variables\":{}}"
        )
    }

    fun getShareableLink(input: ShareableLinkInput): Pair<String, String> {
        return Pair(
            "getShareableLink",
            "{\"query\":\"mutation{getShareableLink(input:{objectType:${input.objectType},timeOfActionInMillis:${input.timeOfActionInMillis},objectId:\\\"${input.objectId}\\\"}){link}}\",\"variables\":{}}"
        )
    }

    fun logShareEvent(objectId: String, objectType: ShareObject, platform: ShareType): Pair<String, String> {
        return Pair(
            "logShareEvent",
            "{\"query\":\"mutation{logShareEvent(input:{objectType:$objectType,platform:$platform,objectId:\\\"$objectId\\\"}){success}}\",\"variables\":{}}"
        )
    }

    fun reportHashtag(hashTagName: String, reportType: ReportOptionsEnum): Pair<String, String> {
        return Pair(
            "reportHashtag",
            """{"query":"mutation{reportHashtag(input:{hashtag:\"$hashTagName\",reportType:$reportType}){success}}","variables":{}}"""
        )
    }

}