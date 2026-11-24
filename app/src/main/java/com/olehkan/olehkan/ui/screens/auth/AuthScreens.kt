package com.olehkan.olehkan.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olehkan.olehkan.R
import com.olehkan.olehkan.ui.theme.OlehKanType

private val BrandGreen = Color(0xFF14452F)
private val SubtitleGray = Color(0xFF4A5568)
private val BorderGray = Color(0xFFE2E8F0)
private val ErrorRedBg = Color(0xFFFFF5F5)
private val ErrorRedBorder = Color(0xFFFED7D7)
private val ErrorRedText = Color(0xFFC53030)
private val ErrorRedIcon = Color(0xFFE53E3E)
private val InfoGreenBg = Color(0xFFF0FFF4)
private val InfoGreenBorder = Color(0xFFC6F6D5)
private val InfoGreenText = Color(0xFF276749)
private val InfoGreenIcon = Color(0xFF38A169)

/**
 * Kontainer Auth utama yang beralih antara Login dan Register secara mulus tanpa menumpuk backstack.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val handleBack = onBack ?: { viewModel.closeLogin() }

    Crossfade(
        targetState = state.registering,
        animationSpec = tween(durationMillis = 300),
        label = "AuthCrossfade",
    ) { isRegistering ->
        if (isRegistering) {
            RegisterScreen(
                viewModel = viewModel,
                onBack = handleBack,
                onNavigateToLogin = { viewModel.openLogin() },
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onBack = handleBack,
                onNavigateToRegister = { viewModel.openRegister() },
            )
        }
    }
}

/**
 * Halaman Login aplikasi OlehKan yang selaras secara visual dengan SplashScreen & WelcomeScreen.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showPassword by remember { mutableStateOf(false) }

    AuthScaffold(
        onBack = onBack,
        modifier = modifier,
    ) {
        // Logo Resmi OlehKan (ic_olehkan_icon + ic_olehkan_text) berada di tengah horizontal di atas judul
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_icon),
                contentDescription = "OlehKan Icon",
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_text),
                contentDescription = "OlehKan",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(115.dp),
            )
        }

        // 1. Tipografi Judul & Subjudul
        Text(
            text = "Selamat Datang!",
            style = OlehKanType.H1.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Masuk ke akunmu untuk melanjutkan berbelanja oleh-oleh khas Nusantara.",
            style = OlehKanType.Body.copy(fontSize = 14.sp),
            color = SubtitleGray,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Kotak Peringatan Error / Pesan
        val errorMessage = state.error ?: session.error
        if (errorMessage != null) {
            ErrorAlertBox(message = errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.message != null) {
            MessageAlertBox(message = state.message!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Field Input Email
        AuthTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            placeholder = "contoh@email.com",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            error = state.fieldErrors["email"],
            enabled = !state.busy,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Field Input Password
        AuthTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            placeholder = "Masukkan password",
            leadingIcon = Icons.Outlined.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true,
            passwordVisible = showPassword,
            onTogglePassword = { showPassword = !showPassword },
            error = state.fieldErrors["password"],
            enabled = !state.busy,
            onDone = {
                focusManager.clearFocus()
                viewModel.login(state.email, state.password)
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Tombol Utama Pill Penuh "Masuk"
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.login(state.email, state.password)
            },
            enabled = !state.busy,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen,
                contentColor = Color.White,
                disabledContainerColor = BrandGreen.copy(alpha = 0.6f),
                disabledContentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (state.busy) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                )
            } else {
                Text(
                    text = "Masuk",
                    style = OlehKanType.ButtonPrimary.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Tautan Bawah "Belum punya akun? Daftar"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Belum punya akun? ",
                style = OlehKanType.Body.copy(fontSize = 14.sp),
                color = SubtitleGray,
            )
            Text(
                text = "Daftar",
                style = OlehKanType.BodyBold.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = BrandGreen,
                modifier = Modifier
                    .clickable(enabled = !state.busy) { onNavigateToRegister() }
                    .padding(4.dp),
            )
        }
    }
}

/**
 * Halaman Register aplikasi OlehKan dengan field Nama Lengkap, Email, Password, dan Konfirmasi Password.
 * Role secara otomatis diset sebagai BUYER oleh database tanpa opsi pemilihan manual.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    AuthScaffold(
        onBack = onBack,
        modifier = modifier,
    ) {
        // Logo Resmi OlehKan (ic_olehkan_icon + ic_olehkan_text) berada di tengah horizontal di atas judul
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_icon),
                contentDescription = "OlehKan Icon",
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_text),
                contentDescription = "OlehKan",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(115.dp),
            )
        }

        // 1. Tipografi Judul & Subjudul
        Text(
            text = "Daftar Akun Baru",
            style = OlehKanType.H1.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            color = BrandGreen,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Lengkapi data dirimu untuk mulai menjelajahi dan memesan oleh-oleh khas daerah.",
            style = OlehKanType.Body.copy(fontSize = 14.sp),
            color = SubtitleGray,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Kotak Peringatan Error / Pesan
        val errorMessage = state.error ?: session.error
        if (errorMessage != null) {
            ErrorAlertBox(message = errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.message != null) {
            MessageAlertBox(message = state.message!!)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Field Input Nama Lengkap (dikirim ke metadata full_name)
        AuthTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = "Nama Lengkap",
            placeholder = "Nama lengkap Anda",
            leadingIcon = Icons.Outlined.Person,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            error = state.fieldErrors["name"],
            enabled = !state.busy,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Field Input Email
        AuthTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            placeholder = "contoh@email.com",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            error = state.fieldErrors["email"],
            enabled = !state.busy,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Field Input Password
        AuthTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            placeholder = "Minimal 6 karakter",
            leadingIcon = Icons.Outlined.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            isPassword = true,
            passwordVisible = showPassword,
            onTogglePassword = { showPassword = !showPassword },
            error = state.fieldErrors["password"],
            enabled = !state.busy,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Field Input Konfirmasi Password
        AuthTextField(
            value = state.confirmation,
            onValueChange = viewModel::onConfirmationChange,
            label = "Konfirmasi Password",
            placeholder = "Ulangi password",
            leadingIcon = Icons.Outlined.Lock,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true,
            passwordVisible = showConfirmPassword,
            onTogglePassword = { showConfirmPassword = !showConfirmPassword },
            error = state.fieldErrors["confirmation"],
            enabled = !state.busy,
            onDone = {
                focusManager.clearFocus()
                viewModel.register(state.name, state.email, state.password, state.confirmation)
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Tombol Utama Pill Penuh "Daftar Sekarang"
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.register(state.name, state.email, state.password, state.confirmation)
            },
            enabled = !state.busy,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGreen,
                contentColor = Color.White,
                disabledContainerColor = BrandGreen.copy(alpha = 0.6f),
                disabledContentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (state.busy) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                )
            } else {
                Text(
                    text = "Daftar Sekarang",
                    style = OlehKanType.ButtonPrimary.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Tautan Bawah "Sudah punya akun? Masuk"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sudah punya akun? ",
                style = OlehKanType.Body.copy(fontSize = 14.sp),
                color = SubtitleGray,
            )
            Text(
                text = "Masuk",
                style = OlehKanType.BodyBold.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = BrandGreen,
                modifier = Modifier
                    .clickable(enabled = !state.busy) { onNavigateToLogin() }
                    .padding(4.dp),
            )
        }
    }
}

/**
 * Kerangka visual bersama untuk Auth: Latar gradasi mint lembut, dekorasi siluet, tombol kembali, dan logo resmi.
 */
@Composable
private fun AuthScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF2FAF5),
                        Color(0xFFF8FCFA),
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),
                    ),
                ),
            )
            .statusBarsPadding(),
    ) {
        // Siluet dekoratif melingkar halus di latar belakang
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.05f),
                radius = 130.dp.toPx(),
                center = Offset(width + 10.dp.toPx(), -10.dp.toPx()),
            )
            drawCircle(
                color = Color(0xFF1F5A45).copy(alpha = 0.04f),
                radius = 70.dp.toPx(),
                center = Offset(width - 20.dp.toPx(), 60.dp.toPx()),
            )
            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.04f),
                radius = 150.dp.toPx(),
                center = Offset(-30.dp.toPx(), height + 20.dp.toPx()),
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .heightIn(min = minHeight - 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Header Atas: Sisakan hanya tombol kembali (<-) di pojok kiri atas (Alignment.TopStart)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                ) {
                    // Tombol Kembali (<-) di pojok kiri atas
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2EFE7)),
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopStart),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Welcome",
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Konten Form (Judul, Input, Tombol Utama, Tautan) berada di tengah secara seimbang
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                ) {
                    content()
                }

                // Spacer penyeimbang bawah agar form berada tepat di tengah layar
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Komponen Input Text modern bersudut 14.dp dengan warna dan leading/trailing icon yang proporsional.
 */
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    error: String? = null,
    enabled: Boolean = true,
    onDone: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = Color(0xFFA0AEC0)) },
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() },
            ),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Lihat password",
                            tint = Color(0xFF718096),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandGreen,
                unfocusedBorderColor = BorderGray,
                focusedLabelColor = BrandGreen,
                unfocusedLabelColor = Color(0xFF718096),
                cursorColor = BrandGreen,
                focusedLeadingIconColor = BrandGreen,
                unfocusedLeadingIconColor = Color(0xFF718096),
                errorBorderColor = ErrorRedIcon,
                errorLabelColor = ErrorRedText,
                errorLeadingIconColor = ErrorRedIcon,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (error != null) {
            Text(
                text = error,
                style = OlehKanType.Caption,
                color = ErrorRedText,
                modifier = Modifier.padding(start = 14.dp, top = 4.dp),
            )
        }
    }
}

/**
 * Komponen Kotak Peringatan Error (Background merah lembut, border tipis, ikon peringatan).
 */
@Composable
private fun ErrorAlertBox(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ErrorRedBg,
        border = BorderStroke(1.dp, ErrorRedBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Peringatan",
                tint = ErrorRedIcon,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = message,
                style = OlehKanType.BodySmall.copy(fontWeight = FontWeight.Medium),
                color = ErrorRedText,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Komponen Kotak Informasi / Pesan Sukses (Background hijau lembut, border tipis, ikon centang).
 */
@Composable
private fun MessageAlertBox(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = InfoGreenBg,
        border = BorderStroke(1.dp, InfoGreenBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Info",
                tint = InfoGreenIcon,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = message,
                style = OlehKanType.BodySmall.copy(fontWeight = FontWeight.Medium),
                color = InfoGreenText,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Komponen pembantu untuk form edit profil dan pengisian akun yang kompatibel.
 */
@Composable
fun AccountField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    error: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    AuthTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        placeholder = label,
        leadingIcon = Icons.Outlined.Person,
        keyboardType = keyboardType,
        imeAction = ImeAction.Done,
        error = error,
        enabled = enabled,
    )
}
