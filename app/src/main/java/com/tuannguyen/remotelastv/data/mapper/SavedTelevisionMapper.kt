package com.tuannguyen.remotelastv.data.mapper

import com.tuannguyen.remotelastv.data.entities.SavedTvEntity
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import kotlinx.serialization.json.Json

fun SavedTvEntity.toDomain() = Television(
    id = id,
    brand = TelevisionBrand.valueOf(brand),
    name = customName,
    ipAddress = ipAddress,
)

fun Television.toEntity(
    credentials: TvCredentials,
    isDefault: Boolean = true,
    now: Long = System.currentTimeMillis(),
) = SavedTvEntity(
    id = id,
    brand = brand.name,
    customName = name,
    ipAddress = ipAddress,
    credentialsJson = Json.encodeToString(credentials),
    lastConnectedAt = now,
    isDefault = isDefault,
)