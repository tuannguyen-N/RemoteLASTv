package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.TvApp
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey

interface TvControllerRepository {
    val capabilities: Set<TvCapability>

    suspend fun sendKey(key: TvKey): Result<Unit>
    suspend fun launchApp(channel: TvApp): Result<Unit>
//    suspend fun sendText(text: TextChange, isSystemIme: Boolean, fullText: String): Result<Unit>
//    suspend fun syncText(newText: String, textOnTv: String): Result<Unit>
}