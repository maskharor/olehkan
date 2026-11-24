package com.olehkan.olehkan.ui.screens.auth

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.data.model.SessionStatus
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.splash.SplashScreen
import com.olehkan.olehkan.ui.screens.welcome.WelcomeScreen

/**
 * Gate navigasi sesi utama aplikasi OlehKan terintegrasi Supabase Auth:
 * - Pengguna Baru (isFirstTime == true): Setelah Splash Screen, diarahkan ke Onboarding (WelcomeScreen)
 *   dan flag isFirstTime diubah menjadi false di SharedPreferences.
 *   Tombol "Mulai Jelajahi Oleh-Oleh" langsung membuka Beranda Utama (Mode Tamu),
 *   sedangkan tombol "Masuk" membuka Login (AuthScreen).
 * - Pengguna Lama (isFirstTime == false): Setelah Splash Screen, halaman Onboarding dilewati,
 *   langsung membuka Beranda Utama (baik akun Supabase aktif maupun Mode Tamu).
 * - Saat Logout / Sesi Berakhir: Sesi Supabase dicabut melalui signOut() dan pengguna
 *   langsung kembali ke Beranda Utama (Mode Tamu) dengan opsi login kapan saja.
 */
@Composable
fun SessionGate(viewModel: AuthViewModel, content: @Composable (UserProfile) -> Unit) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val account by viewModel.uiState.collectAsStateWithLifecycle()
    val showLoginFromVm by viewModel.showLogin.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Cek status isFirstTime dari SharedPreferences saat aplikasi dimuat
    val isFirstTimeUser = remember { viewModel.preferences.getIsFirstTime() }
    var showSplash by remember { mutableStateOf(true) }
    var showWelcome by remember { mutableStateOf(false) }
    var showAuth by remember { mutableStateOf(false) }
    var forceGuestMode by remember { mutableStateOf(false) }

    val guestProfile = remember {
        UserProfile(
            id = "guest_buyer",
            fullName = "Pengunjung",
            email = "tamu@olehkan.id",
            role = UserRole.BUYER,
        )
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.checkSession()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Tentukan layar aktif
    val currentScreenKey = when {
        showSplash -> "splash"
        showWelcome -> "welcome"
        (showAuth || showLoginFromVm) && session.status != SessionStatus.AUTHENTICATED -> "auth"
        else -> "main"
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
                        if (isFirstTimeUser) {
                            // Pengguna baru: ubah isFirstTime menjadi false di SharedPreferences & tampilkan Onboarding
                            viewModel.setFirstTime(false)
                            showWelcome = true
                        } else {
                            // Pengguna lama: lewati Onboarding dan langsung ke Beranda Utama
                            showWelcome = false
                        }
                    }
                )
            }
            "welcome" -> {
                WelcomeScreen(
                    onExploreClick = {
                        // "Mulai Jelajahi Oleh-Oleh" langsung membuka Beranda Utama (Mode Tamu) tanpa wajib login
                        showWelcome = false
                    },
                    onLoginClick = {
                        // "Masuk" membuka layar Login
                        showWelcome = false
                        showAuth = true
                        viewModel.openLogin()
                    },
                    onStoreClick = {
                        showWelcome = false
                        showAuth = true
                        viewModel.openLogin()
                    },
                )
            }
            "auth" -> {
                AuthScreen(
                    viewModel = viewModel,
                    onBack = {
                        showAuth = false
                        viewModel.closeLogin()
                    },
                )
            }
            "main" -> {
                if (forceGuestMode) {
                    content(guestProfile)
                } else {
                    when (session.status) {
                        SessionStatus.AUTHENTICATED -> {
                            session.profile?.let { content(it) }
                        }
                        SessionStatus.SIGNED_OUT -> {
                            // Mode Tamu: pengguna lama tanpa login aktif / setelah logout
                            content(guestProfile)
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
                                Text(session.error ?: "Akun belum dapat dimuat.")
                                account.error?.let { Text(it) }
                                Button(onClick = viewModel::checkSession, enabled = !account.busy) { Text("Coba Lagi") }
                                Button(onClick = { forceGuestMode = true }) { Text("Lanjut Sebagai Tamu") }
                            }
                        }
                    }
                }
            }
        }
    }
}
