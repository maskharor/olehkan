package com.olehkan.olehkan.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.model.AccountValidator
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.network.BackendException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val profileSaved: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val registering: Boolean = false,
    val showPassword: Boolean = false,
    val editName: String? = null,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer.get(application)
    private val repository = container.auth
    val session = repository.session
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState = _uiState.asStateFlow()
    private val _showLogin = MutableStateFlow(false)
    val showLogin = _showLogin.asStateFlow()
    private var restoreJob: Job? = null

    fun openLogin() {
        _uiState.update { it.copy(registering = false, password = "", confirmation = "", error = null, message = null, fieldErrors = emptyMap()) }
        _showLogin.value = true
    }

    fun openRegister() {
        _uiState.update { it.copy(registering = true, password = "", confirmation = "", error = null, message = null, fieldErrors = emptyMap()) }
        _showLogin.value = true
    }

    fun closeLogin() {
        _uiState.update { it.copy(error = null, message = null, fieldErrors = emptyMap()) }
        _showLogin.value = false
    }

    init {
        checkSession()
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                if (session.value.status != SessionStatus.SIGNED_OUT) checkSession()
            }
        }
        viewModelScope.launch {
            var previousUser: String? = null
            session.collect {
                val user = it.profile?.id
                if (user != previousUser) {
                    container.categories.clearCache()
                    container.wishlist.clearCache()
                    if (it.status == SessionStatus.AUTHENTICATED) {
                        try {
                            container.wishlist.loadFavorites()
                        } catch (_: Exception) {}
                    }
                }
                previousUser = user
            }
        }
    }

    fun checkSession() {
        if (restoreJob?.isActive == true || _uiState.value.busy) return
        restoreJob = viewModelScope.launch { repository.restore() }
    }

    fun onNameChange(value: String) { _uiState.update { it.copy(name = value, error = null, fieldErrors = it.fieldErrors - "name") } }
    fun onEmailChange(value: String) { _uiState.update { it.copy(email = value, error = null, fieldErrors = it.fieldErrors - "email") } }
    fun onPasswordChange(value: String) { _uiState.update { it.copy(password = value, error = null, fieldErrors = it.fieldErrors - "password") } }
    fun onConfirmationChange(value: String) { _uiState.update { it.copy(confirmation = value, error = null, fieldErrors = it.fieldErrors - "confirmation") } }
    fun togglePasswordVisibility() { _uiState.update { it.copy(showPassword = !it.showPassword) } }
    fun toggleRegistration() {
        _uiState.update { it.copy(registering = !it.registering, password = "", confirmation = "",
            error = null, message = null, fieldErrors = emptyMap()) }
    }
    fun prepareEditProfile() {
        _uiState.update { it.copy(editName = session.value.profile?.fullName.orEmpty(),
            profileSaved = false, error = null, message = null, fieldErrors = emptyMap()) }
    }
    fun onEditNameChange(value: String) { _uiState.update { it.copy(editName = value) } }

    fun login(email: String, password: String) {
        val errors = buildMap {
            AccountValidator.emailError(email)?.let { put("email", it) }
            if (password.isEmpty()) put("password", "Password wajib diisi.")
            else AccountValidator.passwordError(password)?.let { put("password", it) }
        }
        if (!validate(errors)) return
        perform {
            repository.login(email, password)
            _uiState.update { it.copy(password = "", confirmation = "") }
            _showLogin.value = false
        }
    }

    fun register(name: String, email: String, password: String, confirmation: String) {
        val errors = buildMap {
            AccountValidator.nameError(name)?.let { put("name", it) }
            AccountValidator.emailError(email)?.let { put("email", it) }
            AccountValidator.passwordError(password)?.let { put("password", it) }
            if (confirmation.isEmpty()) put("confirmation", "Konfirmasi password wajib diisi.")
            else if (confirmation != password) put("confirmation", "Konfirmasi password tidak cocok.")
        }
        if (!validate(errors)) return
        perform {
            val needsVerification = repository.register(name, email, password)
            _uiState.update { it.copy(password = "", confirmation = "") }
            if (needsVerification) {
                _uiState.update {
                    it.copy(message = "Akun berhasil dibuat! Silakan periksa email untuk verifikasi akun, lalu kembali dan login.")
                }
            } else {
                _showLogin.value = false
            }
        }
    }

    fun updateProfile(name: String) {
        val error = AccountValidator.nameError(name)
        if (!validate(if (error == null) emptyMap() else mapOf("name" to error))) return
        perform {
            repository.updateProfile(name)
            _uiState.update { it.copy(profileSaved = true, message = "Profil berhasil diperbarui.") }
        }
    }

    fun signOut() = logout()

    fun logout() = perform {
        val revoked = repository.logout()
        _showLogin.value = false
        _uiState.update { AccountUiState(busy = it.busy) }
        container.categories.clearCache()
        _uiState.update { it.copy(message = if (revoked) "Anda telah logout."
            else "Sesi di perangkat dihapus. Pencabutan sesi di server belum dapat dikonfirmasi.") }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(error = null, message = null, fieldErrors = emptyMap(), profileSaved = false) }
    }

    private fun validate(errors: Map<String, String>): Boolean {
        val firstError = errors.values.firstOrNull()
        _uiState.update { it.copy(error = firstError, message = null, fieldErrors = errors, profileSaved = false) }
        return errors.isEmpty()
    }

    private fun perform(action: suspend () -> Unit) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, error = null, profileSaved = false) }
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val message = when (e) {
                    is BackendException -> e.message
                    is IOException -> "Tidak dapat terhubung ke layanan. Periksa koneksi dan coba lagi."
                    is IllegalArgumentException, is IllegalStateException -> e.message
                    else -> "Operasi belum berhasil. Silakan coba lagi."
                }
                _uiState.update { it.copy(error = message) }
            } finally {
                _uiState.update { it.copy(busy = false) }
            }
        }
    }
}
