package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.android_tv.AndroidTvDiscovery
import com.tuannguyen.remotelastv.data.tv.lg.LgTvDiscovery
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscoveryModule {

    @Binds @IntoSet
    abstract fun bindAndroidTv(impl: AndroidTvDiscovery): TvDiscoveryRepository

    @Binds @IntoSet
    abstract fun bindLgTv(impl: LgTvDiscovery): TvDiscoveryRepository
}