package com.tuannguyen.remotelastv.data.tv.android_tv

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.repository.TvDiscoveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidTvDiscovery @Inject constructor(
    @ApplicationContext private val context: Context,
) : TvDiscoveryRepository {
    override val brand = TelevisionBrand.ANDROID_TV

    private val serviceTypes = listOf(
        "_androidtvremote2._tcp.",
        "_androidtvremote._tcp.",
    )

    override fun startScan(): Flow<Television> = callbackFlow {
        val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val found = Channel<NsdServiceInfo>(Channel.UNLIMITED)

        val listeners = serviceTypes.map { type ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onServiceFound(svc: NsdServiceInfo) {
                    found.trySend(svc)
                }

                override fun onServiceLost(svc: NsdServiceInfo) {}
                override fun onDiscoveryStarted(type: String) {}
                override fun onDiscoveryStopped(type: String) {}
                override fun onStartDiscoveryFailed(type: String, code: Int) {}
                override fun onStopDiscoveryFailed(type: String, code: Int) {}
            }
            nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
            listener
        }

        launch {
            val seen = mutableSetOf<String>()
            for (svc in found) {
                val resolved = nsd.resolve(svc) ?: continue
                val ip = resolved.host?.hostAddress ?: continue

                val btMac = resolved.attributes["bt"]
                    ?.toString(Charsets.UTF_8)
                    ?.trim()
                    ?.uppercase()
                    ?.takeIf { it.isNotEmpty() }
                val id = btMac ?: resolved.serviceName

                Log.d("AndroidTvDiscovery", "found ${resolved.serviceName} ip=$ip attrs=${
                    resolved.attributes.mapValues { it.value?.toString(Charsets.UTF_8) }
                }")

                if (!seen.add(id)) continue

                trySend(
                    Television(
                        id = id,
                        name = resolved.serviceName,
                        ipAddress = ip,
                        brand = TelevisionBrand.ANDROID_TV
                    )
                )
            }
        }

        awaitClose {
            found.close()
            listeners.forEach { runCatching { nsd.stopServiceDiscovery(it) } }
        }
    }

    private suspend fun NsdManager.resolve(svc: NsdServiceInfo): NsdServiceInfo? =
        suspendCancellableCoroutine { cont ->
            resolveService(svc, object : NsdManager.ResolveListener {
                override fun onServiceResolved(info: NsdServiceInfo) {
                    cont.resume(info)
                }

                override fun onResolveFailed(info: NsdServiceInfo, code: Int) {
                    cont.resume(null)
                }
            })
        }
}