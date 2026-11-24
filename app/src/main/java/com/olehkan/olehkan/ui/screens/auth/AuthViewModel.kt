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
    val preferences = container.preferences
    val isFirstTime = preferences.isFirstTime
    val session = repository.session
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState = _uiState.asStateFlow()
    private val _showLogin = MutableStateFlow(false)
    val showLogin = _showLogin.asStateFlow()
    private var restoreJob: Job? = null

    fun setFirstTime(value: Boolean) {
        preferences.setFirstTime(value)
    }

    fun openLogin() {
        _showLogin.value = true
    }

    fun closeLogin() {
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
                    if (previousUser != null) {
                        val wishlist = com.olehkan.olehkan.data.repository.WishlistRepositoryProvider.get()
                        wishlist.favoriteIds.value.toList().forEach { id -> wishlist.removeFromFavorites(id) }
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

    fun onNameChange(value: String) { _uiState.update { it.copy(name = value) } }
    fun onEmailChange(value: String) { _uiState.update { it.copy(email = value) } }
    fun onPasswordChange(value: String) { _uiState.update { it.copy(password = value) } }
    fun onConfirmationChange(value: String) { _uiState.update { it.copy(confirmation = value) } }
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
            if (confirmation != password) put("confirmation", "Konfirmasi password tidak sesuai.")
        }
        if (!validate(errors)) return
        perform {
            val needsVerification = repository.register(name, email, password)
            _uiState.update { it.copy(password = "", confirmation = "") }
            if (needsVerification) _uiState.update {
                it.copy(message = "Periksa email untuk verifikasi akun, lalu kembali dan login. Jika email sudah terdaftar, gunakan halaman login.")
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
        _uiState.update { it.copy(error = null, message = null, fieldErrors = errors, profileSaved = false) }
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
