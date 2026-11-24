package com.olehkan.olehkan.ui.screens.admin

import androidx.compose.foundation.BorderStroke
 import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.olehkan.olehkan.R
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.data.repository.AdminInventoryProduct
import com.olehkan.olehkan.ui.theme.*
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    profile: UserProfile,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel(),
    onCategoryClick: (String) -> Unit = { onCategories() },
    onAddCategory: () -> Unit = onCategories,
    onProducts: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    AdminDashboardContent(
        profile, state, onCategories, onProfile, onCategoryClick,
        viewModel::refresh, viewModel::onQueryChange, viewModel::toggleTools,
        viewModel::requestImport, viewModel::dismissImport, viewModel::importLocalCategories,
        onAddCategory, viewModel::toggleCategory, viewModel::loadProducts, onProducts,
    )
}

@Composable
internal fun AdminDashboardContent(
    profile: UserProfile,
    state: AdminDashboardState,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleTools: () -> Unit,
    onRequestImport: () -> Unit,
    onDismissImport: () -> Unit,
    onConfirmImport: () -> Unit,
    onAddCategory: () -> Unit = onCategories,
    onToggleCategory: (String) -> Unit = onCategoryClick,
    onLoadProducts: (String, Boolean) -> Unit = { _, _ -> },
    onProducts: () -> Unit = {},
) {
    val busy = state.loading || state.categoriesLoading || state.importing
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_olehkan_icon),
                    contentDescription = "Logo OlehKan",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(42.dp).clip(OlehKanShapes.Small),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("OlehKan", style = OlehKanType.SubHeaderTitle, color = Green)
                    Text("Toko saya", style = OlehKanType.Caption, color = Muted)
                }
                IconButton(onClick = onRefresh, enabled = !busy) {
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Green, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, "Perbarui dashboard", tint = Green)
                }
                IconButton(onClick = onProfile) {
                    Icon(Icons.Default.AccountCircle, "Buka profil admin", tint = Green, modifier = Modifier.size(30.dp))
                }
            }
        }
        item {
            Surface(shape = OlehKanShapes.Large, color = GreenLight, border = BorderStroke(1.dp, Line)) {
                Row(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenLight, Cream))).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(color = Green, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(profile.fullName.trim().take(1).uppercase(Locale.ROOT),
                                style = OlehKanType.H2, color = White)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("OlehKan", style = OlehKanType.SubHeaderTitle, color = Ink)
                        Text("Dikelola oleh ${profile.fullName}", style = OlehKanType.BodySmall, color = Muted)
                        Text(profile.email, style = OlehKanType.Caption, color = Muted)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StockBadge("ADMIN", false)
                            Text("Kelola toko & stok persediaan", style = OlehKanType.Caption, color = Green)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardMetric("Kategori", state.categoryCount, state.loading, Icons.Default.Category, Modifier.weight(1f))
                DashboardMetric("Buyer", state.buyerCount, state.loading, Icons.Default.People, Modifier.weight(1f))
                DashboardMetric("Dilindungi",
                    if (state.categoriesError == null) state.categories.count { it.isProtected }.toLong() else null,
                    state.categoriesLoading, Icons.Default.Lock, Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Text("Ringkasan toko • Dilindungi: dari kategori yang dimuat", style = OlehKanType.Caption, color = Muted)
            state.updatedAt?.let {
                Text("Diperbarui ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))}",
                    style = OlehKanType.Caption, color = Muted)
            }
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                DashboardError(it, onRefresh, !busy)
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, null, tint = Green, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Kategori & stok etalase", style = OlehKanType.SubHeaderTitle, color = Ink, modifier = Modifier.weight(1f))
                TextButton(onClick = onCategories) { Text("Atur", style = OlehKanType.BodyBold, color = Green) }
            }
            Text("Buka kategori untuk melihat produk dan stok sesuai akses akun.",
                style = OlehKanType.Caption, color = Muted)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.query, onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Cari kategori toko…", style = OlehKanType.BodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Hapus pencarian")
                    }
                },
                shape = OlehKanShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = White,
                    focusedContainerColor = White, unfocusedBorderColor = Line, focusedBorderColor = Green),
            )
            if (state.categoriesLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Green)
            state.categoriesError?.let { DashboardError(it, onRefresh, !busy) }
            if (state.categoriesError != null && state.categories.isNotEmpty())
                Text("Daftar terakhir yang berhasil dimuat.", style = OlehKanType.Caption, color = Muted)
        }
        if (!state.categoriesLoading && state.categoriesError == null && state.visibleCategories.isEmpty()) {
            item {
                Surface(color = GreenLight, shape = OlehKanShapes.Medium) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (state.query.isBlank()) "Kategori pertamamu dimulai di sini" else "Kategori tidak ditemukan",
                            style = OlehKanType.H3, color = Ink)
                        Text(if (state.query.isBlank()) "Tambahkan kategori untuk menata produk toko."
                            else "Coba nama lain atau kosongkan pencarian.", style = OlehKanType.BodySmall, color = Muted)
                        if (state.query.isNotBlank()) TextButton(onClick = { onQueryChange("") }) {
                            Text("Reset pencarian", color = Green)
                        }
                    }
                }
            }
        }
        state.visibleCategories.forEach { category ->
            val expanded = state.expandedCategoryId == category.id
            val inventory = state.inventory[category.id] ?: CategoryInventoryState()
            item(key = "category-${category.id}") {
                Surface(onClick = { onToggleCategory(category.id) }, color = White,
                    shape = OlehKanShapes.Large, border = BorderStroke(1.dp, if (expanded) Green else Line),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(color = GreenLight, shape = CircleShape) {
                            Icon(categoryFoodIcon(category.name), null, tint = Green, modifier = Modifier.padding(10.dp).size(20.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(category.name, style = OlehKanType.H3, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(if (category.usageKnown) "${category.productCount} produk dapat diakses"
                                else "Ketuk untuk melihat produk", style = OlehKanType.Caption, color = Muted)
                        }
                        if (category.isProtected) Icon(Icons.Default.Lock, "Kategori dilindungi",
                            tint = Muted, modifier = Modifier.size(14.dp))
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            if (expanded) "Tutup kategori" else "Buka kategori", tint = Green)
                    }
                }
            }
            if (expanded) {
                item(key = "inventory-info-${category.id}") {
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Produk & stok", style = OlehKanType.BodyBold, color = Green, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onCategoryClick(category.id) }) { Text("Detail kategori", color = Green) }
                    }
                    if (inventory.products.isNotEmpty()) Text(
                        "${inventory.products.size} produk ditampilkan • ${inventory.products.sumOf { it.stock.toLong() }} unit stok ditampilkan",
                        style = OlehKanType.Caption, color = Muted)
                    if (inventory.loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Green)
                    inventory.error?.let { message ->
                        DashboardError(message, { onLoadProducts(category.id, inventory.loaded && inventory.hasMore) }, !inventory.loading)
                    }
                    if (inventory.loaded && !inventory.loading && inventory.products.isEmpty() && inventory.error == null)
                        Text("Belum ada produk yang dapat diakses pada kategori ini.",
                            Modifier.padding(12.dp), style = OlehKanType.BodySmall, color = Muted)
                }
                items(inventory.products, key = { "product-${category.id}-${it.id}" }) { product ->
                    InventoryProductRow(product)
                }
                if (inventory.hasMore && inventory.error == null) item(key = "more-${category.id}") {
                    TextButton(onClick = { onLoadProducts(category.id, true) }, enabled = !inventory.loading,
                        modifier = Modifier.fillMaxWidth()) { Text("Muat lebih banyak", color = Green) }
                }
            }
        }
        item {
            OutlinedButton(onClick = onAddCategory, modifier = Modifier.fillMaxWidth(),
                shape = OlehKanShapes.Medium, border = BorderStroke(1.dp, Green),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = GreenLight, contentColor = Green)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tambah kategori")
            }
        }
        item {
            Button(onClick = onProducts, modifier = Modifier.fillMaxWidth(),
                shape = OlehKanShapes.Medium,
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White)) {
                Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Kelola Produk")
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalDivider(color = Line)
                TextButton(onClick = onToggleTools, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Impor kategori perangkat", Modifier.weight(1f), style = OlehKanType.BodyBold)
                    Icon(if (state.toolsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (state.toolsExpanded) {
                    Text("Salin kategori lokal lama ke toko. Nama yang sudah ada dilewati dan data asli tetap disimpan.",
                        style = OlehKanType.BodySmall, color = Muted)
                    OutlinedButton(onClick = onRequestImport, enabled = !busy, shape = OlehKanShapes.Pill) {
                        Text(if (state.importing) "Mengimpor…" else "Impor kategori lokal")
                    }
                }
                state.message?.let { Text(it, style = OlehKanType.BodySmall, color = Green) }
                state.importError?.let { Text(it, style = OlehKanType.BodySmall, color = HeartRed) }
                Text("OlehKan • Satu toko, beragam cerita", style = OlehKanType.Caption, color = Muted)
            }
        }
    }
    if (state.confirmImport) AlertDialog(
        onDismissRequest = onDismissImport,
        title = { Text("Impor kategori lokal?") },
        text = { Text("Kategori yang belum tersedia akan ditambahkan ke toko dan dapat dilihat pengguna lain. Data lokal tidak dihapus. Jika proses terputus, impor dapat dilanjutkan dengan mencoba kembali.") },
        confirmButton = { TextButton(onClick = onConfirmImport, enabled = !busy) { Text("Impor") } },
        dismissButton = { TextButton(onClick = onDismissImport) { Text("Batal") } },
    )
}

private fun categoryFoodIcon(name: String): ImageVector {
    val normalized = name.trim().lowercase(Locale.ROOT)
    return when {
        "asinan" in normalized || "acar" in normalized -> Icons.Default.SoupKitchen
        "minuman" in normalized || "kopi" in normalized || "teh" in normalized -> Icons.Default.LocalCafe
        "manisan" in normalized || "dodol" in normalized || "kue" in normalized -> Icons.Default.Cake
        "camilan" in normalized || "cemilan" in normalized || "keripik" in normalized ||
            "snack" in normalized || "biskuit" in normalized -> Icons.Default.Cookie
        else -> Icons.Default.Restaurant
    }
}

@Composable
private fun InventoryProductRow(product: AdminInventoryProduct) {
    Surface(color = GreenLight.copy(alpha = 0.6f), shape = OlehKanShapes.Medium,
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SubcomposeAsyncImage(
                    model = product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(OlehKanShapes.Small),
                    loading = { ProductImagePlaceholder() }, error = { ProductImagePlaceholder() },
                )
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = OlehKanType.H3, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                        maximumFractionDigits = 2; minimumFractionDigits = 0
                    }.format(product.price), style = OlehKanType.BodySmall, color = Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StockBadge(if (product.stock == 0) "Stok habis" else "Tersedia ${product.stock} unit", product.stock == 0)
                if (!product.active) Text("Nonaktif", style = OlehKanType.CaptionBold, color = Muted)
            }
        }
    }
}

@Composable
private fun ProductImagePlaceholder() {
    Box(Modifier.fillMaxSize().background(Cream), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Inventory2, null, tint = Green, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun StockBadge(text: String, warning: Boolean) {
    Surface(color = if (warning) WarningBg else GreenLight, shape = OlehKanShapes.Pill) {
        Text(text, Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = OlehKanType.CaptionBold, color = if (warning) WarningText else Green)
    }
}

@Composable
private fun DashboardMetric(label: String, value: Long?, loading: Boolean, icon: ImageVector, modifier: Modifier) {
    Surface(modifier, color = White, shape = OlehKanShapes.Large, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Green, modifier = Modifier.size(21.dp))
            Text(label, style = OlehKanType.CaptionBold, color = Muted)
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Green, strokeWidth = 2.dp)
            else Text(value?.toString() ?: "—", style = OlehKanType.SubHeaderTitle, color = Ink)
        }
    }
}

@Composable
private fun DashboardError(message: String, onRetry: () -> Unit, enabled: Boolean) {
    Surface(color = WarningBg, shape = OlehKanShapes.Medium) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(message, style = OlehKanType.BodySmall, color = WarningText)
            TextButton(onClick = onRetry, enabled = enabled) { Text("Coba lagi", color = Green) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardPreview() {
    OlehKanTheme {
        AdminDashboardContent(
            UserProfile("preview", "Admin OlehKan", "admin@example.test", UserRole.ADMIN),
            AdminDashboardState(loading = false, categoryCount = 2, buyerCount = 12, categoriesLoading = false,
                categories = listOf(Category("1", "Makanan", 4, isProtected = true), Category("2", "Minuman", 2, isProtected = false)),
                expandedCategoryId = "2",
                inventory = mapOf("2" to CategoryInventoryState(loaded = true, products = listOf(
                    AdminInventoryProduct("preview-1", "Wedang Jahe", java.math.BigDecimal("15000"), 12, null, true),
                    AdminInventoryProduct("preview-2", "Kopi Susu", java.math.BigDecimal("18000"), 0, null, false),
                )))),
            {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}
