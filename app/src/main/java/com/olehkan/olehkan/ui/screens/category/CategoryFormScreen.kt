package com.olehkan.olehkan.ui.screens.category

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.CategoryNameValidator
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun CategoryFormScreen(
    categoryId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CategoryViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val category = state.categories.find { it.id == categoryId }

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

    CategoryPage(if (categoryId == null) "Tambah Kategori" else "Edit Kategori", onBack) {
        when {
            state.isLoading -> CategoryLoading()
            state.errorMessage != null && (!state.hasLoaded || (categoryId != null && category == null)) ->
                CategoryError(state.errorMessage!!, viewModel::refresh)
            categoryId != null && category == null ->
                Text("Kategori tidak ditemukan.", style = OlehKanType.Body)
            category != null && (category.isProtected || category.productCount > 0) ->
                Text("Kategori ini masih dipakai produk dan tidak dapat diganti namanya.", style = OlehKanType.Body)
            else -> Column(
                Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nama kategori") },
                    singleLine = true,
                    enabled = !state.isSaving,
                    isError = state.nameError != null,
                    supportingText = {
                        Text(state.nameError ?: "Maksimal ${CategoryNameValidator.MAX_LENGTH} karakter.")
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                state.errorMessage?.let { CategoryError(it) }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.save(categoryId)
                    },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.isSaving) "Menyimpan…" else "Simpan Kategori") }
            }
        }
    }
}
