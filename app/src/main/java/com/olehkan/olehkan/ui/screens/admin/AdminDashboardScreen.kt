package com.olehkan.olehkan.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.theme.*
import java.text.DateFormat
import java.util.Date

@Composable
fun AdminDashboardScreen(
    profile: UserProfile,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel(),
    onCategoryClick: (String) -> Unit = { onCategories() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    AdminDashboardContent(
        profile, state, onCategories, onProfile, onCategoryClick,
        viewModel::refresh, viewModel::onQueryChange, viewModel::toggleTools,
        viewModel::requestImport, viewModel::dismissImport, viewModel::importLocalCategories,
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
) {
    val busy = state.loading || state.categoriesLoading || state.importing
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Surface(
                shape = OlehKanShapes.HeroBottom,
                color = GreenDark,
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(Brush.linearGradient(listOf(GreenDark, Green)))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, null, tint = White, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("OlehKan", style = OlehKanType.SubHeaderTitle, color = White, modifier = Modifier.weight(1f))
                        Surface(color = White.copy(alpha = 0.13f), shape = OlehKanShapes.Pill) {
                            Text("ADMIN", Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = OlehKanType.CaptionBold, color = White)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onProfile) {
                            Icon(Icons.Default.AccountCircle, "Buka profil admin", tint = White)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Selamat datang, ${profile.fullName}", style = OlehKanType.Body, color = White.copy(alpha = 0.8f))
                        Text("Kelola toko,\nsatukan cerita.", style = OlehKanType.Hero, color = White)
                        Text("Pusat pengelolaan toko oleh-olehmu.", style = OlehKanType.BodySmall, color = White.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("DASHBOARD ADMIN", style = OlehKanType.Eyebrow, color = White)
                            Text(
                                state.updatedAt?.let { "Ringkasan diperbarui ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))}" }
                                    ?: "Muat data terbaru dari toko",
                                style = OlehKanType.Caption, color = White.copy(alpha = 0.8f),
                            )
                        }
                        IconButton(onClick = onRefresh, enabled = !busy) {
                            if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, "Perbarui dashboard", tint = White)
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardSectionTitle("SEKILAS TOKO", "Ringkasan toko")
                Text("Total data toko saat ini, bukan penambahan harian.", style = OlehKanType.Caption, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardMetric("Kategori", state.categoryCount, state.loading, Icons.Default.Category, Modifier.weight(1f))
                    DashboardMetric("Buyer terdaftar", state.buyerCount, state.loading, Icons.Default.People, Modifier.weight(1f))
                }
                state.error?.let { DashboardError(it, onRefresh, !busy) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardSectionTitle("AKSES CEPAT", "Manajemen toko")
                DashboardAction("Kelola kategori", "Tambah dan atur kategori produk", Icons.Default.Category, onCategories)
                DashboardAction("Profil & akun", "Perbarui nama atau keluar dari akun", Icons.Default.ManageAccounts, onProfile)
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { DashboardSectionTitle("KATALOG TOKO", "Jelajahi kategori") }
                    TextButton(onClick = onCategories) { Text("Lihat semua", color = Green) }
                }
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = White, focusedContainerColor = White,
                        unfocusedBorderColor = Line, focusedBorderColor = Green,
                    ),
                )
                if (state.categoriesLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Green)
                state.categoriesError?.let { DashboardError(it, onRefresh, !busy) }
                if (state.categoriesError != null && state.categories.isNotEmpty())
                    Text("Daftar terakhir yang berhasil dimuat.", style = OlehKanType.Caption, color = Muted)
                if (!state.categoriesLoading && state.categoriesError == null && state.visibleCategories.isEmpty()) {
                    Surface(color = GreenLight, shape = OlehKanShapes.Medium) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (state.query.isBlank()) "Kategori pertamamu dimulai di sini" else "Kategori tidak ditemukan",
                                style = OlehKanType.H3, color = Ink)
                            Text(if (state.query.isBlank()) "Tambahkan kategori untuk menata produk toko."
                                else "Coba nama lain atau kosongkan pencarian.", style = OlehKanType.BodySmall, color = Muted)
                            TextButton(onClick = if (state.query.isBlank()) onCategories else ({ onQueryChange("") })) {
                                Text(if (state.query.isBlank()) "Kelola kategori" else "Reset pencarian", color = Green)
                            }
                        }
                    }
                }
                if (state.visibleCategories.isNotEmpty())
                    Text("Menampilkan ${state.visibleCategories.size} dari ${state.categories.size} kategori yang dimuat",
                        style = OlehKanType.Caption, color = Muted)
            }
        }
        items(state.visibleCategories, key = { it.id }) { category ->
            Surface(
                onClick = { onCategoryClick(category.id) }, color = White,
                shape = OlehKanShapes.Medium, border = BorderStroke(1.dp, Line),
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = GreenLight, shape = OlehKanShapes.Small) {
                        Icon(Icons.AutoMirrored.Filled.Label, null, tint = Green, modifier = Modifier.padding(12.dp).size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(category.name, style = OlehKanType.H3, color = Ink)
                        Text(if (category.isProtected) "Kategori dilindungi" else "Lihat detail kategori",
                            style = OlehKanType.Caption, color = Muted)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Buka detail", tint = Green, modifier = Modifier.size(20.dp))
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HorizontalDivider(color = Line)
                TextButton(onClick = onToggleTools, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Impor kategori perangkat", Modifier.weight(1f), style = OlehKanType.BodyBold)
                    Icon(if (state.toolsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
                if (state.toolsExpanded) {
                    Text("Pindahkan kategori lokal lama ke database toko. Nama yang sudah ada dilewati; data asli tetap disimpan.",
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

@Composable
private fun DashboardSectionTitle(eyebrow: String, title: String) {
    Text(eyebrow, style = OlehKanType.Eyebrow, color = Orange)
    Spacer(Modifier.height(5.dp))
    Text(title, style = OlehKanType.H2, color = Ink)
}

@Composable
private fun DashboardMetric(label: String, value: Long?, loading: Boolean, icon: ImageVector, modifier: Modifier) {
    Surface(modifier, color = White, shape = OlehKanShapes.Large, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = Green, modifier = Modifier.size(24.dp))
            if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Green, strokeWidth = 2.dp)
            else Text(value?.toString() ?: "—", style = OlehKanType.Hero, color = Ink)
            Text(label, style = OlehKanType.BodySmall, color = Muted)
        }
    }
}

@Composable
private fun DashboardAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick, color = GreenLight, shape = OlehKanShapes.Large, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = Green, modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = OlehKanType.H3, color = Ink)
                Text(subtitle, style = OlehKanType.Caption, color = Green)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Green, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DashboardError(message: String, onRetry: () -> Unit, enabled: Boolean) {
    Surface(color = WarningBg, shape = OlehKanShapes.Medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
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
            AdminDashboardState(loading = false, categoryCount = 3, buyerCount = 12, categoriesLoading = false,
                categories = listOf(Category("1", "Makanan", isProtected = true), Category("2", "Minuman"))),
            {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}
