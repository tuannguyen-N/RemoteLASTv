package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.lg.LgTextInput
import com.tuannguyen.remotelastv.data.tv.lg.LgTvController
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
    @BrandKey(TelevisionBrand.LG)
    abstract fun lgTextInput(impl: LgTextInput, ): TvTextInputRepository
}