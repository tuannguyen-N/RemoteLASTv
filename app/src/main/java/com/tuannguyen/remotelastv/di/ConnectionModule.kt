package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.android_tv.AndroidTvConnection
import com.tuannguyen.remotelastv.data.tv.lg.LgTvConnection
import com.tuannguyen.remotelastv.data.tv.roku.RokuConnection
import com.tuannguyen.remotelastv.data.tv.samsung.SamsungTvConnection
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

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SAMSUNG)
    abstract fun samsungConnection(impl: SamsungTvConnection): TvConnectionRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SONY)
    abstract fun sonyConnection(impl: AndroidTvConnection): TvConnectionRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ANDROID_TV)
    abstract fun androidTvConnection(impl: AndroidTvConnection): TvConnectionRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ROKU)
    abstract fun rokuConnection(impl: RokuConnection): TvConnectionRepository
}