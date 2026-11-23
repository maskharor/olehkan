package com.olehkan.olehkan.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.screens.auth.AccountField
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun EditProfileScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = session.profile ?: return
    val name = state.editName.orEmpty()
    val context = LocalContext.current
    LaunchedEffect(profile.id) {
        if (state.editName == null) viewModel.prepareEditProfile()
    }
    LaunchedEffect(state.profileSaved) {
        if (state.profileSaved) {
            Toast.makeText(context, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
            onBack()
        }
    }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack, enabled = !state.busy) { Text("← Kembali") }
        Text("Edit Profil", style = OlehKanType.H1)
        Text("Email: ${profile.email}", style = OlehKanType.Body)
        AccountField(name, viewModel::onEditNameChange, "Nama lengkap", state.fieldErrors["name"], !state.busy)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.busy) CircularProgressIndicator()
        Button(onClick = { viewModel.updateProfile(name) }, enabled = !state.busy) { Text("Simpan Profil") }
    }
}
