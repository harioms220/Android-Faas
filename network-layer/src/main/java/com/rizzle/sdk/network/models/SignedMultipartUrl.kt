package com.rizzle.sdk.network.models

data class SignedMultipartUrl(
    var url: String,
    val fields: List<Field>,
    var fileType: String = ""
)

data class SignedMultipartUrlResponse(
    var list: MutableList<SignedMultipartUrl> = mutableListOf()
)

data class Field(val key: String, val value: String)