package com.tuannguyen.remotelastv.data.di

import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import dagger.MapKey

@MapKey
annotation class BrandKey(val value: TelevisionBrand)