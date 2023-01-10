package com.rizzle.sdk.network.serialzers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber


/** Class handling serialization and deserialization of the Json data using Moshi library */
class MoshiPojoConverter: JsonPojoConverter {
    private val TAG = javaClass.simpleName
    private val moshi by lazy { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }

    private fun<T> getJsonAdapter(type: Class<T>) = moshi.adapter(type)
    private fun<T> getJsonAdapterForListType(type: Class<T>): JsonAdapter<List<T>>? {
        val customType = Types.newParameterizedType(List::class.java, type)
        return moshi.adapter(customType)
    }

    override fun <T> convertJsonToPojo(jsonString: String, type: Class<T>): T? = getJsonAdapter(type)?.fromJson(jsonString)

    override fun <T> convertPojoToString(pojoObject: T, type: Class<T>): String? = getJsonAdapter(type)?.toJson(pojoObject)


    override fun <T> convertJsonToList(jsonString: String, type: Class<T>): List<T>? {
        return try{
            getJsonAdapterForListType(type)?.fromJson(jsonString)
        }catch (ex: Exception) {
            Timber.tag(TAG).e(ex)
            emptyList()
        }
    }

    override fun <T> convertListToJson(list: List<T>, type: Class<T>): String? {
        return try{
            getJsonAdapterForListType(type)?.toJson(list)
        }catch (ex: Exception) {
            Timber.tag(TAG).e(ex)
            null
        }
    }
}