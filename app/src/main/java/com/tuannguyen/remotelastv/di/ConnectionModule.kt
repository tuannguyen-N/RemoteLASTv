package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.lg.LgTvConnection
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectionModule {

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.LG)
    abstract fun lgConnection(impl: LgTvConnection): TvConnectionRepository

//    @Binds @IntoMap @BrandKey(TelevisionBrand.SAMSUNG)
//    abstract fun samsung(impl: SamsungTvConnection): TvConnectionRepository
//
//    @Binds @IntoMap @BrandKey(TelevisionBrand.ANDROID_TV)
//    abstract fun androidTv(impl: AndroidTvConnection): TvConnectionRepository
}