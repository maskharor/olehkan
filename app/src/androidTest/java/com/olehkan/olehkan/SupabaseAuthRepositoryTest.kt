package com.olehkan.olehkan

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.olehkan.olehkan.data.auth.EncryptedSessionStore
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.data.network.SupabaseHttp
import com.olehkan.olehkan.data.repository.SupabaseAuthRepository
import java.security.KeyStore
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Respons tiruan melalui interceptor; tidak memakai akun atau project Supabase nyata. */
@RunWith(AndroidJUnit4::class)
class SupabaseAuthRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val id = "11111111-1111-1111-1111-111111111111"
    private lateinit var store: EncryptedSessionStore
    private lateinit var http: SupabaseHttp
    private lateinit var repository: SupabaseAuthRepository
    private lateinit var alias: String
    private var serverRole = "BUYER"
    private var fullName = "Buyer Uji"
    private var profileMissing = false
    private var verificationRequired = false
    private var expiresIn = 3600
    private var refreshRejected = false
    private var logoutRejected = false
    private var refreshCount = 0
    private var signupBody = ""
    private var updateBody = ""
    private var signupAuthorization: String? = null

    @Before fun setUp() {
        alias = "olehkan_test_${UUID.randomUUID()}"
        store = EncryptedSessionStore(context, alias, alias)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val grant = request.url.queryParameter("grant_type")
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val body = buffer.readUtf8()
            var code = 200
            val result = when {
                path.endsWith("/token") && grant == "refresh_token" -> {
                    refreshCount++
                    if (refreshRejected) { code = 400; """{"error_code":"refresh_token_not_found"}""" }
                    else tokenResponse()
                }
                path.endsWith("/token") -> tokenResponse()
                path.endsWith("/signup") -> {
                    signupBody = body
                    signupAuthorization = request.header("Authorization")
                    if (verificationRequired) """{"id":"$id"}""" else tokenResponse()
                }
                path.endsWith("/logout") -> {
                    if (logoutRejected) code = 500
                    "{}"
                }
                path.endsWith("/user") -> """{"id":"$id","email":"buyer@example.com"}"""
                path.endsWith("/profiles") && request.method == "PATCH" -> {
                    updateBody = body
                    fullName = JSONObject(body).getString("full_name")
                    """[{"id":"$id"}]"""
                }
                path.endsWith("/profiles") -> if (profileMissing) "[]" else
                    """[{"id":"$id","full_name":"$fullName","role":"$serverRole"}]"""
                else -> error("Unexpected test endpoint")
            }
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(code).message("Test response").body(result.toResponseBody()).build()
        }.build()
        http = SupabaseHttp(context, "https://example.test", "sb_publishable_test", client)
        repository = SupabaseAuthRepository(http, store)
    }

    @After fun tearDown() {
        store.clear()
        context.deleteSharedPreferences(alias)
        KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
    }

    private fun tokenResponse(): String = JSONObject()
        .put("access_token", "test-access-$refreshCount")
        .put("refresh_token", "test-refresh-$refreshCount")
        .put("expires_in", expiresIn).toString()

    @Test fun roleComesFromTrustedProfileAndSessionCanBeRestored() = runBlocking {
        serverRole = "ADMIN"
        repository.login("buyer@example.com", "Password8!")
        assertEquals(UserRole.ADMIN, repository.session.value.profile?.role)
        val restored = SupabaseAuthRepository(http, store)
        restored.restore()
        assertEquals(SessionStatus.AUTHENTICATED, restored.session.value.status)
        assertEquals(id, restored.session.value.profile?.id)
    }

    @Test fun verificationDoesNotCreateSessionOrSubmitRole() = runBlocking {
        verificationRequired = true
        assertTrue(repository.register("Buyer Uji", "buyer@example.com", "Password8!"))
        assertEquals(SessionStatus.SIGNED_OUT, repository.session.value.status)
        assertNull(store.read())
        assertNull(signupAuthorization)
        val body = JSONObject(signupBody)
        assertFalse(body.has("role"))
        assertFalse(body.getJSONObject("data").has("role"))
    }

    @Test fun expiredSessionRotatesRefreshToken() = runBlocking {
        expiresIn = 0
        repository.login("buyer@example.com", "Password8!")
        expiresIn = 3600
        repository.restore()
        assertEquals(1, refreshCount)
        assertEquals("test-refresh-1", JSONObject(store.read()!!).getString("refresh"))
    }

    @Test fun rejectedRefreshClearsSession() = runBlocking {
        expiresIn = 0
        repository.login("buyer@example.com", "Password8!")
        refreshRejected = true
        repository.restore()
        assertEquals(SessionStatus.SIGNED_OUT, repository.session.value.status)
        assertNull(store.read())
    }

    @Test fun missingProfileBlocksAuthenticatedContent() = runBlocking {
        profileMissing = true
        repository.login("buyer@example.com", "Password8!")
        assertEquals(SessionStatus.PROFILE_REQUIRED, repository.session.value.status)
        assertNull(repository.session.value.profile)
    }

    @Test fun unknownRoleIsNeverDefaultedToBuyerOrAdmin() = runBlocking {
        serverRole = "OWNER"
        try {
            repository.login("buyer@example.com", "Password8!")
            fail("Unknown role should fail")
        } catch (_: IllegalStateException) {
            assertEquals(SessionStatus.ERROR, repository.session.value.status)
            assertNull(repository.session.value.profile)
        }
    }

    @Test fun profileUpdateOnlySendsAllowedColumn() = runBlocking {
        repository.login("buyer@example.com", "Password8!")
        repository.updateProfile("Nama Baru")
        val body = JSONObject(updateBody)
        assertEquals(1, body.length())
        assertEquals("Nama Baru", body.getString("full_name"))
        assertEquals("Nama Baru", repository.session.value.profile?.fullName)
    }

    @Test fun logoutClearsLocalSessionEvenWhenServerIsUnavailable() = runBlocking {
        repository.login("buyer@example.com", "Password8!")
        logoutRejected = true
        assertFalse(repository.logout())
        assertNull(store.read())
        assertEquals(SessionStatus.SIGNED_OUT, repository.session.value.status)
    }

    @Test fun anonymousAdministrativeRequestIsRejected() = runBlocking {
        try {
            repository.authorized("POST", "/rest/v1/rpc/admin_overview")
            fail("Request without session must fail")
        } catch (_: IllegalStateException) {
            assertEquals(SessionStatus.SIGNED_OUT, repository.session.value.status)
        }
    }
}
