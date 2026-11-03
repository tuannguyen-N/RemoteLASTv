package com.tuannguyen.remotelastv.data.tv.cast.dlna

import android.os.SystemClock
import android.util.Log
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
        var responseCount = 0
        val startedAt = SystemClock.elapsedRealtime()
        Log.i(TAG, "Starting DLNA discovery: timeoutMs=$timeoutMs hintHost=${hintHost ?: "none"}")
        try {
            ssdp.search(timeoutMs, hintHost).collect { response ->
                responseCount++
                Log.d(
                    TAG,
                    "SSDP response #$responseCount: location=${response.location} usn=${response.usn ?: "unknown"}",
                )
                val device = withContext(Dispatchers.IO) {
                    UpnpDescription.fetch(response.location)
                }
                if (device == null) {
                    Log.w(
                        TAG,
                        "Could not load a DLNA renderer from SSDP location=${response.location}",
                    )
                    return@collect
                }

                devices[device.udn] = device
                val television = device.toTelevision()
                Log.i(
                    TAG,
                    "DLNA renderer found: name=${device.friendlyName} host=${device.host} " +
                            "manufacturer=${device.manufacturer ?: "unknown"} " +
                            "model=${device.modelName ?: "unknown"} brand=${television.brand} " +
                            "udn=${device.udn} avTransport=${device.avTransport.controlUrl}",
                )
                if (emitted.add(device.udn)) emit(television)
            }
        } finally {
            Log.i(
                TAG,
                "DLNA discovery ended: responses=$responseCount renderers=${emitted.size} " +
                        "elapsedMs=${SystemClock.elapsedRealtime() - startedAt}",
            )
        }
    }

    suspend fun findDevice(host: String, timeoutMs: Long = 5_000): DlnaDevice? {
        Log.i(TAG, "Searching for DLNA renderer at host=$host timeoutMs=$timeoutMs")
        val device = discover(timeoutMs, host)
            .firstOrNull { it.ipAddress == host }
            ?.let { television -> devices[television.id] }
        if (device == null) {
            Log.w(TAG, "No DLNA renderer found at host=$host")
        } else {
            Log.i(TAG, "Matched DLNA renderer at host=$host name=${device.friendlyName}")
        }
        return device
    }

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

    private companion object {
        const val TAG = "DlnaDiscovery"
    }
}
