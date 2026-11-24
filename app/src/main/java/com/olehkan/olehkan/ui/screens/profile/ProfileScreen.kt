package com.olehkan.olehkan.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.theme.OlehKanType

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

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Kembali") }
        Text("Profil Akun", style = OlehKanType.H1)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(profile.fullName, style = OlehKanType.H2)
                Text(profile.email, style = OlehKanType.Body)
                Text(if (profile.role == UserRole.ADMIN) "Admin toko" else "Buyer", style = OlehKanType.BodySmall)
            }
        }
        Button(onClick = onEditClick, enabled = !state.busy) { Text("Edit Profil") }
        if (profile.role == UserRole.ADMIN) {
            Button(onClick = onAdminClick) { Text("Dashboard Admin") }
            Button(onClick = onCategoriesClick) { Text("Kelola Kategori") }
        } else {
            TextButton(onClick = onReviewsClick) { Text("Ulasan Saya") }
        }
        state.error?.let { Text(it) }
        OutlinedButton(onClick = { viewModel.signOut() }, enabled = !state.busy) {
            Text(if (state.busy) "Memproses…" else "Logout")
        }
    }
}
