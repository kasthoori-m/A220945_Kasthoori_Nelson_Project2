package com.example.a220945_kasthoori_nelson_project2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Version 3: UserProfileEntity primary key changed from Int to matricNumber (String).
 * fallbackToDestructiveMigration handles the schema change automatically.
 */
@Database(
    entities = [CheckInRecord::class, UserProfileEntity::class],
    version = 4,
    exportSchema = false
)
abstract class EduQuestDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: EduQuestDatabase? = null

        fun getDatabase(context: Context): EduQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduQuestDatabase::class.java,
                    "eduquest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}