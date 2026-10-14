package com.tuannguyen.remotelastv.data.tv.lg

import com.connectsdk.device.ConnectableDevice
import com.connectsdk.service.WebOSTVService

fun ConnectableDevice.stableId(): String =
    getServiceByName(WebOSTVService.ID)
        ?.serviceDescription
        ?.uuid
        ?.removePrefix("uuid:")
        ?.takeIf { it.isNotBlank() }
        ?: id