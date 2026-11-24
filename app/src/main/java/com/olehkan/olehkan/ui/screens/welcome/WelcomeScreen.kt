package com.olehkan.olehkan.ui.screens.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.olehkan.olehkan.R
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.PlusJakartaSans
import com.olehkan.olehkan.ui.theme.White

/**
 * Tampilan Onboarding / Welcome Screen aplikasi OlehKan dengan Tema Terang & Bersih (Light & Clean).
 * Selaras dengan tema visual OlehKan:
 * - Latar belakang putih bersih (#FFFFFF) dengan gradasi mint lembut (#F2FAF5) di bagian atas.
 * - Bagian atas: Logo R.drawable.ic_olehkan_icon dan R.drawable.ic_olehkan_text rapi + kartu ilustrasi oleh-oleh nusantara.
 * - Bagian tengah: Headline "Bawa pulang cerita dari setiap daerah." (#14452F), deskripsi abu-abu gelap, dan 2 chip minimalis.
 * - Bagian bawah: Indikator dot, tombol pill hijau brand "Mulai Jelajahi Oleh-Oleh →", serta baris rapi opsi Masuk / Masuk Toko.
 */
@Composable
fun WelcomeScreen(
    onExploreClick: () -> Unit,
    onLoginClick: () -> Unit,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // Elemen dekoratif siluet lembut di latar belakang
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Pojok Kanan Atas
            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.05f),
                radius = 140.dp.toPx(),
                center = Offset(width + 10.dp.toPx(), -10.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1F5A45).copy(alpha = 0.04f),
                radius = 80.dp.toPx(),
                center = Offset(width - 20.dp.toPx(), 60.dp.toPx())
            )

            // Pojok Kiri Bawah
            drawCircle(
                color = Color(0xFF1ECE99).copy(alpha = 0.05f),
                radius = 160.dp.toPx(),
                center = Offset(-30.dp.toPx(), height + 20.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. BAGIAN ATAS: Logo Rapi (Ikon + Teks)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_olehkan_icon),
                    contentDescription = "OlehKan Icon",
                    modifier = Modifier.size(38.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_olehkan_text),
                    contentDescription = "OlehKan",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(115.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Kartu Ilustrasi / Showcase Bertema Oleh-Oleh Khas Nusantara
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = BorderStroke(1.dp, Color(0xFFE2EFE7)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        // Foto Oleh-Oleh Nusantara (Bakpia & Jajanan Tradisional Autentik)
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1586511926871-deb1aca37f38?auto=format&fit=crop&w=800&q=85",
                            contentDescription = "Oleh-Oleh Tradisional Nusantara",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle gradient overlay at top & bottom of image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                        )

                        // Floating Badges inside the illustration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = White.copy(alpha = 0.92f),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF1F5A45),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Khas Yogyakarta & Nusantara",
                                        style = PlusJakartaSans.let {
                                            androidx.compose.ui.text.TextStyle(
                                                fontFamily = it,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        },
                                        color = Color(0xFF14452F)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = White.copy(alpha = 0.92f),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFEF9A3A),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "4.9",
                                        style = PlusJakartaSans.let {
                                            androidx.compose.ui.text.TextStyle(
                                                fontFamily = it,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        },
                                        color = Color(0xFF14452F)
                                    )
                                }
                            }
                        }

                        // Caption at bottom of illustration image
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Bakpia Pathok & Aneka Jajanan Otentik",
                                style = PlusJakartaSans.let {
                                    androidx.compose.ui.text.TextStyle(
                                        fontFamily = it,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                },
                                color = White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dikurasi dari 34 Provinsi",
                            style = PlusJakartaSans.let {
                                androidx.compose.ui.text.TextStyle(
                                    fontFamily = it,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            color = Color(0xFF5A6661)
                        )
                        Text(
                            text = "100+ UMKM Lokal",
                            style = PlusJakartaSans.let {
                                androidx.compose.ui.text.TextStyle(
                                    fontFamily = it,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            color = Color(0xFF1F5A45)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. BAGIAN TENGAH: Judul, Deskripsi Singkat, & 2 Chip Minimalis
            Text(
                text = "Bawa pulang cerita\ndari setiap daerah.",
                style = PlusJakartaSans.let {
                    androidx.compose.ui.text.TextStyle(
                        fontFamily = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 35.sp,
                    )
                },
                color = Color(0xFF14452F),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Kelezatan bakpia autentik, keripik gurih, hingga kopi sangrai lokal langsung dari perajin terpercaya.",
                style = OlehKanType.Body.copy(
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                ),
                color = Color(0xFF5A6661),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2 Chip Minimalis: 100% Asli Daerah & Pengiriman Cepat
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEBF6F0),
                    border = BorderStroke(1.dp, Color(0xFFCEEBDB)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color(0xFF1F5A45),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "100% Asli Daerah",
                            style = PlusJakartaSans.let {
                                androidx.compose.ui.text.TextStyle(
                                    fontFamily = it,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                            },
                            color = Color(0xFF1F5A45),
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Pengiriman Cepat",
                            style = PlusJakartaSans.let {
                                androidx.compose.ui.text.TextStyle(
                                    fontFamily = it,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                            },
                            color = Color(0xFF9A3412),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. BAGIAN BAWAH: Indikator Dot
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 6.dp)
                        .background(Color(0xFF1F5A45), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFD1DDD7), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFD1DDD7), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Utama Berbentuk Pill Hijau Brand "Mulai Jelajahi Oleh-Oleh →"
            Button(
                onClick = onExploreClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F5A45),
                    contentColor = White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Mulai Jelajahi Oleh-Oleh",
                        style = PlusJakartaSans.let {
                            androidx.compose.ui.text.TextStyle(
                                fontFamily = it,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        },
                        color = White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Baris Rapi: "Sudah punya akun? Masuk" di kiri dan "Masuk Toko >" di kanan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable { onLoginClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Sudah punya akun? ",
                        style = OlehKanType.BodySmall,
                        color = Color(0xFF5A6661),
                    )
                    Text(
                        text = "Masuk",
                        style = OlehKanType.BodyBold,
                        color = Color(0xFF1F5A45),
                        textDecoration = TextDecoration.Underline,
                    )
                }

                Row(
                    modifier = Modifier.clickable { onStoreClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Masuk Toko",
                        style = OlehKanType.BodyBold,
                        color = Color(0xFFE97832),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFE97832),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPreview() {
    OlehKanTheme {
        WelcomeScreen(
            onExploreClick = {},
            onLoginClick = {},
            onStoreClick = {},
        )
    }
}
