package com.olehkan.olehkan.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.model.UserProfile

@Composable
fun SessionGate(viewModel: AuthViewModel, content: @Composable (UserProfile) -> Unit) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val account by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.checkSession()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    when (session.status) {
        SessionStatus.SIGNED_OUT -> AuthScreen(viewModel)
        SessionStatus.AUTHENTICATED -> session.profile?.let { content(it) }
        else -> Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (session.status == SessionStatus.LOADING) {
                CircularProgressIndicator()
                Text("Memeriksa sesi…")
            } else {
                Text(session.error ?: "Akun belum dapat dimuat.")
                account.error?.let { Text(it) }
                Button(onClick = viewModel::checkSession, enabled = !account.busy) { Text("Coba Lagi") }
                Button(onClick = { viewModel.logout() }, enabled = !account.busy) { Text("Logout") }
            }
        }
    }
}
