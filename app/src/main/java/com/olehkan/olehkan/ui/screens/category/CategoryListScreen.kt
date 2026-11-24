package com.olehkan.olehkan.ui.screens.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.theme.*

@Composable
fun CategoryListScreen(onBack: () -> Unit, onAdd: () -> Unit, onCategoryClick: (String) -> Unit, viewModel: CategoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    CategoryPage("Kelola Kategori", onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("category-list"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp),
        ) {
            item { CategoryHero("KATALOG TOKO", "Rapi kategorinya,\nmudah belanjanya.", "Kelompokkan produk oleh-oleh agar lebih mudah ditemukan.") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onAdd, enabled = !state.isLoading, shape = OlehKanShapes.Pill, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah Kategori")
                    }
                    IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, "Muat ulang kategori", tint = Green)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = state.query, onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Cari nama kategori…", style = OlehKanType.BodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                    trailingIcon = { if (state.query.isNotEmpty()) IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Hapus pencarian")
                    } },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Medium,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = White, focusedContainerColor = White,
                        unfocusedBorderColor = Line, focusedBorderColor = Green),
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Semua", "Bisa dikelola", "Dilindungi")) { filter ->
                        FilterChip(selected = state.filter == filter, onClick = { viewModel.onFilterChange(filter) },
                            label = { Text(filter, style = OlehKanType.ChipLabel) },
                            shape = OlehKanShapes.Pill,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Green, selectedLabelColor = White))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${state.visibleCategories.size} dari ${state.categories.size} kategori dimuat",
                        style = OlehKanType.Caption, color = Muted, modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::toggleSort) {
                        Text(if (state.alphabetical) "Urutan: A–Z" else "Urutan: Awal", color = Green)
                    }
                }
            }
            if (state.isLoading) item { CategoryLoading() }
            state.errorMessage?.let { message -> item { CategoryError(message, viewModel::refresh) } }
            if (!state.isLoading && state.errorMessage == null && state.visibleCategories.isEmpty()) {
                item {
                    CategoryNotice(
                        if (state.categories.isEmpty()) "Mulai menata toko" else "Tidak ada hasil",
                        if (state.categories.isEmpty()) "Belum ada kategori. Tambahkan kategori pertama."
                        else "Tidak ada kategori yang cocok dengan pencarian atau filter.",
                    )
                    if (state.categories.isNotEmpty()) TextButton(onClick = viewModel::resetFilters) { Text("Reset filter") }
                }
            }
            items(state.visibleCategories, key = { it.id }) { category ->
                Surface(onClick = { onCategoryClick(category.id) }, color = White, shape = OlehKanShapes.Large,
                    border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.name, style = OlehKanType.SubHeaderTitle, color = Ink, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Buka detail", tint = Green, modifier = Modifier.size(20.dp))
                        }
                        Text(categoryUsage(category), style = OlehKanType.BodySmall, color = Muted)
                        CategoryStatus(category)
                    }
                }
            }
        }
    }
}
