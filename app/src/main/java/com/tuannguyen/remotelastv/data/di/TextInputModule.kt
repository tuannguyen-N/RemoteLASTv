package com.tuannguyen.remotelastv.data.di

import com.tuannguyen.remotelastv.data.tv.android_tv.AndroidTextInput
import com.tuannguyen.remotelastv.data.tv.lg.LgTextInput
import com.tuannguyen.remotelastv.data.tv.roku.RokuTextInput
import com.tuannguyen.remotelastv.data.tv.samsung.SamsungTvTextInput
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvTextInputRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class TextInputModule {

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ANDROID_TV)
    abstract fun androidTvTextInput(impl: AndroidTextInput): TvTextInputRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.LG)
    abstract fun lgTextInput(impl: LgTextInput): TvTextInputRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SONY)
    abstract fun sonyTextInput(impl: AndroidTextInput): TvTextInputRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.SAMSUNG)
    abstract fun samsungTextInput(impl: SamsungTvTextInput): TvTextInputRepository

    @Binds
    @IntoMap
    @BrandKey(TelevisionBrand.ROKU)
    abstract fun rokuTextInput(impl: RokuTextInput): TvTextInputRepository
}