package com.rizzle.sdk.faas.demo

import android.app.Application
import timber.log.Timber

class FaaSApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        // initialise Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}