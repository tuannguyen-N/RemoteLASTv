package com.tuannguyen.remotelastv.domain.repository

interface PinPairable {
    suspend fun submitPin(pin: String)
    suspend fun cancelSendPin()
}