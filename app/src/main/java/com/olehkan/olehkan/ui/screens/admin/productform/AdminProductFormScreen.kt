package com.olehkan.olehkan.ui.screens.admin.productform

import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.White

/**
 * Halaman Form Tambah/Edit Produk (Fitur 2, MVVM).
 * Satu form untuk tambah (productUuid == null) dan edit.
 * Upload gambar: pilih foto dari galeri -> ViewModel upload ke
 * Storage `product-images` saat Simpan, lalu URL-nya disimpan
 * ke tabel `products`.
 *
 * Tampilan mengikuti mockup yang diberikan (Section 2):
 * kartu putih, kotak upload foto garis putus-putus, field
 * membulat, dan tombol "Terbitkan ke Etalase Toko".
 * Logika dan ViewModel tidak berubah — hanya tampilannya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    productUuid: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: AdminProductFormViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val priceText by viewModel.priceText.collectAsStateWithLifecycle()
    val stockText by viewModel.stockText.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val isActive by viewModel.isActive.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val isAdd = productUuid == null
    val fieldShape = RoundedCornerShape(12.dp)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: "product-${System.currentTimeMillis()}.jpg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) viewModel.onImagePicked(uri, bytes, contentType, fileName)
    }

    LaunchedEffect(productUuid) { viewModel.prepareForm(productUuid) }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeSuccess()
            onSaved()
        }
    }

    if (showDeleteDialog && productUuid != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus produk?") },
            text = { Text("Produk \"$name\" akan dihapus. Produk yang sudah dipakai transaksi sebaiknya dinonaktifkan saja, bukan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(productUuid) { onDeleted() }
                    },
                    enabled = !state.isDeleting,
                ) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            },
        )
    }

    Column(Modifier.fillMaxSize().background(Paper)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text(
                if (isAdd) "Tambah Produk" else "Edit Produk",
                style = OlehKanType.SubHeaderTitle, color = Ink,
            )
        }
        when {
            state.isLoading -> {
                Column(
                    Modifier.fillMaxWidth().padding(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Memuat form produk…", style = OlehKanType.Body)
                }
            }
            state.productNotFound -> {
                Text(
                    "Produk tidak ditemukan.",
                    style = OlehKanType.Body,
                    modifier = Modifier.padding(Dimens.ScreenPadding),
                )
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(16.dp)),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // --- Judul form ---
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = Green, modifier = Modifier.size(22.dp))
                                Text(
                                    if (isAdd) "Tambah Produk Baru" else "Edit Produk",
                                    style = OlehKanType.H3, color = Ink,
                                )
                            }
                            Text(
                                "Pajang karya & cita rasa khas toko Anda ke wisatawan dan pembeli nusantara.",
                                style = OlehKanType.BodySmall, color = Muted,
                            )
                        }
                        // --- Foto produk: kotak upload garis putus-putus ---
                        // Foto itu OPSIONAL: boleh dikosongkan, produk tetap
                        // bisa ditambahkan. Kalau upload gagal, produk
                        // disimpan tanpa foto.
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Foto Produk Oleh-Oleh (opsional)", style = OlehKanType.BodyBold, color = Ink)
                            Text(
                                "Foto boleh dikosongkan — produk tetap bisa ditambahkan.",
                                style = OlehKanType.BodySmall, color = Muted,
                            )
                            val previewUri = viewModel.previewImageUri()
                            val previewModel: Any? = previewUri ?: state.existingImageUrl.takeIf { it.isNotBlank() }
                            Box(
                                Modifier.fillMaxWidth()
                                    .dashedBorder(color = Muted, cornerRadius = 12.dp)
                                    .background(Paper, RoundedCornerShape(12.dp))
                                    .clickable { imagePicker.launch("image/*") }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (previewModel != null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(Modifier.size(128.dp)) {
                                            AsyncImage(
                                                model = previewModel,
                                                contentDescription = "Foto produk",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(128.dp).clip(RoundedCornerShape(12.dp)),
                                            )
                                            IconButton(
                                                onClick = { viewModel.onImageRemoved() },
                                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
                                            ) {
                                                Box(
                                                    Modifier.size(24.dp).clip(RoundedCornerShape(999.dp)).background(White),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(Icons.Filled.Close, contentDescription = "Hapus foto", tint = Ink, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
                                            Text("Foto siap diunggah", style = OlehKanType.Caption, color = Green)
                                        }
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            Modifier.size(48.dp).clip(CircleShape).background(GreenLight),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = Green, modifier = Modifier.size(24.dp))
                                        }
                                        Text("Pilih Foto dari Galeri / Kamera", style = OlehKanType.BodyBold, color = Ink)
                                        Text("Format JPG, PNG atau WebP (Maks. 5MB)", style = OlehKanType.BodySmall, color = Muted)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, Line, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                                                Text("Galeri", style = OlehKanType.Caption, color = Muted)
                                            }
                                            Row(
                                                Modifier.clip(RoundedCornerShape(999.dp)).border(1.dp, Line, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                                                Text("Kamera", style = OlehKanType.Caption, color = Muted)
                                            }
                                        }
                                    }
                                }
                            }
                            state.pickedImageName?.let {
                                Text("Foto baru: $it (akan di-upload saat Simpan)", style = OlehKanType.BodySmall, color = Muted)
                            }
                            state.imageError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = OlehKanType.Body)
                            }
                        }
                        // --- Nama produk ---
                        OutlinedTextField(
                            value = name,
                            onValueChange = viewModel::onNameChange,
                            label = { Text("Nama Produk Oleh-Oleh") },
                            placeholder = { Text("Contoh: Geplak Bantul Aneka Warna") },
                            singleLine = true,
                            enabled = !state.isSaving,
                            isError = state.nameError != null,
                            supportingText = { state.nameError?.let { Text(it) } },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // --- Kategori ---
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                        ) {
                            val selectedName = state.categories.find { it.id == categoryId }?.name.orEmpty()
                            OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Kategori") },
                                placeholder = { Text("Pilih kelompok kategori...") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                isError = state.categoryError != null,
                                supportingText = { state.categoryError?.let { Text(it) } },
                                shape = fieldShape,
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                            ) {
                                state.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.onCategorySelected(category.id)
                                            categoryExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        // --- Harga & Stok ---
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = viewModel::onPriceChange,
                                label = { Text("Harga Jual (Rp)") },
                                placeholder = { Text("35.000") },
                                singleLine = true,
                                enabled = !state.isSaving,
                                isError = state.priceError != null,
                                supportingText = { state.priceError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = fieldShape,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = stockText,
                                onValueChange = viewModel::onStockChange,
                                label = { Text("Jumlah Stok") },
                                placeholder = { Text("25") },
                                singleLine = true,
                                enabled = !state.isSaving,
                                isError = state.stockError != null,
                                supportingText = { state.stockError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Text("item", style = OlehKanType.Caption, color = Muted) },
                                shape = fieldShape,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // --- Deskripsi ---
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = description,
                                onValueChange = viewModel::onDescriptionChange,
                                label = { Text("Deskripsi Singkat Rasa/Keistimewaan") },
                                placeholder = { Text("Jelaskan aroma rempah, kelembutan tekstur, resep warisan atau daya tahan produk...") },
                                enabled = !state.isSaving,
                                minLines = 3,
                                shape = fieldShape,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                "Tips: Sebutkan ketahanan masa kadaluarsa untuk produk oleh-oleh.",
                                style = OlehKanType.Caption, color = Muted,
                            )
                        }
                        // --- Aktif / nonaktif ---
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Produk aktif (tampil di katalog)", style = OlehKanType.Body)
                            Switch(
                                checked = isActive,
                                onCheckedChange = viewModel::onIsActiveChange,
                                enabled = !state.isSaving,
                            )
                        }
                        state.errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = OlehKanType.Body)
                        }
                        Button(
                            onClick = { viewModel.save(productUuid) },
                            enabled = !state.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = White),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(
                                when {
                                    state.isUploading -> "Mengunggah foto…"
                                    state.isSaving -> "Menyimpan…"
                                    isAdd -> "Terbitkan ke Etalase Toko"
                                    else -> "Simpan Perubahan"
                                },
                            )
                        }
                    }
                }
                if (productUuid != null) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !state.isSaving && !state.isDeleting,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Hapus Produk", color = MaterialTheme.colorScheme.error) }
                    Text(
                        "Tips: produk yang sudah dipakai transaksi sebaiknya dinonaktifkan (matikan switch di atas), bukan dihapus.",
                        style = OlehKanType.BodySmall, color = Muted,
                    )
                }
            }
        }
    }
}

/** Border garis putus-putus, seperti kotak upload di mockup. */
private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
    )
}
