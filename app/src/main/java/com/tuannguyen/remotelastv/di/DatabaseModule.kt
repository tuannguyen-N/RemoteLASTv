package com.tuannguyen.remotelastv.di

import android.content.Context
import androidx.room3.Room
import com.tuannguyen.remotelastv.data.dao.SavedTvDao
import com.tuannguyen.remotelastv.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "remotelastv.db")
            .build()

    @Provides
    fun provideSavedTvDao(db: AppDatabase): SavedTvDao = db.savedTvDao()
}