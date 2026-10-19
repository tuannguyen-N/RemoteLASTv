package com.tuannguyen.remotelastv.data.tv.android_tv

import android.util.Log
import com.google.polo.wire.protobuf.PoloProto
import com.google.protobuf.ByteString
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLSocket

@Singleton
class PairingClient @Inject constructor(
    private val certManager: AndroidTvCertManager
) {

    enum class PinResult { SUCCESS, INVALID_PIN, REJECTED, ERROR }

    @Volatile
    private var socket: SSLSocket? = null
    @Volatile
    private var clientCert: Certificate? = null

    /** Bắt tay TLS + gửi Pairing/Options/Configuration. Thành công thì TV đang hiện mã PIN. */
    suspend fun start(ipAddress: String, alias: String, port: Int = PORT): Boolean =
        withContext(Dispatchers.IO) {
            cancel()
            try {
                clientCert = certManager.getCertificate(alias) ?: return@withContext false

                val s = certManager.createSocketFactory(alias).createSocket() as SSLSocket
                socket = s
                s.connect(InetSocketAddress(ipAddress, port), CONNECT_TIMEOUT_MS)
                s.soTimeout = READ_TIMEOUT_MS
                s.enabledProtocols = arrayOf("TLSv1.2")
                s.startHandshake()

                val input = s.inputStream
                val output = s.outputStream

                val ok = exchange(pairingRequest(), input, output) { it.hasPairingRequestAck() } &&
                        exchange(options(), input, output) { it.hasOptions() } &&
                        exchange(configuration(), input, output) { it.hasConfigurationAck() }
                if (!ok) cancel()
                ok
            } catch (e: Exception) {
                Log.e(TAG, "start error", e)
                cancel()
                false
            }
        }

    suspend fun finish(pin: String): PinResult = withContext(Dispatchers.IO) {
        val s = socket
        val client = clientCert
        if (s == null || client == null) return@withContext PinResult.ERROR
        try {
            val server = s.session.peerCertificates.first()
            val secret =
                computeSecret(client, server, pin) ?: return@withContext PinResult.INVALID_PIN

            val msg = outer {
                setSecret(PoloProto.Secret.newBuilder().setSecret(ByteString.copyFrom(secret)))
            }
            msg.writeDelimitedTo(s.outputStream)
            s.outputStream.flush()

            val resp = PoloProto.OuterMessage.parseDelimitedFrom(s.inputStream)
            if (resp?.status == PoloProto.OuterMessage.Status.STATUS_OK && resp.hasSecretAck()) {
                PinResult.SUCCESS
            } else {
                Log.e(TAG, "Pairing rejected: $resp")
                PinResult.REJECTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "finish error", e)
            PinResult.ERROR
        }
    }

    fun cancel() {
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.w(TAG, "cancel error", e)
        } finally {
            socket = null
            clientCert = null
        }
    }

    // ---------- messages ----------

    private fun outer(block: PoloProto.OuterMessage.Builder.() -> Unit): PoloProto.OuterMessage =
        PoloProto.OuterMessage.newBuilder()
            .setProtocolVersion(2)
            .setStatus(PoloProto.OuterMessage.Status.STATUS_OK)
            .apply(block)
            .build()

    private fun hexEncoding(): PoloProto.Options.Encoding =
        PoloProto.Options.Encoding.newBuilder()
            .setType(PoloProto.Options.Encoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
            .setSymbolLength(6)
            .build()

    private fun pairingRequest() = outer {
        setPairingRequest(
            PoloProto.PairingRequest.newBuilder()
                .setClientName("Remote App")
                .setServiceName("Android TV")
        )
    }

    private fun options() = outer {
        setOptions(
            PoloProto.Options.newBuilder()
                .addInputEncodings(hexEncoding())
                .setPreferredRole(PoloProto.Options.RoleType.ROLE_TYPE_INPUT)
        )
    }

    private fun configuration() = outer {
        setConfiguration(
            PoloProto.Configuration.newBuilder()
                .setEncoding(hexEncoding())
                .setClientRole(PoloProto.Options.RoleType.ROLE_TYPE_INPUT)
        )
    }

    private fun exchange(
        msg: PoloProto.OuterMessage,
        input: InputStream,
        output: OutputStream,
        expect: (PoloProto.OuterMessage) -> Boolean
    ): Boolean {
        msg.writeDelimitedTo(output)
        output.flush()
        val resp = PoloProto.OuterMessage.parseDelimitedFrom(input)
        if (resp == null) {
            Log.w(TAG, "No response from TV")
            return false
        }
        Log.d(TAG, "TV response: $resp")
        return resp.status == PoloProto.OuterMessage.Status.STATUS_OK && expect(resp)
    }

    // ---------- secret ----------

    /**
     * PIN 6 hex = 1 byte checksum + 2 byte nonce.
     * secret = SHA256(clientMod, clientExp, serverMod, serverExp, nonce); hash[0] phải khớp byte checksum.
     */
    private fun computeSecret(
        client: Certificate,
        server: Certificate,
        rawPin: String
    ): ByteArray? {
        val pin = rawPin.trim().uppercase()
        if (!PIN_REGEX.matches(pin)) return null

        val check = pin.take(2).toInt(16).toByte()
        val nonce = hexToBytes(pin.substring(2))

        val c = client.publicKey as RSAPublicKey
        val s = server.publicKey as RSAPublicKey
        val hash = MessageDigest.getInstance("SHA-256").apply {
            update(trimLeadingZeros(c.modulus.toByteArray()))
            update(trimLeadingZeros(c.publicExponent.toByteArray()))
            update(trimLeadingZeros(s.modulus.toByteArray()))
            update(trimLeadingZeros(s.publicExponent.toByteArray()))
            update(nonce)
        }.digest()

        return if (hash[0] == check) hash else null
    }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun trimLeadingZeros(bytes: ByteArray): ByteArray {
        var i = 0
        while (i < bytes.size && bytes[i] == 0.toByte()) i++
        return if (i == bytes.size) byteArrayOf(0) else bytes.copyOfRange(i, bytes.size)
    }

    private companion object {
        const val TAG = "PairingClient"
        const val PORT = 6467
        const val CONNECT_TIMEOUT_MS = 5_000
        const val READ_TIMEOUT_MS = 15_000
        val PIN_REGEX = Regex("^[0-9A-F]{6}$")
    }
}