package com.olehkan.olehkan.ui.screens.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun CategoryListScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: CategoryViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CategoryPage("Kelola Kategori", onBack) {
        Text("Atur kategori produk oleh-oleh.", style = OlehKanType.Body, color = Muted)
        Button(onClick = onAdd, enabled = !state.isLoading) { Text("Tambah Kategori") }
        when {
            state.isLoading -> CategoryLoading()
            state.errorMessage != null -> CategoryError(state.errorMessage!!, viewModel::refresh)
            state.categories.isEmpty() ->
                Text("Belum ada kategori. Tambahkan kategori pertama.", style = OlehKanType.Body)
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.categories, key = { it.id }) { category ->
                    Card(
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(category.name, style = OlehKanType.H3)
                            Text(
                                "${category.productCount} produk • Ketuk untuk detail",
                                style = OlehKanType.BodySmall,
                                color = Muted,
                            )
                        }
                    }
                }
            }
        }
    }
}
