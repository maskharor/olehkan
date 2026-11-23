package com.olehkan.olehkan.data.model

enum class UserRole { ADMIN, BUYER }

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
)

enum class SessionStatus { LOADING, SIGNED_OUT, AUTHENTICATED, PROFILE_REQUIRED, ERROR }

data class UserSessionState(
    val status: SessionStatus = SessionStatus.LOADING,
    val profile: UserProfile? = null,
    val error: String? = null,
)

object AccountValidator {
    fun nameError(name: String): String? = when {
        name.trim().isBlank() -> "Nama lengkap wajib diisi."
        name.trim().length > 100 -> "Nama maksimal 100 karakter."
        else -> null
    }

    fun emailError(email: String): String? =
        if (Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) null
        else "Masukkan alamat email yang valid."

    fun passwordError(password: String): String? =
        if (password.length >= 8) null else "Password minimal 8 karakter."
}
