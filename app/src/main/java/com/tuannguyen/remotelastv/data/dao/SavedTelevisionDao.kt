package com.tuannguyen.remotelastv.data.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.tuannguyen.remotelastv.data.entities.SavedTvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTvDao {
    @Query("SELECT * FROM saved_tv ORDER BY lastConnectedAt DESC")
    fun observeAll(): Flow<List<SavedTvEntity>>

    @Query("SELECT * FROM saved_tv WHERE id = :id")
    suspend fun get(id: String): SavedTvEntity?

    @Query("SELECT * FROM saved_tv WHERE isDefault = 1 LIMIT 1")
    fun observeDefault(): Flow<SavedTvEntity?>

    @Upsert
    suspend fun upsert(entity: SavedTvEntity)

    @Query("UPDATE saved_tv SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefault()

    @Transaction
    suspend fun upsertAsDefault(entity: SavedTvEntity) {
        clearDefault()
        upsert(entity.copy(isDefault = true))
    }

    @Query("UPDATE saved_tv SET customName = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("UPDATE saved_tv SET isDefault = 1 WHERE id = (SELECT id FROM saved_tv ORDER BY lastConnectedAt DESC LIMIT 1)")
    suspend fun promoteLatestToDefault()

    @Transaction
    suspend fun deleteAndFixDefault(id: String) {
        val wasDefault = get(id)?.isDefault == true
        delete(id)
        if (wasDefault) promoteLatestToDefault()
    }

    @Query("DELETE FROM saved_tv WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM saved_tv ORDER BY lastConnectedAt DESC LIMIT 1")
    suspend fun getLastConnected(): SavedTvEntity?
}