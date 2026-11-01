package com.tuannguyen.remotelastv.domain.model

class CastException(
    val reason: CastFailReason,
    cause: Throwable? = null,
) : Exception(reason.name, cause)
