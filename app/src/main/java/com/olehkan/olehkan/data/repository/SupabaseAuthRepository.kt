package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.auth.EncryptedSessionStore
import com.olehkan.olehkan.data.model.AccountValidator
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.data.model.UserSessionState
import com.olehkan.olehkan.data.network.BackendException
import com.olehkan.olehkan.data.network.SupabaseHttp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SupabaseAuthRepository(
    private val http: SupabaseHttp,
    private val store: EncryptedSessionStore,
) {
    private data class Tokens(val access: String, val refresh: String, val expiresAt: Long)
    private val mutex = Mutex()
    private var tokens: Tokens? = null
    private val _session = MutableStateFlow(UserSessionState())
    val session = _session.asStateFlow()

    suspend fun restore() = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                if (tokens == null) {
                    val raw = try { store.read() } catch (_: Exception) {
                        store.clear()
                        null
                    }
                    if (raw != null) tokens = JSONObject(raw).let {
                        Tokens(it.getString("access"), it.getString("refresh"), it.getLong("expires_at"))
                    }
                }
                if (tokens == null) {
                    _session.value = UserSessionState(SessionStatus.SIGNED_OUT)
                } else {
                    ensureFreshLocked()
                    fetchProfileLocked()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (_session.value.status != SessionStatus.SIGNED_OUT) {
                    _session.value = UserSessionState(
                        SessionStatus.ERROR, error = "Sesi belum dapat diperiksa. Periksa koneksi dan coba lagi."
                    )
                }
            }
        }
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val response = http.request("POST", "/auth/v1/token?grant_type=password",
                body = JSONObject().put("email", email.trim()).put("password", password).toString())
            acceptTokensLocked(JSONObject(response))
            finishSignInLocked()
        }
    }

    /** true berarti email perlu diverifikasi; tidak menciptakan sesi palsu. */
    suspend fun register(fullName: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(AccountValidator.nameError(fullName) == null)
            require(AccountValidator.emailError(email) == null)
            require(AccountValidator.passwordError(password) == null)
            val response = JSONObject(http.request("POST", "/auth/v1/signup", body = JSONObject()
                .put("email", email.trim()).put("password", password)
                .put("data", JSONObject().put("full_name", fullName.trim())).toString()))
            if (response.optString("access_token").isNotBlank()) {
                acceptTokensLocked(response)
                finishSignInLocked()
                false
            } else {
                _session.value = UserSessionState(SessionStatus.SIGNED_OUT)
                true
            }
        }
    }

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            var revoked = true
            try {
                tokens?.let { http.request("POST", "/auth/v1/logout?scope=local", it.access) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                revoked = false
            }
            store.clear()
            tokens = null
            _session.value = UserSessionState(SessionStatus.SIGNED_OUT)
            revoked
        }
    }

    suspend fun updateProfile(fullName: String) {
        require(AccountValidator.nameError(fullName) == null) { AccountValidator.nameError(fullName).orEmpty() }
        val profile = _session.value.profile ?: throw IllegalStateException("Profil belum tersedia.")
        val result = authorized("PATCH",
            "/rest/v1/profiles?id=eq.${SupabaseHttp.encode(profile.id)}&select=id",
            JSONObject().put("full_name", fullName.trim()).toString(),
            mapOf("Prefer" to "return=representation"))
        check(JSONArray(result).length() == 1) { "Profil tidak berhasil diperbarui." }
        restore()
        check(_session.value.status == SessionStatus.AUTHENTICATED) {
            "Nama telah dikirim, tetapi profil belum dapat dimuat ulang. Silakan coba lagi."
        }
    }

    suspend fun authorized(
        method: String, path: String, body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (tokens == null) {
                _session.value = UserSessionState(SessionStatus.SIGNED_OUT)
                throw IllegalStateException("Silakan login kembali.")
            }
            ensureFreshLocked()
            requestLocked(method, path, body, headers)
        }
    }

    private suspend fun finishSignInLocked() {
        try {
            fetchProfileLocked()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (_session.value.status != SessionStatus.SIGNED_OUT || tokens != null) {
                _session.value = UserSessionState(
                    SessionStatus.ERROR, error = "Sesi tersimpan, tetapi profil belum dapat dimuat. Silakan coba lagi."
                )
            }
            throw e
        }
    }

    private suspend fun requestLocked(
        method: String, path: String, body: String? = null, headers: Map<String, String> = emptyMap(),
    ): String {
        try {
            return http.request(method, path, tokens!!.access, body, headers)
        } catch (e: BackendException) {
            if (e.statusCode != 401) throw e
            refreshLocked()
            try {
                return http.request(method, path, tokens!!.access, body, headers)
            } catch (retry: BackendException) {
                if (retry.statusCode == 401) invalidateLocked()
                throw retry
            }
        }
    }

    private suspend fun fetchProfileLocked() {
        val user = JSONObject(requestLocked("GET", "/auth/v1/user"))
        val id = user.getString("id")
        val rows = JSONArray(requestLocked("GET",
            "/rest/v1/profiles?id=eq.${SupabaseHttp.encode(id)}&select=id,full_name,role"))
        if (rows.length() == 0) {
            _session.value = UserSessionState(
                SessionStatus.PROFILE_REQUIRED,
                error = "Profil akun belum tersedia. Hubungi admin lalu coba muat ulang.",
            )
            return
        }
        val row = rows.getJSONObject(0)
        val role = UserRole.entries.find { it.name == row.getString("role") }
            ?: throw IllegalStateException("Role akun tidak dikenali.")
        _session.value = UserSessionState(
            SessionStatus.AUTHENTICATED,
            UserProfile(id, row.getString("full_name"), user.optString("email"), role),
        )
    }

    private suspend fun ensureFreshLocked() {
        if (tokens!!.expiresAt <= System.currentTimeMillis() / 1000 + 60) refreshLocked()
    }

    private suspend fun refreshLocked() {
        try {
            val response = http.request("POST", "/auth/v1/token?grant_type=refresh_token",
                body = JSONObject().put("refresh_token", tokens!!.refresh).toString())
            acceptTokensLocked(JSONObject(response))
        } catch (e: BackendException) {
            if (e.statusCode in listOf(400, 401, 403)) invalidateLocked()
            throw e
        }
    }

    private fun acceptTokensLocked(json: JSONObject) {
        val next = Tokens(
            json.getString("access_token"), json.getString("refresh_token"),
            json.optLong("expires_at", System.currentTimeMillis() / 1000 + json.getLong("expires_in")),
        )
        // Simpan rotasi refresh token sebelum memublikasikan sesi.
        store.write(JSONObject().put("access", next.access).put("refresh", next.refresh)
            .put("expires_at", next.expiresAt).toString())
        tokens = next
    }

    private fun invalidateLocked() {
        store.clear()
        tokens = null
        _session.value = UserSessionState(SessionStatus.SIGNED_OUT, error = "Sesi berakhir. Silakan login kembali.")
    }
}
