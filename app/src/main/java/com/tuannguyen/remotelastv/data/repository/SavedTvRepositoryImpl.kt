package com.tuannguyen.remotelastv.data.repository

import com.tuannguyen.remotelastv.data.dao.SavedTvDao
import com.tuannguyen.remotelastv.data.mapper.toDomain
import com.tuannguyen.remotelastv.data.mapper.toEntity
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.SavedTvRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Singleton
class SavedTvRepositoryImpl @Inject constructor(
    private val dao: SavedTvDao,
) : SavedTvRepository {

    override val savedTvs: Flow<List<Television>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getTelevisions(): List<Television> =
        dao.getAll().map { it.toDomain() }

    override suspend fun get(id: String) = dao.get(id)?.toDomain()

    override suspend fun getCredentials(id: String): TvCredentials =
        dao.get(id)?.credentialsJson
            ?.let { Json.decodeFromString<TvCredentials>(it) }
            ?: TvCredentials.None

    override suspend fun getCustomName(id: String): String? {
        return dao.get(id)?.customName
    }

    override suspend fun save(tv: Television, credentials: TvCredentials) {
        val old = dao.get(tv.id)
        dao.upsert(
            tv.toEntity(credentials)
                .copy(customName = old?.customName ?: tv.name)
        )
    }

    override suspend fun rename(id: String, name: String) = dao.rename(id, name)

    override suspend fun delete(id: String) = dao.deleteAndFixDefault(id)

    override suspend fun getLastConnected(): Television? = dao.getLastConnected()?.toDomain()
    override suspend fun observe(id: String): Flow<Television?> = dao.observe(id).map {
        it?.toDomain()
    }
}