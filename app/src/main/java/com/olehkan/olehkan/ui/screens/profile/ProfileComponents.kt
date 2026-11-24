package com.olehkan.olehkan.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.R
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.theme.*
import java.util.Locale

@Composable
internal fun ProfileTopBar(title: String, onBack: () -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().background(GreenDark).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, enabled = enabled) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = White)
        }
        Text(title, style = OlehKanType.SubHeaderTitle, color = White, modifier = Modifier.weight(1f))
        Image(painterResource(R.drawable.ic_olehkan_icon), "Logo OlehKan",
            modifier = Modifier.padding(end = 12.dp).size(32.dp))
    }
}

@Composable
internal fun ProfileIdentity(profile: UserProfile, displayName: String = profile.fullName) {
    Surface(shape = OlehKanShapes.Large, color = GreenDark,
        border = BorderStroke(1.dp, White.copy(alpha = 0.12f))) {
        Box(Modifier.fillMaxWidth().heightIn(min = 152.dp)
            .background(Brush.horizontalGradient(listOf(GreenDark, Green)))) {
            // Dekorasi berada di belakang konten dan tidak menambah informasi untuk pembaca layar.
            Box(Modifier.align(Alignment.TopEnd).offset(x = 58.dp, y = (-82).dp)
                .size(190.dp).border(1.dp, White.copy(alpha = 0.10f), CircleShape))
            Box(Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 74.dp)
                .size(160.dp).border(1.dp, White.copy(alpha = 0.08f), CircleShape))
            Image(
                painter = painterResource(R.drawable.ic_olehkan_icon),
                contentDescription = null, alpha = 0.10f,
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 24.dp).size(144.dp).rotate(-12f),
            )
            Row(Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 20.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = White.copy(alpha = 0.15f), shape = CircleShape,
                    border = BorderStroke(2.dp, White.copy(alpha = 0.35f)),
                    modifier = Modifier.size(76.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(displayName.trim().take(1).uppercase(Locale.ROOT).ifBlank { "?" },
                            style = OlehKanType.Hero, color = White)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(displayName.ifBlank { "Nama lengkap" }, style = OlehKanType.H2, color = White)
                    Surface(color = White.copy(alpha = 0.16f), shape = OlehKanShapes.Pill) {
                        Text(if (profile.role == UserRole.ADMIN) "ADMIN TOKO" else "BUYER",
                            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = OlehKanType.CaptionBold, color = White)
                    }
                    Text(profile.email, style = OlehKanType.BodySmall, color = White.copy(alpha = 0.88f))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 230)
@Composable
private fun ProfileIdentityPreview() {
    OlehKanTheme {
        Box(Modifier.fillMaxSize().background(Paper).padding(20.dp)) {
            ProfileIdentity(UserProfile("preview", "Pembeli OlehKan", "buyer@example.test", UserRole.BUYER))
        }
    }
}

@Composable
internal fun ProfileSection(title: String) {
    Text(title, style = OlehKanType.Eyebrow, color = Orange)
}

@Composable
internal fun ProfileAction(title: String, subtitle: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, color = White, shape = OlehKanShapes.Medium,
        border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = GreenLight, shape = OlehKanShapes.Small) {
                Icon(icon, null, tint = Green, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = OlehKanType.H3, color = Ink)
                Text(subtitle, style = OlehKanType.BodySmall, color = Muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Green)
        }
    }
}

@Composable
internal fun ProfileNotice(title: String, message: String, error: Boolean = false) {
    Surface(color = if (error) WarningBg else GreenLight, shape = OlehKanShapes.Medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = OlehKanType.H3, color = if (error) WarningText else Green)
            Text(message, style = OlehKanType.BodySmall, color = if (error) WarningText else Ink)
        }
    }
}
