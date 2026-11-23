package com.olehkan.olehkan.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val registering = state.registering
    val name = state.name
    val email = state.email
    val password = state.password
    val confirmation = state.confirmation
    val showPassword = state.showPassword

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("OlehKan", style = OlehKanType.Hero)
        Text(if (registering) "Daftar sebagai pembeli" else "Selamat datang kembali", style = OlehKanType.H2)
        Text("Jelajahi oleh-oleh pilihan dari toko kami.", style = OlehKanType.Body)
        if (registering) AccountField(
            name, viewModel::onNameChange, "Nama lengkap", state.fieldErrors["name"], !state.busy,
        )
        AccountField(
            email, viewModel::onEmailChange, "Email", state.fieldErrors["email"], !state.busy, KeyboardType.Email,
        )
        OutlinedTextField(
            value = password, onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") }, singleLine = true, enabled = !state.busy,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = viewModel::togglePasswordVisibility) {
                    Text(if (showPassword) "Sembunyikan" else "Lihat")
                }
            },
            isError = state.fieldErrors.containsKey("password"),
            supportingText = { state.fieldErrors["password"]?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        if (registering) OutlinedTextField(
            value = confirmation, onValueChange = viewModel::onConfirmationChange,
            label = { Text("Konfirmasi password") }, singleLine = true, enabled = !state.busy,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = state.fieldErrors.containsKey("confirmation"),
            supportingText = { state.fieldErrors["confirmation"]?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        (state.error ?: session.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.message?.let { Text(it, style = OlehKanType.Body) }
        if (state.busy) CircularProgressIndicator()
        Button(
            onClick = {
                if (registering) viewModel.register(name, email, password, confirmation)
                else viewModel.login(email, password)
            },
            enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
        ) { Text(if (registering) "Daftar Buyer" else "Login") }
        TextButton(onClick = viewModel::toggleRegistration, enabled = !state.busy) {
            Text(if (registering) "Sudah punya akun? Login" else "Belum punya akun? Daftar")
        }
    }
}

@Composable
fun AccountField(
    value: String, onChange: (String) -> Unit, label: String,
    error: String? = null, enabled: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        singleLine = true, enabled = enabled, isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = { error?.let { Text(it) } }, modifier = Modifier.fillMaxWidth(),
    )
}
