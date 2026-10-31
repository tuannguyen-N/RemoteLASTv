package com.tuannguyen.remotelastv.data.tv.cast.dlna

import com.tuannguyen.remotelastv.data.tv.cast.server.LocalMediaServer
import com.tuannguyen.remotelastv.domain.model.CastException
import com.tuannguyen.remotelastv.domain.model.CastFailReason
import com.tuannguyen.remotelastv.domain.model.CastProviderId
import com.tuannguyen.remotelastv.domain.model.Television
import com.tuannguyen.remotelastv.domain.repository.CastProvider
import com.tuannguyen.remotelastv.domain.repository.CastSession
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DlnaCastProvider @Inject constructor(
    private val discovery: DlnaDiscovery,
    private val soap: UpnpSoapClient,
    private val mediaServer: LocalMediaServer,
) : CastProvider {
    override val id = CastProviderId.DLNA
    override val priority = 0

    override fun supports(television: Television): Boolean = true

    override suspend fun createSession(television: Television): CastSession {
        val device = discovery.findDevice(television.ipAddress)
            ?: throw CastException(CastFailReason.DEVICE_NOT_FOUND)
        return DlnaCastSession(device, soap, mediaServer)
    }
}
