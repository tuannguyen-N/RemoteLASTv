package com.tuannguyen.remotelastv.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "saved_tv")
data class SavedTvEntity(
    @PrimaryKey val id: String,
    val brand: String,
    val customName: String,
    val ipAddress: String,
    val credentialsJson: String,
    val lastConnectedAt: Long,
    val isDefault: Boolean,
)