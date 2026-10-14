package com.tuannguyen.remotelastv.data.tv.lg

import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
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

    // Chờ discovery tìm thấy device có id này, dùng khi reconnect từ danh sách đã lưu
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
}