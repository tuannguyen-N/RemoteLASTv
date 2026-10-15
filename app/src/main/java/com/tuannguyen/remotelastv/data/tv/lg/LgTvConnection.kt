package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.command.ServiceCommandError
import com.tuannguyen.remotelastv.domain.model.ConnectionStatus
import com.tuannguyen.remotelastv.domain.model.FailReason
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TvCredentials
import com.tuannguyen.remotelastv.domain.repository.PinPairable
import com.tuannguyen.remotelastv.domain.repository.TvConnectionRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Singleton
class LgTvConnection @Inject constructor(
    private val registry: LgDeviceRegistry,
    private val discovery: LgTvDiscovery,
) : TvConnectionRepository, PinPairable {

    override fun connect(
        television: Television,
        credentials: TvCredentials
    ): Flow<ConnectionStatus> = callbackFlow {
        Log.d("LgConn", "connect() START ${television.id}")

        send(ConnectionStatus.Connecting)

        val cached = (registry.findById(television.id) ?: registry.find(television.ipAddress))
            ?.takeIf { it.services.isNotEmpty() }

        if (cached == null) {
            registry.remove(television.id)
        }

        val device = cached ?: run {
            val scanJob = launch { discovery.startScan().collect { } }
            try {
                registry.awaitById(television.id, DISCOVERY_WAIT_MS)
            } finally {
                scanJob.cancel()
            }
        }
        if (device == null) {
            send(ConnectionStatus.Failed(FailReason.DEVICE_NOT_FOUND))
            close()
            return@callbackFlow
        }
        registry.put(device)
        registry.select(device)

        val timeout = AtomicReference<Job?>(null)
        val reachedConnected = AtomicBoolean(false)

        fun armTimeout(ms: Long) {
            val job = launch {
                delay(ms)
                trySend(ConnectionStatus.Failed(FailReason.TIMEOUT))
                close()
            }
            timeout.getAndSet(job)?.cancel()
        }

        val listener = object : ConnectableDeviceListener {
            override fun onDeviceReady(d: ConnectableDevice) {
                timeout.getAndSet(null)?.cancel()
                reachedConnected.set(true)
                trySend(ConnectionStatus.Connected(TvCredentials.None))
            }

            override fun onPairingRequired(
                d: ConnectableDevice, s: DeviceService, type: DeviceService.PairingType
            ) {
                val status = when (type) {
                    DeviceService.PairingType.FIRST_SCREEN -> ConnectionStatus.WaitingForAuth
                    DeviceService.PairingType.PIN_CODE,
                    DeviceService.PairingType.MIXED -> ConnectionStatus.WaitingForPin
                    else -> null
                } ?: return
                armTimeout(PAIRING_TIMEOUT_MS)
                trySend(status)
            }

            override fun onConnectionFailed(d: ConnectableDevice, e: ServiceCommandError) {
                timeout.getAndSet(null)?.cancel()
                trySend(ConnectionStatus.Failed(e.toFailReason()))
                close()
            }

            override fun onDeviceDisconnected(d: ConnectableDevice, e: ServiceCommandError?) {
                timeout.getAndSet(null)?.cancel()
                Log.e("LgTv", "onDeviceDisconnected: code=${e?.code} msg=${e?.message}")

                val status =
                    if (reachedConnected.get() && e.isConnectionAborted()) {
                        ConnectionStatus.TempDisconnected
                    } else {
                        ConnectionStatus.Disconnected(reason = e?.message, recoverable = true)
                    }
                trySend(status)
                close()
            }

            override fun onCapabilityUpdated(
                d: ConnectableDevice?, added: List<String?>?, removed: List<String?>?
            ) = Unit
        }

        armTimeout(CONNECT_TIMEOUT_MS)
        device.addListener(listener)

        if (device.isConnected)
            listener.onDeviceReady(device)
        else
            device.connect()

        awaitClose {
            Log.d("LgConn", "awaitClose ${television.id} reached=${reachedConnected.get()}")
            timeout.getAndSet(null)?.cancel()
            device.removeListener(listener)
            if (!reachedConnected.get()) {
                device.disconnect()
                registry.remove(device.id)
            }
            registry.deselect(device)
        }
    }

    override suspend fun disconnect() {
        registry.activeDevice?.disconnect()
        registry.clearSelection()
    }

    override suspend fun submitPin(pin: String) {
        registry.activeDevice?.sendPairingKey(pin)
    }

    private fun ServiceCommandError.toFailReason() =
        if (code == 401) FailReason.PAIRING_REJECTED else FailReason.UNKNOWN

    private fun ServiceCommandError?.isConnectionAborted() =
        this?.message?.contains("connection aborted", ignoreCase = true) == true

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000L
        const val PAIRING_TIMEOUT_MS = 60_000L
        const val DISCOVERY_WAIT_MS = 6_000L
    }
}