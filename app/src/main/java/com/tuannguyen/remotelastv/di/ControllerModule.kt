package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.android_tv.AndroidTextInput
import com.tuannguyen.remotelastv.data.tv.android_tv.AndroidTvTController
import com.tuannguyen.remotelastv.data.tv.lg.LgTextInput
import com.tuannguyen.remotelastv.data.tv.lg.LgTvController
import com.tuannguyen.remotelastv.data.tv.roku.RokuTvController
import com.tuannguyen.remotelastv.data.tv.samsung.SamsungTvController
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class ControllerModule {

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.LG)
    abstract fun lgController(impl: LgTvController): TvControllerRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ANDROID_TV)
    abstract fun androidTvController(impl: AndroidTvTController): TvControllerRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SAMSUNG)
    abstract fun samsungController(impl: SamsungTvController): TvControllerRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SONY)
    abstract fun sonyController(impl: AndroidTvTController): TvControllerRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ROKU)
    abstract fun rokuController(impl: RokuTvController): TvControllerRepository
}