package com.tuannguyen.remotelastv.domain.tv

sealed class TvException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotConnected : TvException("No active TV connection")
    class Unsupported(feature: String) : TvException("$feature is not supported on this TV")
    class CommandFailed(cause: Throwable) : TvException("Command failed", cause)
}