package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.core.TextInputStatusInfo
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.service.WebOSTVService
import com.connectsdk.service.capability.TextInputControl
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.command.ServiceSubscription
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LgDeviceRegistry @Inject constructor() {
    private val devices = ConcurrentHashMap<String, ConnectableDevice>()   // key = stableId (UUID)

    @Volatile
    var activeDevice: ConnectableDevice? = null
        private set

    @Volatile
    var keyboardFocused: Boolean = false
        private set

    private var textSub: ServiceSubscription<TextInputControl.TextInputStatusListener>? = null

    fun put(device: ConnectableDevice) {
        devices[device.stableId()] = device
    }

    fun findById(id: String): ConnectableDevice? =
        devices[id] ?: DiscoveryManager.getInstance()
            .allDevices.values
            .firstOrNull { it.stableId() == id }
            ?.also(::put)

    fun find(ip: String): ConnectableDevice? =
        devices.values.firstOrNull { it.ipAddress == ip }
            ?: DiscoveryManager.getInstance()
                .allDevices.values
                .firstOrNull { it.ipAddress == ip }
                ?.also(::put)

    suspend fun awaitById(id: String, timeoutMs: Long): ConnectableDevice? =
        withTimeoutOrNull(timeoutMs) {
            while (true) {
                findById(id)?.let { return@withTimeoutOrNull it }
                delay(250)
            }
            @Suppress("UNREACHABLE_CODE") null
        }

    fun remove(id: String) {
        devices.remove(id)
        if (activeDevice?.stableId() == id) activeDevice = null
    }

    fun select(device: ConnectableDevice) { activeDevice = device }

    fun clearSelection() { activeDevice = null }

    fun deselect(device: ConnectableDevice) {
        if (activeDevice === device) activeDevice = null
    }

    @Synchronized
    fun startKeyboardTracking(device: ConnectableDevice) {
        stopKeyboardTracking()
        val webOs = device.getServiceByName(WebOSTVService.ID) as? WebOSTVService ?: return
        textSub = webOs.subscribeTextInputStatus(object : TextInputControl.TextInputStatusListener {
            override fun onSuccess(status: TextInputStatusInfo) {
                keyboardFocused = status.isFocused
            }

            override fun onError(error: ServiceCommandError) {
                Log.e("LgTv", "text input sub failed: ${error.message}")
            }
        })
    }

    @Synchronized
    fun stopKeyboardTracking() {
        runCatching { textSub?.unsubscribe() }
        textSub = null
        keyboardFocused = false
    }
}