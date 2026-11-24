package com.olehkan.olehkan.ui.screens.category

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.CategoryNameValidator
import com.olehkan.olehkan.ui.theme.*

@Composable
fun CategoryFormScreen(categoryId: String?, onBack: () -> Unit, onSaved: () -> Unit, viewModel: CategoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val category = state.categories.find { it.id == categoryId }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val normalized = CategoryNameValidator.normalize(name)
    val dirty = state.hasLoaded && normalized != category?.name.orEmpty()
    val goBack: () -> Unit = {
        if (!state.isSaving) {
            if (dirty) confirmDiscard = true else onBack()
        }
    }
    BackHandler(enabled = dirty || state.isSaving) { goBack() }
    LaunchedEffect(state.isLoading, categoryId) {
        if (state.hasLoaded && !state.isLoading) viewModel.prepareForm(categoryId)
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSuccess()
            onSaved()
        }
    }
    CategoryPage(if (categoryId == null) "Tambah Kategori" else "Edit Kategori", goBack) {
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when {
                state.isLoading -> CategoryLoading()
                state.errorMessage != null && (!state.hasLoaded || (categoryId != null && category == null)) ->
                    CategoryError(state.errorMessage!!, viewModel::refresh)
                categoryId != null && category == null -> CategoryNotice("Data tidak tersedia", "Kategori tidak ditemukan.")
                category != null && !category.canManage -> {
                    CategoryNotice("Kategori belum dapat diedit", "Kategori dilindungi, digunakan produk, atau relasinya belum dapat diperiksa.")
                    TextButton(onClick = viewModel::refresh) { Text("Muat ulang", color = Green) }
                }
                else -> {
                    CategoryHero("PENGATURAN KATALOG", if (categoryId == null) "Kategori baru,\ncerita baru." else "Perbarui kategori.",
                        "Gunakan nama yang singkat dan mudah dikenali pembeli.")
                    Text("INFORMASI KATEGORI", style = OlehKanType.Eyebrow, color = Orange)
                    OutlinedTextField(
                        value = name, onValueChange = viewModel::onNameChange,
                        label = { Text("Nama kategori") }, singleLine = true,
                        enabled = !state.isSaving, isError = state.nameError != null,
                        supportingText = {
                            Column {
                                Text(state.nameError ?: "Nama harus unik; Semua dipakai untuk filter.")
                                Text("${normalized.length}/${CategoryNameValidator.MAX_LENGTH} karakter")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Medium,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = White, focusedContainerColor = White,
                            unfocusedBorderColor = Line, focusedBorderColor = Green),
                    )
                    CategoryNotice("Pratinjau nama", normalized.ifBlank { "Nama kategori akan muncul di sini." })
                    state.errorMessage?.let { CategoryError(it) }
                    Button(onClick = { focusManager.clearFocus(); viewModel.save(categoryId) },
                        enabled = !state.isSaving && state.hasLoaded && (categoryId == null || dirty),
                        modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Pill) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (state.isSaving) "Menyimpan…" else "Simpan Kategori")
                    }
                    Text("Perubahan disimpan ke toko setelah berhasil dikonfirmasi server.", style = OlehKanType.Caption, color = Muted)
                }
            }
        }
    }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text("Buang perubahan?") },
        text = { Text("Nama yang belum disimpan akan hilang jika keluar dari form.") },
        confirmButton = { TextButton(onClick = { confirmDiscard = false; onBack() }) { Text("Buang perubahan") } },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Lanjut edit") } },
    )
}
