package com.rizzle.sdk.faas.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rizzle.sdk.faas.db.dao.PostDao
import com.rizzle.sdk.faas.models.HashtagConverter
import com.rizzle.sdk.faas.models.HeatMapConverter
import com.rizzle.sdk.faas.models.Post

@Database(
    entities = [Post::class], version = 1,
    exportSchema = false
)
@TypeConverters(HeatMapConverter::class, HashtagConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        private lateinit var dbInstance: AppDatabase

        fun getDBInstance(context: Context): AppDatabase {
            synchronized(AppDatabase::class.java) {
                if (!::dbInstance.isInitialized) {
                    dbInstance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "postData"
                        )
                        .build()
                }
            }
            return dbInstance
        }
    }
}