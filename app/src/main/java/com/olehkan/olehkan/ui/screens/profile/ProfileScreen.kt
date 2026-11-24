package com.olehkan.olehkan.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AccountUiState
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onAdminClick: () -> Unit,
    onReviewsClick: () -> Unit,
    onCategoriesClick: () -> Unit = {},
    viewModel: AuthViewModel,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = session.profile ?: return
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ProfileContent(profile, state, onAdminClick, onReviewsClick, onCategoriesClick,
        onEditClick, onBack, viewModel::signOut, onCopyEmail = {
            val message = try {
                context.getSystemService(ClipboardManager::class.java)
                    .setPrimaryClip(ClipData.newPlainText("Email OlehKan", profile.email))
                "Email disalin."
            } catch (_: Exception) { "Email belum dapat disalin." }
            scope.launch { snackbar.showSnackbar(message) }
        }, snackbar = snackbar)
}

@Composable
internal fun ProfileContent(
    profile: UserProfile,
    state: AccountUiState,
    onAdminClick: () -> Unit,
    onReviewsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onEditClick: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onCopyEmail: () -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    var confirmLogout by rememberSaveable(profile.id) { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize()) {
            ProfileTopBar("Profil Akun", onBack, !state.busy)
            LazyColumn(modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    ProfileIdentity(profile)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onEditClick, enabled = !state.busy, shape = OlehKanShapes.Pill,
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profil")
                    }
                }
                item {
                    ProfileSection("INFORMASI AKUN")
                    Spacer(Modifier.height(10.dp))
                    Surface(color = White, border = BorderStroke(1.dp, Line), shape = OlehKanShapes.Large) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Nama lengkap", style = OlehKanType.Caption, color = Muted)
                            Text(profile.fullName, style = OlehKanType.H3, color = Ink)
                            HorizontalDivider(color = Line)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Email akun", style = OlehKanType.Caption, color = Muted)
                                    Text(profile.email, style = OlehKanType.BodySmall, color = Ink)
                                }
                                IconButton(onClick = onCopyEmail, enabled = !state.busy && profile.email.isNotBlank()) {
                                    Icon(Icons.Default.ContentCopy, "Salin email", tint = Green, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
                item { ProfileSection(if (profile.role == UserRole.ADMIN) "KELOLA TOKO" else "AKTIVITAS & TOKO") }
                if (profile.role == UserRole.ADMIN) {
                    item { ProfileAction("Dashboard Admin", "Lihat ringkasan dan etalase toko", Icons.Default.SpaceDashboard, !state.busy, onAdminClick) }
                    item { ProfileAction("Kelola Kategori", "Atur pengelompokan produk toko", Icons.Default.Category, !state.busy, onCategoriesClick) }
                } else {
                    item { ProfileAction("Ulasan Saya", "Buka halaman ulasan", Icons.Default.RateReview, !state.busy, onReviewsClick) }
                    item { ProfileAction("Tentang Toko", "Kenali toko OlehKan", Icons.Default.Storefront, !state.busy, onAdminClick) }
                }
                item {
                    ProfileNotice("Akses akun", if (profile.role == UserRole.ADMIN)
                        "Akun admin digunakan untuk mengelola toko. Role akun ditetapkan oleh pengelola backend."
                        else "Akun buyer digunakan untuk berbelanja. Role akun tidak dapat diubah melalui profil.")
                }
                state.error?.let { message -> item { ProfileNotice("Belum berhasil", message, error = true) } }
                item {
                    OutlinedButton(onClick = { confirmLogout = true }, enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Pill,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeartRed)) {
                        if (state.busy) CircularProgressIndicator(Modifier.size(18.dp), color = HeartRed, strokeWidth = 2.dp)
                        else Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.busy) "Memproses…" else "Logout")
                    }
                    Text("OlehKan • Satu toko, beragam cerita", Modifier.padding(top = 16.dp),
                        style = OlehKanType.Caption, color = Muted)
                }
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
    if (confirmLogout) AlertDialog(
        onDismissRequest = { confirmLogout = false },
        title = { Text("Keluar dari akun?") },
        text = { Text("Sesi di perangkat ini akan dihapus. Kamu perlu login kembali untuk menggunakan akun.") },
        confirmButton = { TextButton(onClick = { confirmLogout = false; onLogout() }, enabled = !state.busy) { Text("Keluar", color = HeartRed) } },
        dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Batal") } },
    )
}

@Preview(showBackground = true)
@Composable
private fun AdminProfilePreview() {
    OlehKanTheme {
        ProfileContent(UserProfile("preview", "Admin OlehKan", "admin@example.test", UserRole.ADMIN),
            AccountUiState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyerProfilePreview() {
    OlehKanTheme {
        ProfileContent(UserProfile("preview", "Pembeli OlehKan", "buyer@example.test", UserRole.BUYER),
            AccountUiState(), {}, {}, {}, {}, {}, {}, {})
    }
}
