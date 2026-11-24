package com.olehkan.olehkan.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olehkan.olehkan.R
import com.olehkan.olehkan.ui.theme.DmSans
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash Screen aplikasi OlehKan dengan tema terang brand:
 * - Gradasi vertikal halus dari putih (#FFFFFF) ke hijau mint sangat muda (#F2FAF5)
 * - Elemen dekoratif siluet lembut di sudut layar (opasitas 5-8%)
 * - Logo R.drawable.ic_olehkan_icon (animasi spring membal) & ic_olehkan_text (slide-up)
 * - Tagline "Citarasa Nusantara" muncul dengan fade-in menyusul logo
 * - Durasi total 1,8 detik sebelum memanggil onFinish
 */
@Composable
fun SplashScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinish by rememberUpdatedState(onFinish)

    // Animasi untuk ikon
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }

    // Animasi untuk teks logo
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(50f) }

    // Animasi untuk tagline "Citarasa Nusantara"
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffsetY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        // 1. Ikon muncul lebih dulu: spring bounce + fade-in
        launch {
            iconAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // 2. Jeda 300ms: teks logo muncul dari bawah (slide-up + fade-in)
        delay(300)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }

        // 3. Jeda 250ms berikutnya: tagline "Citarasa Nusantara" muncul dengan fade-in menyusul logo
        delay(250)
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            taglineOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }

        // 4. Menunggu sisa durasi hingga total 1,8 detik (300 + 250 + 1250 = 1800ms)
        delay(1250)
        currentOnFinish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF9FDFB),
                        Color(0xFFF2FAF5),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Elemen dekoratif siluet daun & lingkaran lembut di sudut layar (opasitas 5-8%)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Pojok Kanan Atas: Lingkaran & Kurva Daun Halus
            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.06f),
                radius = 160.dp.toPx(),
                center = Offset(width + 20.dp.toPx(), -20.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1F5A45).copy(alpha = 0.05f),
                radius = 90.dp.toPx(),
                center = Offset(width - 30.dp.toPx(), 40.dp.toPx())
            )

            // Siluet silinder/daun dekoratif pojok kanan atas
            val leafPathTop = Path().apply {
                moveTo(width - 120.dp.toPx(), 0f)
                cubicTo(
                    width - 80.dp.toPx(), 90.dp.toPx(),
                    width, 100.dp.toPx(),
                    width, 0f
                )
                close()
            }
            drawPath(
                path = leafPathTop,
                color = Color(0xFF1F5A45).copy(alpha = 0.05f),
                style = Fill
            )

            // Pojok Kiri Bawah: Lingkaran & Kurva Daun Halus
            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.06f),
                radius = 180.dp.toPx(),
                center = Offset(-40.dp.toPx(), height + 30.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1F5A45).copy(alpha = 0.04f),
                radius = 100.dp.toPx(),
                center = Offset(50.dp.toPx(), height - 50.dp.toPx())
            )

            val leafPathBottom = Path().apply {
                moveTo(0f, height - 130.dp.toPx())
                cubicTo(
                    90.dp.toPx(), height - 90.dp.toPx(),
                    100.dp.toPx(), height,
                    0f, height
                )
                close()
            }
            drawPath(
                path = leafPathBottom,
                color = Color(0xFF1ECE99).copy(alpha = 0.07f),
                style = Fill
            )
        }

        // Konten Utama Splash Screen
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ikon OlehKan (spring scale + fade-in)
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_icon),
                contentDescription = "OlehKan Icon",
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                        alpha = iconAlpha.value
                    }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Teks Logo OlehKan (slide-up + fade-in)
            Image(
                painter = painterResource(id = R.drawable.ic_olehkan_text),
                contentDescription = "OlehKan Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(162.dp)
                    .graphicsLayer {
                        translationY = textOffsetY.value
                        alpha = textAlpha.value
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline "Citarasa Nusantara" (fade-in menyusul teks logo)
            Text(
                text = "Citarasa Nusantara",
                style = DmSans.let {
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = it,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 3.5.sp,
                    )
                },
                color = Color(0xFF1F5A45),
                modifier = Modifier.graphicsLayer {
                    translationY = taglineOffsetY.value
                    alpha = taglineAlpha.value
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenPreview() {
    OlehKanTheme {
        SplashScreen(onFinish = {})
    }
}
