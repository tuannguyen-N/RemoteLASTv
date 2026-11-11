package com.tuannguyen.remotelastv.data.di

import com.tuannguyen.remotelastv.data.repository.PremiumRepositoryImpl
import com.tuannguyen.remotelastv.domain.repository.PremiumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PremiumModule {
    @Binds
    abstract fun bindPremiumRepository(impl: PremiumRepositoryImpl): PremiumRepository
}