package com.tuannguyen.remotelastv.data.tv.android_tv

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.math.sqrt

class VoiceRecorder @Inject constructor() {

    @SuppressLint("MissingPermission")
    /** Mỗi phần tử là một khối PCM16 mono. Hủy collect là mic tự dừng và release. */
    fun record(): Flow<ByteArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        check(minBuffer > 0) { "Unsupported microphone format" }

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer * 2, READ_SIZE * 4),
        )
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) {
                "Microphone could not be initialized"
            }
            recorder.startRecording()

            val buffer = ByteArray(READ_SIZE)
            while (currentCoroutineContext().isActive) {
                val count = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                if (count < 0) error("Microphone read failed: $count")
                if (count == 0) break
                emit(buffer.copyOf(count))
            }
        } finally {
            try {
                recorder.stop()
            } catch (_: IllegalStateException) {
            }
            recorder.release()
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val SAMPLE_RATE = 8_000
        const val BYTES_PER_SAMPLE = 2
        private const val READ_SIZE = 2_048
    }
}

internal fun pcmRms(pcm: ByteArray): Int {
    var sum = 0.0
    var n = 0
    var i = 0
    while (i + 1 < pcm.size) {
        val s = ((pcm[i].toInt() and 0xFF) or (pcm[i + 1].toInt() shl 8)).toShort().toInt()
        sum += s.toDouble() * s
        n++
        i += 2
    }
    return if (n == 0) 0 else sqrt(sum / n).toInt()
}

internal fun amplifyPcm16(pcm: ByteArray, gain: Float) {
    var i = 0
    while (i + 1 < pcm.size) {
        val s = ((pcm[i].toInt() and 0xFF) or (pcm[i + 1].toInt() shl 8)).toShort().toInt()
        val a = (s * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        pcm[i] = a.toByte()
        pcm[i + 1] = (a shr 8).toByte()
        i += 2
    }
}

internal class SilenceDetector(
    private val minThreshold: Int = 300,
    private val calibrationMs: Long = 400,
    private val silenceTimeoutMs: Long = 1_500,
    private val noSpeechTimeoutMs: Long = 5_000,
    private val maxDurationMs: Long = 5_000,
) {
    var threshold = minThreshold
        private set

    private var elapsedMs = 0L
    private var noiseFloor = Int.MAX_VALUE
    private var heardSpeech = false
    private var silentMs = 0L

    fun onChunk(rms: Int, durationMs: Long): Boolean {
        elapsedMs += durationMs

        if (elapsedMs >= maxDurationMs) return true

        if (elapsedMs <= calibrationMs) {
            noiseFloor = minOf(noiseFloor, rms)
            threshold = maxOf(minThreshold, (noiseFloor * 2.5f).toInt())
            return false
        }

        if (rms >= threshold) {
            heardSpeech = true
            silentMs = 0
        } else {
            silentMs += durationMs
        }

        return if (heardSpeech) silentMs >= silenceTimeoutMs
        else elapsedMs >= noSpeechTimeoutMs
    }
}