package com.rizzle.sdk.network.models

enum class ReportOptionsEnum(val reportValue: String) {

    NUDITY_OR_PORNOGRAPHY("Nudity or Pornography"),
    VIOLENCE("Violence"),
    UNAUTHORISED_SPEECH("Unauthorized speech"),
    SPAM("Spam"),
    SEE_FEWER_VIDEOS("See fewer Videos"),
    COPY_RIGHT_VIOLATION("Copy Right Violation"),
    LOW_QUALITY_VIDEO("Low quality video"),
    HARASSMENT("Harassment"),
    UNAUTHORISED_SALES("unauthorized_sales"),
    // Track report values

    INAPPROPRIATE_LANGUAGE( "Inappropriate Language"),
    LOW_QUALITY_AUDIO("Low quality audio"),
    HATE_SPEECH("Hate Speech"),
    OTHER ("Other");

    companion object {
        fun getReportPostList(): List<ReportOptionsEnum>{
            return listOf(
                NUDITY_OR_PORNOGRAPHY,
                VIOLENCE,
                UNAUTHORISED_SPEECH,
                SPAM,
                SEE_FEWER_VIDEOS,
                COPY_RIGHT_VIOLATION,
                LOW_QUALITY_VIDEO,
                HATE_SPEECH,
                HARASSMENT
            )
        }


        fun getReportTrackList(): List<ReportOptionsEnum>{
            return listOf(
                COPY_RIGHT_VIOLATION,
                INAPPROPRIATE_LANGUAGE,
                LOW_QUALITY_AUDIO,
                HATE_SPEECH,
                OTHER
            )
        }

        fun getReportHashtagList(): List<ReportOptionsEnum> {
            return listOf(
                NUDITY_OR_PORNOGRAPHY,
                HARASSMENT,
                INAPPROPRIATE_LANGUAGE,
                HATE_SPEECH,
                SPAM,
                VIOLENCE,

            )
        }
    }
}