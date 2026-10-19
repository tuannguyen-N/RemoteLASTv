package com.tuannguyen.remotelastv.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface TvCredentials {
    @Serializable
    data object None : TvCredentials

    @Serializable
    data class Lg(val clientKey: String) : TvCredentials

    @Serializable
    data class Samsung(val token: String) : TvCredentials

    @Serializable
    data class AndroidTv(val certAlias: String) : TvCredentials
}