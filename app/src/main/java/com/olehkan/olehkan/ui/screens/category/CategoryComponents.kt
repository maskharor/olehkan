package com.olehkan.olehkan.ui.screens.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.ui.theme.*

@Composable
internal fun CategoryPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        Row(Modifier.fillMaxWidth().background(GreenDark).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = White) }
            Text(title, style = OlehKanType.SubHeaderTitle, color = White, modifier = Modifier.weight(1f))
            Text("OLEHKAN", Modifier.padding(end = 12.dp), style = OlehKanType.Eyebrow, color = White)
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
internal fun CategoryHero(eyebrow: String, title: String, subtitle: String) {
    Surface(shape = OlehKanShapes.Large, color = Green) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, Green))).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Category, null, tint = White, modifier = Modifier.size(28.dp))
            Text(eyebrow, style = OlehKanType.Eyebrow, color = White.copy(alpha = 0.8f))
            Text(title, style = OlehKanType.H1, color = White)
            Text(subtitle, style = OlehKanType.BodySmall, color = White.copy(alpha = 0.85f))
        }
    }
}

@Composable
internal fun CategoryStatus(category: Category) {
    val label = when {
        category.isProtected -> "Dilindungi"
        !category.usageKnown -> "Perlu diperiksa"
        category.productCount > 0 -> "Digunakan produk"
        else -> "Bisa dikelola"
    }
    Surface(color = if (category.canManage) GreenLight else WarningBg, shape = OlehKanShapes.Pill) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = OlehKanType.CaptionBold, color = if (category.canManage) Green else WarningText)
    }
}

internal fun categoryUsage(category: Category): String = if (category.usageKnown)
    "${category.productCount} produk yang dapat diakses" else "Relasi produk belum dapat diperiksa"

@Composable
internal fun CategoryNotice(title: String, text: String) {
    Surface(color = GreenLight, shape = OlehKanShapes.Medium, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = OlehKanType.H3, color = Green)
            Text(text, style = OlehKanType.BodySmall, color = Ink)
        }
    }
}

@Composable
internal fun CategoryLoading() {
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(Modifier.size(24.dp), color = Green, strokeWidth = 2.dp)
        Text("Memuat kategori…", style = OlehKanType.Body, color = Muted)
    }
}

@Composable
internal fun CategoryError(message: String, onRetry: (() -> Unit)? = null) {
    Surface(color = WarningBg, shape = OlehKanShapes.Medium) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(message, color = WarningText, style = OlehKanType.BodySmall)
            if (onRetry != null) TextButton(onClick = onRetry) { Text("Coba Lagi", color = Green) }
        }
    }
}
