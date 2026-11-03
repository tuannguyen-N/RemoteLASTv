package com.tuannguyen.remotelastv.data.tv

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Singleton
class BraviaDetector @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val jsonType = "application/json; charset=UTF-8".toMediaType()

    suspend fun isSonyBravia(ip: String): Boolean = withContext(Dispatchers.IO) {
        val body = """{"method":"getInterfaceInformation","id":1,"version":"1.0","params":[]}"""
            .toRequestBody(jsonType)
        val req = Request.Builder()
            .url("http://$ip/sony/system")
            .post(body)
            .build()

        runCatching {
            client.newCall(req).execute().use { resp ->
                when {
                    resp.code == 403 -> true
                    !resp.isSuccessful -> false
                    else -> {
                        val text = resp.body?.string().orEmpty()
                        //{"result":[{"productName":"TV","modelName":"KD-55X80J","productCategory":"tv","serverName":"","interfaceVersion":"6.0.2"}],"id":1}
                        text.contains("interfaceVersion") || text.contains("modelName")
                    }
                }
            }
        }.getOrDefault(false)
    }
}