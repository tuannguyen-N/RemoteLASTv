package com.tuannguyen.remotelastv.data.tv.roku

import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration.Companion.milliseconds

class RokuConnection @Inject constructor(
    private val client: OkHttpClient,
    private val registry: RokuRegistry,
) : TvConnectionRepository {

    override fun connect(
        television: Television,
        credentials: TvCredentials,
    ): Flow<ConnectionStatus> = flow {
        emit(ConnectionStatus.Connecting)

        var reachable = false
        repeat(3) { attempt ->
            if (!reachable) {
                reachable = ping(television.ipAddress)
                if (!reachable && attempt < 2) delay(1000.milliseconds)
            }
        }
        if (!reachable) {
            emit(ConnectionStatus.Failed(FailReason.UNKNOWN))
            return@flow
        }
        registry.ip = television.ipAddress
        emit(ConnectionStatus.Connected(TvCredentials.None))

        var failures = 0
        var connected = true
        while (currentCoroutineContext().isActive) {
            delay(10_000.milliseconds)
            if (ping(television.ipAddress)) {
                failures = 0
                if (!connected) {
                    connected = true
                    emit(ConnectionStatus.Connected(TvCredentials.None))
                }
            } else {
                failures++
                if (connected) {
                    emit(ConnectionStatus.TempDisconnected)
                } else if (failures >= 3) {
                    emit(ConnectionStatus.Disconnected(recoverable = true))
                    return@flow
                }
                connected = false
            }
        }
    }

    override suspend fun disconnect() {
        registry.ip = null
    }

    private suspend fun ping(ip: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(
                Request.Builder().url("http://$ip:8060/query/device-info").build()
            ).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }
}