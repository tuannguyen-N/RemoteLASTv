package com.tuannguyen.remotelastv.data.tv.cast.dlna

import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvDiscovery
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@Singleton
class DlnaDiscovery @Inject constructor(
    private val ssdp: SsdpDiscovery,
) : TvDiscovery {
    private val devices = ConcurrentHashMap<String, DlnaDevice>()

    override fun discover(timeoutMs: Long, hintHost: String?): Flow<Television> = flow {
        val emitted = HashSet<String>()
        ssdp.search(timeoutMs, hintHost).collect { response ->
            val device = withContext(Dispatchers.IO) { UpnpDescription.fetch(response.location) }
                ?: return@collect
            devices[device.udn] = device
            if (emitted.add(device.udn)) emit(device.toTelevision())
        }
    }

    suspend fun findDevice(host: String, timeoutMs: Long = 5_000): DlnaDevice? =
        discover(timeoutMs, host)
            .firstOrNull { it.ipAddress == host }
            ?.let { television -> devices[television.id] }

    private fun DlnaDevice.toTelevision() = Television(
        id = udn,
        ipAddress = host,
        name = friendlyName,
        brand = when {
            manufacturer.orEmpty().contains("LG", ignoreCase = true) -> TelevisionBrand.LG
            manufacturer.orEmpty().contains("Samsung", ignoreCase = true) -> TelevisionBrand.SAMSUNG
            manufacturer.orEmpty().contains("Android", ignoreCase = true) ||
                    modelName.orEmpty()
                        .contains("Android", ignoreCase = true) -> TelevisionBrand.ANDROID_TV

            else -> TelevisionBrand.UNKNOWN
        },
    )
}
