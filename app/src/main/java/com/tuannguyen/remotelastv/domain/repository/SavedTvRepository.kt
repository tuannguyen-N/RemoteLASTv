package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import kotlinx.coroutines.flow.Flow

interface SavedTvRepository {
    val savedTvs: Flow<List<Television>>
    suspend fun getTelevisions(): List<Television>
    suspend fun get(id: String): Television?
    suspend fun getCredentials(id: String): TvCredentials
    suspend fun getCustomName(id: String): String?
    suspend fun save(tv: Television, credentials: TvCredentials)
    suspend fun rename(id: String, name: String)
    suspend fun delete(id: String)
    suspend fun getLastConnected(): Television?
    suspend fun observe(id: String): Flow<Television?>
}