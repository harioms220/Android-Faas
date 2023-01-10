package com.rizzle.sdk.faas.helpers

import java.io.File

class FileUtils {
    companion object {
        fun getFileSizeInBytes(filePath: String): Long {
            return File(filePath).length()
        }
    }
}