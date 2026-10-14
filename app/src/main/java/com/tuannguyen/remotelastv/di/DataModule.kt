package com.tuannguyen.remotelastv.di

import android.content.Context
import androidx.room3.Room
import com.tuannguyen.remotelastv.data.dao.SavedTvDao
import com.tuannguyen.remotelastv.data.database.AppDatabase
import com.tuannguyen.remotelastv.data.repository.SavedTvRepositoryImpl
import com.tuannguyen.remotelastv.data.repository.SettingsRepositoryImpl
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSavedTvRepository(impl: SavedTvRepositoryImpl): SavedTvRepository

    @Binds
    @Singleton
    abstract fun bindAppLifecycleTracker(impl: com.tuannguyen.remotelastv.data.lifecycle.AppLifecycleTrackerImpl): com.tuannguyen.remotelastv.domain.connection.AppLifecycleTracker

}