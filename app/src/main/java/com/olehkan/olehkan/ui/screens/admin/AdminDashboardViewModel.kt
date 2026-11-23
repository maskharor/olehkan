package com.olehkan.olehkan.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class AdminDashboardState(
    val loading: Boolean = true,
    val categoryCount: Int? = null,
    val buyerCount: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val importing: Boolean = false,
)

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer.get(application)
    private val _uiState = MutableStateFlow(AdminDashboardState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val result = JSONObject(container.auth.authorized("POST", "/rest/v1/rpc/admin_overview"))
                _uiState.update {
                    it.copy(loading = false, categoryCount = result.getInt("category_count"),
                        buyerCount = result.getInt("buyer_count"))
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(loading = false, categoryCount = null, buyerCount = null,
                    error = if (e is BackendException) e.message else "Ringkasan toko belum dapat dimuat.") }
            }
        }
    }

    fun importLocalCategories() {
        if (_uiState.value.importing) return
        _uiState.update { it.copy(importing = true, message = null, error = null) }
        viewModelScope.launch {
            try {
                val count = container.categories.importLocal()
                _uiState.update { it.copy(message = "$count kategori lokal berhasil diimpor. Data lokal tetap disimpan.") }
                refresh()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(error = if (e is IllegalStateException) e.message else
                    "Impor belum berhasil. Periksa koneksi dan izin admin; data lokal tetap disimpan.") }
            } finally {
                _uiState.update { it.copy(importing = false) }
            }
        }
    }
}
