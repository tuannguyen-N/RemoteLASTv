package com.tuannguyen.remotelastv.data.tv.android_tv

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.auth.x500.X500Principal
import org.bouncycastle.x509.X509V3CertificateGenerator

/**
 * Client cert dạng software key (RSA 2048) lưu trong file PKCS12 ở bộ nhớ riêng của app.
 * Không dùng AndroidKeyStore vì Conscrypt ký TLS bằng key đó hay lỗi
 * "RSA routines: internal error" khi bắt tay với Android TV.
 * TvCredentials.AndroidTv(certAlias) vẫn chỉ cần lưu alias.
 */
@Singleton
class AndroidTvCertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun aliasFor(tvId: String) = "androidtv_" + tvId.replace(Regex("[^A-Za-z0-9]"), "_")

    fun contains(alias: String): Boolean = file(alias).exists()

    fun delete(alias: String) {
        file(alias).delete()
    }

    /** Tạo cặp khóa RSA + cert self-signed (sống 10 năm). Ghi đè nếu alias đã tồn tại. */
    fun createIdentity(alias: String) {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

        val now = System.currentTimeMillis()
        val dn = X500Principal("CN=AndroidTvRemote,O=RemoteLaStv")
        val cert = X509V3CertificateGenerator().apply {
            setSerialNumber(BigInteger.valueOf(now))
            setIssuerDN(dn)
            setSubjectDN(dn)
            setNotBefore(Date(now - 60_000))
            setNotAfter(Date(now + 10L * 365 * 24 * 60 * 60 * 1000))
            setPublicKey(keyPair.public)
            setSignatureAlgorithm("SHA256WithRSA")
        }.generate(keyPair.private)

        val ks = KeyStore.getInstance("PKCS12").apply {
            load(null, null)
            setKeyEntry(ENTRY, keyPair.private, PASSWORD, arrayOf(cert))
        }
        FileOutputStream(file(alias)).use { ks.store(it, PASSWORD) }
    }

    fun getCertificate(alias: String): X509Certificate? =
        load(alias)?.getCertificate(ENTRY) as? X509Certificate

    /** TV dùng cert self-signed nên trust-all (có thể pin lại sau). */
    @Suppress("CustomX509TrustManager", "TrustAllX509TrustManager")
    fun createSocketFactory(alias: String): SSLSocketFactory {
        val ks = load(alias) ?: error("Không tìm thấy cert cho $alias")
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, PASSWORD)

        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        })

        return SSLContext.getInstance("TLS").apply {
            init(kmf.keyManagers, trustAll, SecureRandom())
        }.socketFactory
    }

    private fun load(alias: String): KeyStore? {
        val f = file(alias)
        if (!f.exists()) return null
        return KeyStore.getInstance("PKCS12").apply {
            FileInputStream(f).use { load(it, PASSWORD) }
        }
    }

    private fun file(alias: String) = File(context.filesDir, "$alias.p12")

    private companion object {
        const val ENTRY = "client"
        val PASSWORD = "androidtv".toCharArray()
    }
}