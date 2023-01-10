package com.rizzle.sdk.network

import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor

object DeltaFile {
    fun getChuck(): Interceptor = ChuckerInterceptor.Builder(NetworkUtils.application).build()
}