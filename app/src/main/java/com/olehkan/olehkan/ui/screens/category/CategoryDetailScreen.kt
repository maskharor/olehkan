package com.olehkan.olehkan.ui.screens.category

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun CategoryDetailScreen(
    categoryId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: CategoryViewModel,
) {
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
        when {
            state.isLoading -> CategoryLoading()
            state.errorMessage != null && (!state.hasLoaded || category == null) ->
                CategoryError(state.errorMessage!!, viewModel::refresh)
            category == null -> Text("Kategori tidak ditemukan.", style = OlehKanType.Body)
            else -> Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(category.name, style = OlehKanType.H1)
                Text("${category.productCount} produk menggunakan kategori ini.", style = OlehKanType.Body)
                if (category.isProtected || category.productCount > 0) {
                    Text(
                        "Kategori yang masih dipakai produk tidak dapat diganti nama atau dihapus.",
                        style = OlehKanType.BodySmall,
                    )
                }
                state.errorMessage?.let { CategoryError(it) }
                Button(
                    onClick = { onEdit(category.id) },
                    enabled = state.hasLoaded && !state.isSaving && !category.isProtected && category.productCount == 0,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Edit Kategori") }
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    enabled = state.hasLoaded && !state.isSaving && !category.isProtected && category.productCount == 0,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.isSaving) "Menghapus…" else "Hapus Kategori") }
            }
        }
    }

    if (showDeleteConfirmation && category != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Hapus kategori?") },
            text = { Text("Hapus kategori \"${category.name}\"? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.delete(categoryId)
                    },
                    enabled = state.hasLoaded && !state.isSaving && !category.isProtected && category.productCount == 0,
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Batal") }
            },
        )
    }
}
