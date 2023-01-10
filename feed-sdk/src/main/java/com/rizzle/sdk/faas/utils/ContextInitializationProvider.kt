package com.rizzle.sdk.faas.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.rizzle.sdk.faas.helpers.FeedSdkInitializer

/**
 * Empty content provider class. Only useful to get application context on startup
 * */
internal class ContextInitializationProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let { FeedSdkInitializer.install(it) }
        return true
    }

    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        return null
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

}