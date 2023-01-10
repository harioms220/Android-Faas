package com.rizzle.sdk.faas.models.responseModels

class ApiError {
    val message: String = ""
    val displayMessage: String = ""
    val code: Int? = null
    val name: String = ""
    val path: List<String> = listOf()

    var query: String? = null

    override fun toString(): String {
        return "API Error exception \n" +
                "Message : $message \n" +
                "Query : $query \n" +
                "Code : $code \n" +
                "Name : $name"
    }
}