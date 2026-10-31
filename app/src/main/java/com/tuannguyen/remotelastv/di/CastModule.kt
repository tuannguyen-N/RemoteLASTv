package com.tuannguyen.remotelastv.di

import com.tuannguyen.remotelastv.data.tv.cast.AndroidMediaInfoReader
import com.tuannguyen.remotelastv.data.tv.cast.AndroidVideoThumbnailReader
import com.tuannguyen.remotelastv.data.tv.cast.Media3MediaPreparer
import com.tuannguyen.remotelastv.data.tv.cast.dlna.DlnaCastProvider
import com.tuannguyen.remotelastv.data.tv.cast.dlna.DlnaDiscovery
import com.tuannguyen.remotelastv.domain.repository.CastProvider
import com.tuannguyen.remotelastv.domain.repository.MediaInfoReader
import com.tuannguyen.remotelastv.domain.repository.MediaPreparer
import com.tuannguyen.remotelastv.domain.repository.TvDiscovery
import com.tuannguyen.remotelastv.domain.repository.VideoThumbnailReader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CastModule {
    @Binds
    @Singleton
    @IntoSet
    abstract fun bindCastProvider(impl: DlnaCastProvider): CastProvider

    @Binds
    @Singleton
    @IntoSet
    abstract fun bindTvDiscovery(impl: DlnaDiscovery): TvDiscovery

    @Binds
    @Singleton
    abstract fun bindMediaPreparer(impl: Media3MediaPreparer): MediaPreparer

    @Binds
    @Singleton
    abstract fun bindMediaInfoReader(impl: AndroidMediaInfoReader): MediaInfoReader

    @Binds
    @Singleton
    abstract fun bindVideoThumbnailReader(impl: AndroidVideoThumbnailReader): VideoThumbnailReader
}