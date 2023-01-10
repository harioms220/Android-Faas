package com.rizzle.sdk.faas.models

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.*
import com.rizzle.sdk.faas.helpers.Constants
import com.rizzle.sdk.faas.helpers.hashify
import com.rizzle.sdk.faas.utils.InternalUtils
import java.util.*

data class Feed(
    var posts: FeedInfo? = null
)

data class FeedInfo(
    var nodes: List<Post>? = null,
    var pageInfo: PageInfo? = null,
)

data class PageInfo(
    var endCursor: String? = null
)

data class PostCTAResponse(
    @ColumnInfo(name = "ctaThumbnailUrl")
    var thumbnailUrl: String? = null,
    @ColumnInfo(name = "ctaTitle")
    var title: String? = null,
    var buttonText: String? = null,
    var deepLink: String? = null
)

@Entity(tableName = "posts")
data class Post(
    @NonNull
    @PrimaryKey
    var id: String,

    @Embedded
    var context: PostContext? = null,

    @Embedded
    var cta: PostCTAResponse? = null,

    @Embedded
    var track: Track? = null,

    @Embedded
    var video: Video? = null,

    @TypeConverters(HashtagConverter::class)
    var hashtags: List<HashTag>? = null,

    @TypeConverters(HeatMapConverter::class)
    var heatmap: List<Heatmap>? = null,

    var description: String? = null,
    var isFeatured: Boolean? = null,
    var isTrending: Boolean? = null,
    var likeCount: Long? = null,
    var viewCount: Long? = null,
    var title: String? = null,
){
    var isCached: Boolean = false
    var isWatched: Boolean = false
    fun getUri(): Uri = Uri.parse(video?.url)

    // Show collapsed cta card if CTA(Deeplink) is available
    val showCollapsedCtaCard: Boolean
        get() = cta?.title.isNullOrEmpty().not() && areCtaFieldsPresent

    /**
     * return first hashtag with sequence number
     */
    fun getHashTags(): String {
        val tagBuilder = StringBuilder()
        hashtags?.forEach { tag ->
            tagBuilder.append(tag.name?.hashify()).append(Constants.HASHTAG_SPACE)
        }
        return tagBuilder.toString()
    }

    val areCtaFieldsPresent: Boolean
        get() {
            return cta?.deepLink.isNullOrEmpty().not() && cta?.thumbnailUrl.isNullOrEmpty().not()
                    && cta?.buttonText.isNullOrEmpty().not()
        }
}

data class PostContext(
    var isLiked: Boolean? = false
)

data class Video(
    var width: Int? = null,
    var height: Int? = null,
    var thumbnailUrl: String? = null,
    var webpUrl: String? = null,
    var url: String? = null,
    var duration: Int? = null,
)


/*
 * One can support custom data types by providing type converters, which are methods that
 * tell Room how to convert custom types to and from known types that Room can persist.
 * One can identify type converters by using the @TypeConverter annotation.
 */
object HeatMapConverter {

    @TypeConverter
    fun stringToList(data: String?): List<Heatmap?>? {
        if (data == null) return Collections.emptyList()
        return InternalUtils.jsonSerializer.convertJsonToList(data, Heatmap::class.java)
    }

    @TypeConverter
    fun listToString(someObjects: List<Heatmap>?): String? {
        return someObjects?.let { InternalUtils.jsonSerializer.convertListToJson(someObjects, Heatmap::class.java) }
    }
}

object HashtagConverter {

    @TypeConverter
    fun stringToList(data: String?): List<HashTag?>? {
        if (data == null) return Collections.emptyList()
        return InternalUtils.jsonSerializer.convertJsonToList(data, HashTag::class.java)
    }

    @TypeConverter
    fun listToString(someObjects: List<HashTag>?): String? {
        return someObjects?.let { InternalUtils.jsonSerializer.convertListToJson(someObjects, HashTag::class.java) }
    }
}
