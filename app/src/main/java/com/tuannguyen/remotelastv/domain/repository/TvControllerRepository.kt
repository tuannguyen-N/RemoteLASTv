package com.tuannguyen.remotelastv.domain.repository

import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TouchpadEvent
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.VolumeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

interface TvControllerRepository {
    val capabilities: Set<TvCapability>
    val touchpadMode: TouchpadMode get() = TouchpadMode.DIRECTIONAL

    suspend fun sendKey(key: TvKey): Result<Unit>
    suspend fun getApps(): Result<List<KnownApp>>
    suspend fun launchApp(app: KnownApp): Result<Unit>
    suspend fun startVoiceRecording(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Voice recording is not supported"))

    suspend fun stopVoiceRecording(): Result<Unit> = Result.success(Unit)
    fun observeVoiceRecording(): Flow<Boolean> = flowOf(false)
    suspend fun sendTouchpad(event: TouchpadEvent): Result<Unit> = when (event) {
        TouchpadEvent.Tap -> sendKey(TvKey.OK)
        else -> Result.success(Unit)
    }

    fun observeVolume(): Flow<VolumeState> = emptyFlow()
}