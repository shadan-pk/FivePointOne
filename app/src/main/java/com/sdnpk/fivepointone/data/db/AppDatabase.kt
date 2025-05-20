package com.sdnpk.fivepointone.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sdnpk.fivepointone.data.db.dao.SpeakerConfigDao
import com.sdnpk.fivepointone.data.db.entity.SpeakerConfigEntity

@Database(entities = [SpeakerConfigEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speakerConfigDao(): SpeakerConfigDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "five_point_one_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
