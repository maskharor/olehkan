package com.olehkan.olehkan.ui.screens.category

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.theme.*

@Composable
fun CategoryDetailScreen(categoryId: String, onBack: () -> Unit, onEdit: (String) -> Unit,
    onDeleted: () -> Unit, viewModel: CategoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val category = state.categories.find { it.id == categoryId }
    var showDeleteConfirmation by rememberSaveable(categoryId) { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSuccess()
            onDeleted()
        }
    }
    CategoryPage("Detail Kategori", onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when {
                state.isLoading -> CategoryLoading()
                state.errorMessage != null && (!state.hasLoaded || category == null) ->
                    CategoryError(state.errorMessage!!, viewModel::refresh)
                category == null -> CategoryNotice("Data tidak tersedia", "Kategori tidak ditemukan.")
                else -> {
                    CategoryHero("DETAIL KATEGORI", category.name, "Informasi dan pengaturan kategori toko.")
                    CategoryStatus(category)
                    CategoryNotice("Relasi produk", categoryUsage(category) +
                        ". Hitungan mengikuti hak akses akun, termasuk produk nonaktif yang dapat dibaca.")
                    Text("ID kategori", style = OlehKanType.Eyebrow, color = Orange)
                    Text(category.id, style = OlehKanType.BodySmall, color = Muted)
                    if (!category.canManage) CategoryNotice("Pengaturan dibatasi", when {
                        category.isProtected -> "Kategori ini dilindungi oleh pengaturan toko."
                        !category.usageKnown -> "Relasi produk belum dapat diperiksa. Muat ulang agar edit dan hapus dapat dipertimbangkan."
                        else -> "Kategori masih digunakan produk. Pindahkan produk ke kategori lain sebelum mengganti nama atau menghapusnya."
                    })
                    state.errorMessage?.let { CategoryError(it) }
                    Button(onClick = { onEdit(category.id) }, enabled = state.hasLoaded && !state.isSaving && category.canManage,
                        modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Pill) { Text("Edit Kategori") }
                    OutlinedButton(onClick = { showDeleteConfirmation = true },
                        enabled = state.hasLoaded && !state.isSaving && category.canManage,
                        modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Pill,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeartRed)) {
                        Text(if (state.isSaving) "Menghapus…" else "Hapus Kategori")
                    }
                    TextButton(onClick = viewModel::refresh, enabled = !state.isSaving) { Text("Muat ulang detail", color = Green) }
                }
            }
        }
    }
    if (showDeleteConfirmation && category != null) AlertDialog(
        onDismissRequest = { showDeleteConfirmation = false },
        title = { Text("Hapus kategori?") },
        text = { Text("Hapus kategori \"${category.name}\"? Tindakan ini tidak dapat dibatalkan. Kategori yang masih direferensikan produk tidak boleh dihapus.") },
        confirmButton = { TextButton(onClick = {
            showDeleteConfirmation = false
            viewModel.delete(categoryId)
        }, enabled = state.hasLoaded && !state.isSaving && !state.isLoading && category.canManage) { Text("Hapus", color = HeartRed) } },
        dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Batal") } },
    )
}
