package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.repository.SettingsRepositoryImpl
import com.tuannguyen.remotelastv.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository
}