package com.olehkan.olehkan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Penyimpanan preferensi aplikasi lokal menggunakan SharedPreferences.
 * Sesuai arsitektur dan spesifikasi:
 * - HANYA menyimpan flag `isFirstTime` untuk menentukan alur Onboarding.
 * - Status login TIDAK disimpan sebagai flag manual, melainkan bersumber
 *   langsung dari verifikasi sesi Supabase Auth via SupabaseAuthRepository.
 */
class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _isFirstTime = MutableStateFlow(prefs.getBoolean(KEY_IS_FIRST_TIME, true))
    val isFirstTime: StateFlow<Boolean> = _isFirstTime.asStateFlow()

    fun getIsFirstTime(): Boolean = prefs.getBoolean(KEY_IS_FIRST_TIME, true)

    fun setFirstTime(value: Boolean) {
        prefs.edit().putBoolean(KEY_IS_FIRST_TIME, value).apply()
        _isFirstTime.value = value
    }

    companion object {
        private const val PREF_NAME = "olehkan_app_preferences"
        private const val KEY_IS_FIRST_TIME = "is_first_time"
    }
}
