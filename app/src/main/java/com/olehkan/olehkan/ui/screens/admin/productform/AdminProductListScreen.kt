package com.olehkan.olehkan.ui.screens.admin.productform

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.formattedPrice
import com.olehkan.olehkan.ui.theme.CompleteBg
import com.olehkan.olehkan.ui.theme.CompleteText
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.WarningBg
import com.olehkan.olehkan.ui.theme.WarningText
import com.olehkan.olehkan.ui.theme.White

/**
 * Halaman "Toko Saya" / Kelola Produk (Fitur 2, MVVM).
 *
 * Tampilan mengikuti mockup merchant yang diberikan:
 * kartu profil toko, statistik bento 3 kolom, lalu
 * "Kategori & Stok Etalase" sebagai accordion per kategori,
 * dengan baris produk (foto, nama, harga, chip stok Aman/Menipis)
 * dan tombol edit cepat.
 *
 * Data yang tampil adalah data asli dari Supabase — bukan contoh
 * di mockup (nama toko, angka penjualan, dan rating contoh
 * tidak dipakai). Stok "Menipis" = stok <= 10, selain itu "Aman".
 */
@Composable
fun AdminProductListScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onManageCategories: () -> Unit = {},
    viewModel: AdminProductListViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var expandedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    val products = state.products
    val grouped = remember(products) { products.groupBy { it.category.ifBlank { "Tanpa Kategori" } } }
    // Buka kategori pertama secara default, seperti mockup.
    LaunchedEffect(grouped.keys) {
        if (expandedCategories.isEmpty() && grouped.isNotEmpty()) {
            expandedCategories = setOf(grouped.keys.first())
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSuccess()
        }
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus produk?") },
            text = { Text("Produk \"${product.name}\" akan dihapus. Produk yang sudah dipakai transaksi sebaiknya dinonaktifkan saja, bukan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(product.uuid)
                        productToDelete = null
                    },
                    enabled = !state.isDeleting,
                ) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("Batal") }
            },
        )
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text("Toko Saya", style = OlehKanType.SubHeaderTitle, color = Ink)
        }
        when {
            state.isLoading -> {
                Column(
                    Modifier.fillMaxWidth().padding(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Memuat produk…", style = OlehKanType.Body)
                }
            }
            state.errorMessage != null -> {
                Column(
                    Modifier.fillMaxWidth().padding(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error, style = OlehKanType.Body)
                    Button(onClick = viewModel::loadProducts) { Text("Coba Lagi") }
                }
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // --- Kartu profil toko ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(16.dp)),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape).background(Green),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("O", style = OlehKanType.Hero, color = White)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "OlehKan",
                                        style = OlehKanType.H2,
                                        color = Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Icon(Icons.Filled.Verified, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                                }
                                Text("Oleh-Oleh Khas • Toko Saya", style = OlehKanType.BodySmall, color = Muted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StockChip("Toko Buka", CompleteBg, CompleteText)
                                    if (products.any { !it.isActive }) {
                                        StockChip("${products.count { !it.isActive }} nonaktif", WarningBg, WarningText)
                                    }
                                }
                            }
                        }
                    }
                }
                // --- Statistik bento: 3 kolom ---
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            icon = Icons.Filled.ShoppingBag,
                            label = "Total SKU",
                            value = "${products.size} Produk",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon = Icons.Filled.Inventory,
                            label = "Total Stok",
                            value = "${products.sumOf { it.stock }} item",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon = Icons.Filled.Category,
                            label = "Kategori",
                            value = "${grouped.size} Kategori",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                // --- Judul seksi Kategori & Stok ---
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Layers, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                            Text("Kategori & Stok Etalase", style = OlehKanType.H3, color = Ink)
                        }
                        TextButton(onClick = onManageCategories) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                            Text("Atur", color = Green)
                        }
                    }
                }
                if (products.isEmpty()) {
                    item {
                        Text("Belum ada produk. Tambahkan produk pertama.", style = OlehKanType.Body)
                    }
                }
                // --- Accordion per kategori ---
                items(grouped.entries.toList(), key = { it.key }) { (categoryName, categoryProducts) ->
                    val expanded = categoryName in expandedCategories
                    val totalStock = categoryProducts.sumOf { it.stock }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(16.dp)),
                    ) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    expandedCategories = if (expanded) expandedCategories - categoryName
                                    else expandedCategories + categoryName
                                }.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(GreenLight),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Category, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(categoryName, style = OlehKanType.H3, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        "${categoryProducts.count { it.isActive }} varian aktif",
                                        style = OlehKanType.BodySmall, color = Muted,
                                    )
                                }
                                StockChip("$totalStock tersisa", CompleteBg, CompleteText)
                                Icon(
                                    Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = Muted,
                                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                                )
                            }
                            if (expanded) {
                                Column(
                                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    categoryProducts.forEach { product ->
                                        ProductStockRow(
                                            product = product,
                                            onEdit = { onEdit(product.uuid) },
                                            onDelete = { productToDelete = product },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // --- Tombol tambah / ubah kelompok barang ---
                item {
                    OutlinedButton(
                        onClick = onManageCategories,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                        Text("Tambah / Ubah Kelompok Barang", color = Green)
                    }
                }
                item {
                    Button(
                        onClick = onAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Tambah Produk")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockChip(text: String, background: Color, foreground: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(999.dp)).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = OlehKanType.Caption, color = foreground)
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, Line, RoundedCornerShape(16.dp)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(GreenLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, style = OlehKanType.Caption, color = Muted)
                Text(value, style = OlehKanType.H3, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ProductStockRow(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Paper).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(product.name, style = OlehKanType.Body, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.formattedPrice(), style = OlehKanType.BodySmall, color = Muted)
            if (!product.isActive) {
                Text("Nonaktif", style = OlehKanType.Caption, color = WarningText)
            }
        }
        if (product.stock <= 10) {
            StockChip("Menipis (${product.stock})", WarningBg, WarningText)
        } else {
            StockChip("Aman (${product.stock})", CompleteBg, CompleteText)
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit ${product.name}", tint = Muted, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Hapus ${product.name}", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
    }
}
