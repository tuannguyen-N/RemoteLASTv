package com.tuannguyen.remotelastv.data.tv.android_tv

import android.util.Log
import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.PinPairable
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTvConnection @Inject constructor(
    private val certManager: AndroidTvCertManager,
    private val pairingClient: PairingClient,
    private val remoteClient: RemoteClient,
) : TvConnectionRepository, PinPairable {

    private val pinChannel = Channel<String>(Channel.CONFLATED)

    @Volatile
    private var closedSignal = CompletableDeferred<Unit>()

    override fun connect(
        television: Television,
        credentials: TvCredentials
    ): Flow<ConnectionStatus> = flow {
        emit(ConnectionStatus.Connecting)

        val closed = CompletableDeferred<Unit>()
        closedSignal = closed

        val alias = (credentials as? TvCredentials.AndroidTv)?.certAlias
            ?: certManager.aliasFor(television.id)
        var current: TvCredentials = credentials

        try {
            if (credentials !is TvCredentials.AndroidTv || !certManager.contains(alias)) {
                certManager.createIdentity(alias)
                pinChannel.tryReceive()

                if (!pairingClient.start(television.ipAddress, alias)) {
                    if (closed.isCompleted) {
                        certManager.delete(alias)
                        emit(ConnectionStatus.Disconnected(recoverable = false))
                    } else {
                        Log.e(
                            TAG,
                            "Android TV pairing connection failed: tv=${television.id}, " +
                                    "ip=${television.ipAddress}, port=${PAIRING_PORT}"
                        )
                        certManager.delete(alias)
                        emit(ConnectionStatus.Failed(FailReason.NETWORK_ERROR))
                    }
                    return@flow
                }

                emit(ConnectionStatus.WaitingForPin)

                val pin = withTimeoutOrNull(PIN_TIMEOUT_MS) {
                    select {
                        pinChannel.onReceive { it }
                        closed.onAwait { null }
                    }
                }
                if (pin == null) {
                    certManager.delete(alias)
                    emit(
                        if (closed.isCompleted) ConnectionStatus.Disconnected(recoverable = false)
                        else ConnectionStatus.Failed(FailReason.TIMEOUT)
                    )
                    return@flow
                }

                val pinResult = pairingClient.finish(pin)
                if (closed.isCompleted) {
                    certManager.delete(alias)
                    emit(ConnectionStatus.Disconnected(recoverable = false))
                    return@flow
                }

                when (pinResult) {
                    PairingClient.PinResult.SUCCESS -> Unit

                    PairingClient.PinResult.INVALID_PIN,
                    PairingClient.PinResult.REJECTED -> {
                        certManager.delete(alias)
                        emit(ConnectionStatus.Failed(FailReason.PAIRING_REJECTED))
                        return@flow
                    }

                    PairingClient.PinResult.ERROR -> {
                        Log.e(
                            TAG,
                            "Android TV pairing PIN exchange failed: tv=${television.id}, " +
                                    "ip=${television.ipAddress}, port=${PAIRING_PORT}"
                        )
                        certManager.delete(alias)
                        emit(ConnectionStatus.Failed(FailReason.NETWORK_ERROR))
                        return@flow
                    }
                }
                pairingClient.cancel()
                current = TvCredentials.AndroidTv(alias)
            }

            if (closed.isCompleted) {
                emit(ConnectionStatus.Disconnected(recoverable = false))
                return@flow
            }

            val androidCredentials = current as? TvCredentials.AndroidTv
            val remoteConnected = androidCredentials != null &&
                    remoteClient.connect(television.ipAddress, androidCredentials.certAlias)
            if (closed.isCompleted) {
                emit(ConnectionStatus.Disconnected(recoverable = false))
                return@flow
            }
            if (!remoteConnected) {
                Log.e(
                    TAG,
                    "Android TV remote connection failed: tv=${television.id}, " +
                            "ip=${television.ipAddress}, port=${REMOTE_PORT}"
                )
                emit(ConnectionStatus.Failed(FailReason.NETWORK_ERROR))
                return@flow
            }

            emit(ConnectionStatus.Connected(current))
            coroutineScope {
                val remoteClosed = async { remoteClient.awaitClosed() }
                try {
                    select {
                        closed.onAwait { true }
                        remoteClosed.onAwait { false }
                    }
                    emit(
                        if (closed.isCompleted) {
                            ConnectionStatus.Disconnected(recoverable = false)
                        } else {
                            ConnectionStatus.TempDisconnected
                        }
                    )
                } finally {
                    remoteClosed.cancel()
                }
            }
        } finally {
            pairingClient.cancel()
            remoteClient.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun disconnect() {
        closedSignal.complete(Unit)
        pairingClient.cancel()
        remoteClient.disconnect()
    }

    override suspend fun submitPin(pin: String) {
        pinChannel.send(pin)
    }

    override suspend fun cancelSendPin() {
        disconnect()
    }

    private companion object {
        const val TAG = "AndroidTvConnection"
        const val PAIRING_PORT = 6467
        const val REMOTE_PORT = 6466
        const val PIN_TIMEOUT_MS = 120_000L
    }
}