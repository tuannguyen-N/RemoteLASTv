package com.tuannguyen.remotelastv.data.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.tuannguyen.remotelastv.data.dao.SavedTvDao
import com.tuannguyen.remotelastv.data.entities.SavedTvEntity

@Database(
    entities = [SavedTvEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedTvDao(): SavedTvDao
}