package com.olehkan.olehkan.data.network

import android.content.Context
import com.olehkan.olehkan.R
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BackendException(val statusCode: Int, val errorCode: String, message: String) : IOException(message)

class SupabaseHttp(
    context: Context,
    private val baseUrl: String = context.getString(R.string.supabase_url).trim(),
    private val publicKey: String = context.getString(R.string.supabase_publishable_key).trim(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false)
        .build(),
) {

    private fun checkConfiguration() {
        check(baseUrl.startsWith("https://") &&
            publicKey.isNotBlank()) {
            "Supabase belum dikonfigurasi. Hubungi pengelola aplikasi."
        }
        check(!publicKey.startsWith("sb_secret_")) {
            "Konfigurasi key tidak sesuai untuk aplikasi."
        }
        // Legacy service-role JWT juga ditolak; jangan gunakan kunci privat di APK.
        val parts = publicKey.split('.')
        if (parts.size == 3) {
            val payload = runCatching {
                String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            }.getOrDefault("")
            check(!payload.contains("service_role")) { "Konfigurasi key tidak sesuai untuk aplikasi." }
        }
    }

    suspend fun request(
        method: String,
        path: String,
        token: String? = null,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): String = withContext(Dispatchers.IO) {
        checkConfiguration()
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("apikey", publicKey)
            .header("Accept", "application/json")
        if (token != null) builder.header("Authorization", "Bearer $token")
        headers.forEach { (name, value) -> builder.header(name, value) }
        val requestBody = when {
            body != null -> body.toRequestBody("application/json".toMediaType())
            method in listOf("POST", "PUT", "PATCH") -> "{}".toRequestBody("application/json".toMediaType())
            else -> null
        }
        builder.method(method, requestBody)
        client.newCall(builder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val json = runCatching { JSONObject(text) }.getOrNull()
                val code = json?.optString("error_code")?.takeIf { it.isNotBlank() }
                    ?: json?.optString("code").orEmpty()
                val message = when {
                    code == "invalid_credentials" -> "Email atau password salah."
                    code == "email_not_confirmed" -> "Verifikasi email sebelum login."
                    code == "user_already_exists" -> "Email sudah terdaftar. Silakan login."
                    code == "weak_password" -> "Password belum memenuhi aturan keamanan server."
                    response.code == 429 -> "Terlalu banyak percobaan. Silakan tunggu."
                    response.code == 401 -> "Kredensial atau sesi tidak valid. Silakan login kembali."
                    response.code == 403 || code == "42501" -> "Anda tidak memiliki izin untuk operasi ini."
                    code == "23505" -> "Nama kategori sudah digunakan."
                    response.code >= 500 -> "Layanan sedang bermasalah. Silakan coba lagi."
                    response.code == 400 || response.code == 422 -> "Data tidak dapat diproses. Periksa input dan konfigurasi akun."
                    else -> "Permintaan gagal. Periksa koneksi dan konfigurasi layanan."
                }
                throw BackendException(response.code, code, message)
            }
            text
        }
    }

    suspend fun requestBytes(
        method: String,
        path: String,
        token: String? = null,
        bytes: ByteArray,
        contentType: String,
    ): String = withContext(Dispatchers.IO) {
        checkConfiguration()
        val requestBody = bytes.toRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("apikey", publicKey)
            .header("Accept", "application/json")
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .header("x-upsert", "true")
            .method(method, requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val json = runCatching { JSONObject(text) }.getOrNull()
                val code = json?.optString("error_code")?.takeIf { it.isNotBlank() }
                    ?: json?.optString("code").orEmpty()
                val message = when {
                    response.code == 401 -> "Kredensial atau sesi tidak valid. Silakan login kembali."
                    response.code == 403 || code == "42501" -> "Anda tidak memiliki izin untuk mengunggah gambar ini."
                    response.code == 413 -> "Ukuran gambar melebihi batas 5 MB."
                    response.code >= 500 -> "Layanan sedang bermasalah. Silakan coba lagi."
                    else -> "Upload gambar gagal. Periksa koneksi dan format file."
                }
                throw BackendException(response.code, code, message)
            }
            val publicPath = path.removePrefix("/storage/v1/object/")
            baseUrl.trimEnd('/') + "/storage/v1/object/public/" + publicPath
        }
    }

    companion object {
        fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    }
}
