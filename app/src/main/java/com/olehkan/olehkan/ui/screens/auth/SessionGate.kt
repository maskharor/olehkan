package com.olehkan.olehkan.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.ui.screens.splash.SplashScreen
import com.olehkan.olehkan.ui.screens.welcome.WelcomeScreen

/**
 * Gate navigasi sesi utama aplikasi OlehKan terintegrasi langsung dengan Supabase Auth:
 * - Tidak menggunakan flag isFirstTime di local storage untuk menghindari race condition.
 * - Satu aturan tunggal pengecekan sesi Supabase setelah SplashScreen selesai:
 *   1. Jika pengguna sudah login (sesi Supabase aktif): Langsung ke Beranda Utama (sesuai role).
 *   2. Jika pengguna belum login (sesi Supabase kosong): Selalu arahkan ke halaman WelcomeScreen
 *      (baik saat aplikasi pertama kali dibuka maupun setelah pengguna logout).
 * - Dari WelcomeScreen:
 *   - "Mulai Sekarang →": Membuka form Login.
 *   - "Belum punya akun? Daftar": Membuka form Register.
 * - Saat Logout (signOut()): Sesi dicabut dan pengguna langsung kembali ke WelcomeScreen,
 *   dengan backstack Beranda dibersihkan (tombol kembali di HP tidak dapat kembali ke Beranda).
 */
@Composable
fun SessionGate(viewModel: AuthViewModel, content: @Composable (UserProfile) -> Unit) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsStateWithLifecycle()
    val account by viewModel.uiState.collectAsStateWithLifecycle()
    val showLoginFromVm by viewModel.showLogin.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showSplash by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.checkSession()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Tentukan layar aktif berdasarkan status sesi Supabase Auth:
    // 1. Tampilkan SplashScreen terlebih dahulu (1,8 detik)
    // 2. Jika sesi Supabase aktif (AUTHENTICATED), arahkan langsung ke Beranda Utama
    // 3. Jika belum login (SIGNED_OUT):
    //    - Tampilkan halaman Login/Register jika pengguna memilihnya dari WelcomeScreen
    //    - Jika tidak, selalu tampilkan WelcomeScreen (halaman awal)
    val currentScreenKey = when {
        showSplash -> "splash"
        session.status == SessionStatus.AUTHENTICATED -> "main"
        showLoginFromVm -> "auth"
        else -> "welcome"
    }

    // Cegah navigasi kembali ke Beranda setelah logout saat berada di WelcomeScreen
    BackHandler(enabled = (currentScreenKey == "welcome")) {
        (context as? Activity)?.finish()
    }

    Crossfade(
        targetState = currentScreenKey,
        animationSpec = tween(durationMillis = 350),
        label = "NavigationCrossfade"
    ) { screen ->
        when (screen) {
            "splash" -> {
                SplashScreen(
                    onFinish = {
                        showSplash = false
                    }
                )
            }
            "welcome" -> {
                WelcomeScreen(
                    onStartClick = {
                        viewModel.openLogin()
                    },
                    onRegisterClick = {
                        viewModel.openRegister()
                    },
                )
            }
            "auth" -> {
                AuthScreen(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.closeLogin()
                    },
                )
            }
            "main" -> {
                when (session.status) {
                    SessionStatus.AUTHENTICATED -> {
                        session.profile?.let { content(it) }
                    }
                    SessionStatus.LOADING -> {
                        Column(
                            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                            Text("Memeriksa sesi…")
                        }
                    }
                    else -> {
                        Column(
                            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(session.error ?: "Sesi belum dapat diperiksa.")
                            account.error?.let { Text(it) }
                            Button(onClick = viewModel::checkSession, enabled = !account.busy) { Text("Coba Lagi") }
                            Button(onClick = { viewModel.openLogin() }) { Text("Masuk") }
                        }
                    }
                }
            }
        }
    }
}
