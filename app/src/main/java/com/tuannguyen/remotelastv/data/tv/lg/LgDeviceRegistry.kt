package com.tuannguyen.remotelastv.data.tv.lg

import com.connectsdk.device.ConnectableDevice
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LgDeviceRegistry @Inject constructor() {
    private val devices = ConcurrentHashMap<String, ConnectableDevice>()
    fun put(device: ConnectableDevice) { devices[device.ipAddress] = device }
    fun get(ip: String): ConnectableDevice? = devices[ip]
    fun remove(ip: String) { devices.remove(ip) }
}