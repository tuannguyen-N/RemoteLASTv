package com.tuannguyen.remotelastv.data.tv.android_tv

import android.util.Log
import android.view.KeyEvent
import com.tuannguyen.remotelastv.data.tv.AppCatalog
import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand
import com.tuannguyen.remotelastv.domain.model.TouchpadMode
import com.tuannguyen.remotelastv.domain.model.TvCapability
import com.tuannguyen.remotelastv.domain.model.TvKey
import com.tuannguyen.remotelastv.domain.model.VolumeState
import com.tuannguyen.remotelastv.domain.repository.TvControllerRepository
import com.tuannguyen.remotelastv.domain.tv.TvException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTvTController @Inject constructor(
    private val remoteClient: RemoteClient,
    private val voiceRecorder: VoiceRecorder,
) : TvControllerRepository {
    override val capabilities: Set<TvCapability> =
        setOf(TvCapability.VOLUME_STATE, TvCapability.VOICE)
    override val touchpadMode: TouchpadMode get() = TouchpadMode.DIRECTIONAL

    private val voiceMutex = Mutex()
    private val voiceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _voiceRecording = MutableStateFlow(false)
    private var voiceJob: Job? = null

    override fun observeVoiceRecording(): Flow<Boolean> = _voiceRecording.asStateFlow()

    override suspend fun startVoiceRecording(): Result<Unit> = voiceMutex.withLock {
        if (voiceJob?.isActive == true) return@withLock Result.success(Unit)
        if (!remoteClient.isConnected) return@withLock Result.failure(TvException.NotConnected())

        val sessionId = try {
            remoteClient.startVoiceSession()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withLock Result.failure(TvException.CommandFailed(e))
        }

        _voiceRecording.value = true
        voiceJob = voiceScope.launch { streamVoice(sessionId) }
        Result.success(Unit)
    }

    override suspend fun stopVoiceRecording(): Result<Unit> = voiceMutex.withLock {
        voiceJob?.cancelAndJoin() // finally trong streamVoice sẽ gửi VoiceEnd
        voiceJob = null
        Result.success(Unit)
    }

    private suspend fun streamVoice(sessionId: Int) {
        val pending = ByteArrayOutputStream(VOICE_SEND_CHUNK_SIZE)
        val detector = SilenceDetector()

        try {
            voiceRecorder.record()
                .takeWhile { remoteClient.isConnected }
                .transformWhile { pcm ->
                    val durationMs = pcm.size * 1_000L /
                            (VoiceRecorder.SAMPLE_RATE * VoiceRecorder.BYTES_PER_SAMPLE)

                    val rms = pcmRms(pcm)
                    val finished = detector.onChunk(rms, durationMs)
                    Log.d(TAG, "rms=$rms threshold=${detector.threshold}")

                    amplifyPcm16(pcm, VOICE_GAIN)
                    emit(pcm)

                    !finished
                }
                .collect { pcm ->
                    pending.write(pcm)
                    if (pending.size() >= VOICE_SEND_CHUNK_SIZE) {
                        remoteClient.sendVoicePayload(sessionId, pending.toByteArray())
                        pending.reset()
                    }
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Voice streaming stopped unexpectedly", e)
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    if (pending.size() > 0 && remoteClient.isConnected) {
                        remoteClient.sendVoicePayload(sessionId, pending.toByteArray())
                    }
                    remoteClient.sendVoiceEnd(sessionId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to finish voice session", e)
                } finally {
                    _voiceRecording.value = false
                }
            }
        }
    }

    override suspend fun sendKey(key: TvKey): Result<Unit> {
        if (!remoteClient.isConnected) {
            return Result.failure(TvException.NotConnected())
        }

        return try {
            remoteClient.sendKey(key.toAndroidKeyCode())
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(TvException.CommandFailed(e))
        }
    }

    override suspend fun getApps(): Result<List<KnownApp>> {
        return Result.success(
            KnownApp.entries.filter { app ->
                AppCatalog.targetsOf(app, TelevisionBrand.ANDROID_TV).isNotEmpty()
            }
        )
    }

    override suspend fun launchApp(app: KnownApp): Result<Unit> {
        val appLink = AppCatalog.targetsOf(app, TelevisionBrand.ANDROID_TV).firstOrNull()
            ?: return Result.failure(TvException.AppNotAvailable(app))
        if (!remoteClient.isConnected) {
            return Result.failure(TvException.NotConnected())
        }

        return try {
            remoteClient.launchApp(appLink)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(TvException.CommandFailed(e))
        }
    }

    private fun TvKey.toAndroidKeyCode(): Int = when (this) {
        TvKey.UP -> KeyEvent.KEYCODE_DPAD_UP
        TvKey.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        TvKey.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
        TvKey.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
        TvKey.OK -> KeyEvent.KEYCODE_DPAD_CENTER
        TvKey.BACK -> KeyEvent.KEYCODE_BACK
        TvKey.HOME -> KeyEvent.KEYCODE_HOME
        TvKey.MENU -> KeyEvent.KEYCODE_MENU
        TvKey.POWER -> KeyEvent.KEYCODE_POWER
        TvKey.VOLUME_UP -> KeyEvent.KEYCODE_VOLUME_UP
        TvKey.VOLUME_DOWN -> KeyEvent.KEYCODE_VOLUME_DOWN
        TvKey.MUTE -> KeyEvent.KEYCODE_VOLUME_MUTE
        TvKey.CHANNEL_UP -> KeyEvent.KEYCODE_CHANNEL_UP
        TvKey.CHANNEL_DOWN -> KeyEvent.KEYCODE_CHANNEL_DOWN
        TvKey.NUMBER_0 -> KeyEvent.KEYCODE_0
        TvKey.NUMBER_1 -> KeyEvent.KEYCODE_1
        TvKey.NUMBER_2 -> KeyEvent.KEYCODE_2
        TvKey.NUMBER_3 -> KeyEvent.KEYCODE_3
        TvKey.NUMBER_4 -> KeyEvent.KEYCODE_4
        TvKey.NUMBER_5 -> KeyEvent.KEYCODE_5
        TvKey.NUMBER_6 -> KeyEvent.KEYCODE_6
        TvKey.NUMBER_7 -> KeyEvent.KEYCODE_7
        TvKey.NUMBER_8 -> KeyEvent.KEYCODE_8
        TvKey.NUMBER_9 -> KeyEvent.KEYCODE_9
        TvKey.DELETE -> KeyEvent.KEYCODE_DEL
        TvKey.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        TvKey.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
        TvKey.PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        TvKey.SEARCH -> KeyEvent.KEYCODE_SEARCH
    }

    override fun observeVolume(): Flow<VolumeState> = remoteClient.volumeState

    private fun amplifyPcm16InPlace(buffer: ByteArray, length: Int, gain: Float) {
        var index = 0
        while (index + 1 < length) {
            val sample = (
                    (buffer[index].toInt() and 0xFF) or
                            (buffer[index + 1].toInt() shl 8)
                    ).toShort().toInt()
            val adjusted =
                (sample * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[index] = adjusted.toByte()
            buffer[index + 1] = (adjusted shr 8).toByte()
            index += 2
        }
    }

    private fun pcmRms(buffer: ByteArray, length: Int): Int {
        var sumSquares = 0.0
        var sampleCount = 0
        var index = 0
        while (index + 1 < length) {
            val sample = (
                    (buffer[index].toInt() and 0xFF) or
                            (buffer[index + 1].toInt() shl 8)
                    ).toShort().toInt()
            sumSquares += sample.toDouble() * sample
            sampleCount++
            index += 2
        }
        return if (sampleCount == 0) 0 else kotlin.math.sqrt(sumSquares / sampleCount).toInt()
    }

    private companion object {
        const val TAG = "AndroidTvTController"
        const val VOICE_SAMPLE_RATE = 8_000
        const val VOICE_BYTES_PER_SAMPLE = 2
        const val VOICE_READ_SIZE = 2_048
        const val VOICE_SEND_CHUNK_SIZE = 16 * 1024
        const val VOICE_GAIN = 2f
        const val VOICE_SPEECH_RMS_THRESHOLD = 300
        const val VOICE_SILENCE_TIMEOUT_MS = 2_000L
        const val VOICE_MAX_DURATION_MS = 10_000L
    }
}