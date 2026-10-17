package com.tuannguyen.remotelastv.domain.tv

import com.tuannguyen.remotelastv.domain.model.KnownApp

sealed class TvException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotConnected : TvException("No active TV connection")
    class Unsupported(feature: String) : TvException("$feature is not supported on this TV")
    class AppNotAvailable(app: KnownApp) : TvException("App $app is not available on this TV")
    class CommandFailed(cause: Throwable) : TvException("Command failed", cause)
}