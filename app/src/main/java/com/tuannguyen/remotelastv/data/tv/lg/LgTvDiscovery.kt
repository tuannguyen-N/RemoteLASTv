package com.tuannguyen.remotelastv.data.tv.lg

import android.util.Log
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.WebOSTVService
import com.connectsdk.service.command.ServiceCommandError
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale.getDefault

class LgTvDiscovery @Inject constructor(
    private val registry: LgDeviceRegistry
) : TvDiscoveryRepository {

    override val brand = TelevisionBrand.LG

    override fun startScan(): Flow<Television> = callbackFlow {
        val manager = DiscoveryManager.getInstance()
        Log.d("LgScan", "start, compatible sẵn có: ${manager.compatibleDevices.size}")

        val listener = object : DiscoveryManagerListener {
            override fun onDeviceAdded(m: DiscoveryManager, d: ConnectableDevice) {
                Log.d(
                    "LgScan",
                    "ADDED ${d.friendlyName} ${d.ipAddress} services=${d.services.map { it.serviceName }}"
                )
                handle(d)
            }

            override fun onDeviceUpdated(m: DiscoveryManager, d: ConnectableDevice) {
                Log.d(
                    "LgScan",
                    "UPDATED ${d.friendlyName} services=${d.services.map { it.serviceName }}"
                )
                handle(d)
            }

            override fun onDeviceRemoved(m: DiscoveryManager, d: ConnectableDevice) {
                Log.d("LgScan", "REMOVED ${d.friendlyName}")
                registry.remove(d.stableId())
            }

            override fun onDiscoveryFailed(m: DiscoveryManager, e: ServiceCommandError) {
                Log.e("LgScan", "FAILED ${e.message}")
                close(Exception(e.message))
            }

            private fun handle(d: ConnectableDevice) {
                if (d.getServiceByName(WebOSTVService.ID) == null) return
                registry.put(d)
                trySend(
                    Television(
                        id = d.stableId(),
                        ipAddress = d.ipAddress,
                        name = formatName(d.friendlyName),
                        brand = TelevisionBrand.LG,
                    )
                )
            }
        }

        manager.addListener(listener)
        manager.start()
        awaitClose { manager.removeListener(listener) }
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    private fun formatName(name: String): String = name.replace(Regex("\\[.*?]"), "").trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
}