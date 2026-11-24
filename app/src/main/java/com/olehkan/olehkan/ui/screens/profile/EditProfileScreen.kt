package com.olehkan.olehkan.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.AccountValidator
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AccountUiState
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.theme.*

@Composable
fun EditProfileScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = session.profile ?: return
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
    EditProfileContent(profile, state,
        onNameChange = { viewModel.onEditNameChange(it); viewModel.clearFeedback() },
        onSave = viewModel::updateProfile,
        onBack = { viewModel.prepareEditProfile(); onBack() })
}

@Composable
internal fun EditProfileContent(
    profile: UserProfile,
    state: AccountUiState,
    onNameChange: (String) -> Unit,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
) {
    val name = state.editName ?: profile.fullName
    val normalized = name.trim()
    val dirty = normalized != profile.fullName.trim()
    var touched by rememberSaveable(profile.id) { mutableStateOf(false) }
    var confirmDiscard by rememberSaveable(profile.id) { mutableStateOf(false) }
    val validation = AccountValidator.nameError(name)
    val fieldError = if (touched) validation else state.fieldErrors["name"]
    val focusManager = LocalFocusManager.current
    val requestBack: () -> Unit = {
        if (!state.busy) {
            if (dirty) confirmDiscard = true else onBack()
        }
    }
    BackHandler(enabled = dirty || state.busy) { requestBack() }
    Column(Modifier.fillMaxSize().background(Paper).imePadding()) {
        ProfileTopBar("Edit Profil", requestBack, !state.busy)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileSection("IDENTITAS AKUN")
            Text("Buat akunmu mudah dikenali.", style = OlehKanType.H2, color = Ink)
            Text("Perubahan nama akan tersimpan pada profil akunmu.", style = OlehKanType.BodySmall, color = Muted)
            OutlinedTextField(
                value = name, onValueChange = { touched = true; onNameChange(it) },
                label = { Text("Nama lengkap") }, singleLine = true, enabled = !state.busy,
                isError = fieldError != null,
                supportingText = {
                    Column {
                        fieldError?.let { Text(it) }
                        Text("${normalized.length}/100 karakter")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = White,
                    focusedContainerColor = White, unfocusedBorderColor = Line, focusedBorderColor = Green),
            )
            ProfileNotice("Email akun", profile.email)
            Text("Email dan role tidak diubah melalui form ini.", style = OlehKanType.Caption, color = Muted)
            ProfileSection("PRATINJAU NAMA")
            Surface(color = GreenLight, shape = OlehKanShapes.Medium) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Person, null, tint = Green)
                    Text(normalized.ifBlank { "Nama lengkap" }, style = OlehKanType.H3, color = Ink)
                }
            }
            state.error?.let { ProfileNotice("Perubahan belum berhasil", it, error = true) }
            Button(onClick = { focusManager.clearFocus(); onSave(normalized) },
                enabled = !state.busy && dirty && validation == null,
                modifier = Modifier.fillMaxWidth(), shape = OlehKanShapes.Pill) {
                if (state.busy) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.busy) "Menyimpan…" else "Simpan Profil")
            }
            if (!dirty) Text("Ubah nama untuk mengaktifkan tombol simpan.", style = OlehKanType.Caption, color = Muted)
            Spacer(Modifier.height(8.dp))
        }
    }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text("Buang perubahan?") },
        text = { Text("Perubahan nama yang belum disimpan akan hilang.") },
        confirmButton = { TextButton(onClick = { confirmDiscard = false; onBack() }) { Text("Buang perubahan") } },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Lanjut edit") } },
    )
}

@Preview(showBackground = true)
@Composable
private fun EditProfilePreview() {
    OlehKanTheme {
        EditProfileContent(UserProfile("preview", "Admin OlehKan", "admin@example.test", UserRole.ADMIN),
            AccountUiState(editName = "Admin Toko"), {}, {}, {})
    }
}
