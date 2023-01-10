package com.rizzle.sdk.network.serialzers


/**
 * Interface for parsing JSON into Kotlin classes and vice versa.
 */
interface JsonPojoConverter {

    /**
     * Use this function to convert json string data to kotlin pojo class object
     *
     * @param jsonString String containing json data
     * @param type Kotlin pojo class in which jsonString to convert
     */
    fun<T> convertJsonToPojo(jsonString: String, type: Class<T>) : T?

    /**
     * Use this function to convert kotlin pojo class object to json string
     *
     * @param pojoObject The object to be converted to jsonString
     * @param type Kotlin pojo class corresponding to that object
     */
    fun<T> convertPojoToString(pojoObject: T, type: Class<T>): String?

    /**
     * Use this function to convert json string data to list of kotlin pojo class objects
     *
     * @param jsonString String containing json data
     * @param type Kotlin pojo class of single item of list
     */
    fun<T> convertJsonToList(jsonString: String, type: Class<T>): List<T>?


    /**
     * Use this function to convert list of kotlin pojo class objects to json data
     *
     * @param list List of objects to convert to json
     * @param type Kotlin pojo class of single item of list
     */
    fun <T> convertListToJson(list: List<T>, type: Class<T>): String?
}