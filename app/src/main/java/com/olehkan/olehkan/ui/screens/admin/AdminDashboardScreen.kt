package com.olehkan.olehkan.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun AdminDashboardScreen(
    profile: UserProfile,
    onCategories: () -> Unit,
    onProfile: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("OlehKan", style = OlehKanType.Hero)
            Text("Dashboard Admin", style = OlehKanType.H2)
            Text("Selamat datang, ${profile.fullName}", style = OlehKanType.Body)
            Text(profile.email, style = OlehKanType.BodySmall)
            TextButton(onClick = onProfile) { Text("Profil & akun") }
        }
        item {
            Text("Ringkasan Toko", style = OlehKanType.H3)
            if (state.loading) CircularProgressIndicator()
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::refresh) { Text("Coba Lagi") }
            }
        }
        state.categoryCount?.let { count ->
            item { OverviewCard("Kategori", count) }
        }
        state.buyerCount?.let { count ->
            item { OverviewCard("Buyer Terdaftar", count) }
        }
        item {
            Text("Manajemen Toko", style = OlehKanType.H2)
            Card(onClick = onCategories, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kelola Kategori", style = OlehKanType.H3)
                    Text("Tambah, lihat, dan kelola kategori produk.", style = OlehKanType.Body)
                }
            }
        }
        item {
            Text("Migrasi kategori perangkat", style = OlehKanType.H3)
            Text("Impor kategori lokal ke toko. Nama yang sudah tersedia dilewati dan data lokal tetap disimpan.",
                style = OlehKanType.Body)
            OutlinedButton(onClick = viewModel::importLocalCategories, enabled = !state.importing) {
                Text(if (state.importing) "Mengimpor…" else "Impor Kategori Lokal")
            }
            state.message?.let { Text(it, style = OlehKanType.Body) }
        }
        item {
            Text("Manajemen produk dan pesanan belum terhubung ke database.",
                style = OlehKanType.BodySmall)
            Button(onClick = onProfile) { Text("Pengaturan Akun") }
        }
    }
}

@Composable
private fun OverviewCard(label: String, count: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(count.toString(), style = OlehKanType.Hero)
            Text(label, style = OlehKanType.Body)
        }
    }
}
