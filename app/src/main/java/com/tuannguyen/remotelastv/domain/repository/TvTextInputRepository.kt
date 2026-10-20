package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.TextCapability
import com.tuannguyen.remotelastv.domain.model.TextInputState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface TvTextInputRepository {
    val capabilities: Set<TextCapability>
    fun observeInput(): Flow<TextInputState>
    val keyboardShowRequests: Flow<Unit>
        get() = emptyFlow()

    suspend fun sendText(text: String): Result<Unit>
    suspend fun sendDelete(): Result<Unit>
    suspend fun sendEnter(): Result<Unit>
    suspend fun replaceText(text: String): Result<Unit>
}