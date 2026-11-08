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

        val scanJob = if (cached == null) launch { discovery.startScan().collect { } } else null
        val restored = if (cached == null) {
            registry.restoreFromStore(television.id, television.ipAddress)
        } else {
            null
        }
        if (cached == null && restored == null) registry.remove(television.id)

        val initialDevice = cached
            ?: restored
            ?: registry.awaitById(television.id, DISCOVERY_WAIT_MS)
            ?: run {
                scanJob?.cancel()
                send(ConnectionStatus.Failed(FailReason.DEVICE_NOT_FOUND))
                close()
                return@callbackFlow
            }
        var device = initialDevice
        var usingRestoredDevice = cached == null && restored != null
        var fallbackAttempted = false
        registry.put(device)
        registry.select(device)

        val timeout = AtomicReference<Job?>(null)
        val reachedConnected = AtomicBoolean(false)
        lateinit var listener: ConnectableDeviceListener
        lateinit var fallbackToDiscovery: () -> Unit

        fun fail(reason: FailReason) {
            timeout.getAndSet(null)?.cancel()
            trySend(ConnectionStatus.Failed(reason))
            close()
        }

        fun armTimeout(ms: Long, fallbackOnTimeout: Boolean = usingRestoredDevice) {
            val job = launch {
                delay(ms)
                if (fallbackOnTimeout && usingRestoredDevice && !fallbackAttempted) {
                    fallbackToDiscovery()
                } else {
                    fail(FailReason.TIMEOUT)
                }
            }
            timeout.getAndSet(job)?.cancel()
        }

        listener = object : ConnectableDeviceListener {
            override fun onDeviceReady(d: ConnectableDevice) {
                timeout.getAndSet(null)?.cancel()
                reachedConnected.set(true)
                scanJob?.cancel()
                registry.startKeyboardTracking(d)
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
                armTimeout(PAIRING_TIMEOUT_MS, fallbackOnTimeout = false)
                trySend(status)
            }

            override fun onConnectionFailed(d: ConnectableDevice, e: ServiceCommandError) {
                timeout.getAndSet(null)?.cancel()
                val reason = e.toFailReason()
                if (usingRestoredDevice && !fallbackAttempted &&
                    reason != FailReason.PAIRING_DECLINED &&
                    reason != FailReason.PAIRING_REJECTED
                ) {
                    fallbackToDiscovery()
                } else {
                    fail(reason)
                }
            }

            override fun onDeviceDisconnected(d: ConnectableDevice, e: ServiceCommandError?) {
                timeout.getAndSet(null)?.cancel()
                Log.e("LgTv", "onDeviceDisconnected: code=${e?.code} msg=${e?.message}")

                if (usingRestoredDevice && !fallbackAttempted && !reachedConnected.get() &&
                    !e.isPairingFailure()
                ) {
                    fallbackToDiscovery()
                    return
                }

                val status =
                    when {
                        e.isPairingDeclined() ->
                            ConnectionStatus.Failed(FailReason.PAIRING_DECLINED)

                        reachedConnected.get() && e.isConnectionAborted() ->
                            ConnectionStatus.TempDisconnected

                        else ->
                            ConnectionStatus.Disconnected(reason = e?.message, recoverable = true)
                    }
                trySend(status)
                close()
            }

            override fun onCapabilityUpdated(
                d: ConnectableDevice?, added: List<String?>?, removed: List<String?>?
            ) = Unit
        }

        fallbackToDiscovery = fallback@{
            if (fallbackAttempted) return@fallback
            fallbackAttempted = true
            timeout.getAndSet(null)?.cancel()
            val previousDevice = device
            launch {
                val discovered = registry.awaitReplacement(
                    television.id,
                    previousDevice,
                    DISCOVERY_WAIT_MS,
                )
                if (discovered == null) {
                    fail(FailReason.DEVICE_NOT_FOUND)
                    return@launch
                }

                previousDevice.removeListener(listener)
                previousDevice.disconnect()
                device = discovered
                usingRestoredDevice = false
                registry.put(discovered)
                registry.select(discovered)
                discovered.addListener(listener)
                armTimeout(CONNECT_TIMEOUT_MS)
                if (discovered.isConnected) {
                    listener.onDeviceReady(discovered)
                } else {
                    discovered.connect()
                }
            }
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
            scanJob?.cancel()
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

    override suspend fun cancelSendPin() {
        registry.activeDevice?.cancelPairing()
    }

    private fun ServiceCommandError.toFailReason() = when {
        isPairingDeclined() -> FailReason.PAIRING_DECLINED
        code == 401 -> FailReason.PAIRING_REJECTED
        else -> FailReason.UNKNOWN
    }

    private fun ServiceCommandError?.isConnectionAborted() =
        this?.message?.contains("connection aborted", ignoreCase = true) == true

    private fun ServiceCommandError?.isPairingDeclined() =
        this != null &&
                (code == 403 || message?.contains(
                    "user rejected pairing",
                    ignoreCase = true
                ) == true)

    private fun ServiceCommandError?.isPairingFailure() =
        this.isPairingDeclined() || this?.code == 401

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000L
        const val PAIRING_TIMEOUT_MS = 60_000L
        const val DISCOVERY_WAIT_MS = 6_000L
    }
}