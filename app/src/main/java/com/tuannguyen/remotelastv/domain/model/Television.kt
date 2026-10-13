package com.tuannguyen.remotelastv.domain.model

data class Television(
    val ipAddress: String,
    val name: String,
    val brand: TelevisionBrand = TelevisionBrand.UNKNOWN,
)