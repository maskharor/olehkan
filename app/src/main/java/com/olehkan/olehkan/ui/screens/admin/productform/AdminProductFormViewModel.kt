package com.olehkan.olehkan.ui.screens.admin.productform

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.olehkan.olehkan.data.network.BackendException
import com.olehkan.olehkan.data.repository.CategoryRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.SupabaseProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel Form Tambah/Edit Produk (Fitur 2, MVVM).
 *
 * Alur simpan, mengikuti dokumentasi database:
 * 1. Validasi input di ViewModel (bukan di UI).
 * 2. Kalau ada gambar baru: upload dulu ke Storage `product-images`
 *    via SupabaseProductRepository.uploadImage(), dapat public URL.
 * 3. Baru simpan produk ke tabel `products` dengan URL itu
 *    via ProductRepository.save(productUuid, ...).
 * Upload dan simpan adalah 2 operasi terpisah — kalau simpan gagal
 * setelah upload berhasil, file di Storage jadi file tidak terpakai.
 *
 * Field form disimpan di SavedStateHandle (seperti CategoryViewModel)
 * supaya tidak hilang saat rotasi layar.
 */
class AdminProductFormViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductFormUiState())
    val uiState = _uiState.asStateFlow()

    val name = savedStateHandle.getStateFlow("productName", "")
    val description = savedStateHandle.getStateFlow("productDescription", "")
    val priceText = savedStateHandle.getStateFlow("productPrice", "")
    val stockText = savedStateHandle.getStateFlow("productStock", "")
    val categoryId = savedStateHandle.getStateFlow("productCategoryId", "")
    val isActive = savedStateHandle.getStateFlow("productIsActive", true)

    /** Gambar baru yang dipilih pengguna, belum di-upload. */
    private var pickedImageUri: Uri? = null
    private var pickedImageBytes: ByteArray? = null
    private var pickedImageContentType: String = "image/jpeg"
    private var pickedImageFileName: String = ""

    fun prepareForm(productUuid: String?) {
        if (savedStateHandle.get<Boolean>("formInitialized") == true) return
        savedStateHandle["formInitialized"] = true
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                categoryRepository.refresh()
                val categories = categoryRepository.categories.value
                if (productUuid == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false, hasLoaded = true,
                            categories = categories,
                        )
                    }
                    return@launch
                }
                // Muat produk yang diedit: cari berdasarkan UUID.
                val products = productRepository.getProducts()
                val product = products.find { it.uuid == productUuid }
                if (product == null) {
                    _uiState.update {
                        it.copy(isLoading = false, hasLoaded = true, productNotFound = true, categories = categories)
                    }
                    return@launch
                }
                savedStateHandle["productName"] = product.name
                savedStateHandle["productDescription"] = product.description
                savedStateHandle["productPrice"] = product.price.toString()
                savedStateHandle["productStock"] = product.stock.toString()
                savedStateHandle["productCategoryId"] = product.categoryId
                savedStateHandle["productIsActive"] = product.isActive
                _uiState.update {
                    it.copy(
                        isLoading = false, hasLoaded = true,
                        categories = categories,
                        existingImageUrl = product.image,
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat form produk.")
                }
            }
        }
    }

    fun onNameChange(value: String) {
        savedStateHandle["productName"] = value
        _uiState.update { it.copy(nameError = null, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        savedStateHandle["productDescription"] = value
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPriceChange(value: String) {
        // Hanya angka.
        val filtered = value.filter { it.isDigit() }
        savedStateHandle["productPrice"] = filtered
        _uiState.update { it.copy(priceError = null, errorMessage = null) }
    }

    fun onStockChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        savedStateHandle["productStock"] = filtered
        _uiState.update { it.copy(stockError = null, errorMessage = null) }
    }

    fun onCategorySelected(id: String) {
        savedStateHandle["productCategoryId"] = id
        _uiState.update { it.copy(categoryError = null, errorMessage = null) }
    }

    fun onIsActiveChange(value: Boolean) {
        savedStateHandle["productIsActive"] = value
    }

    fun onImagePicked(uri: Uri, bytes: ByteArray, contentType: String, fileName: String) {
        if (bytes.isEmpty()) {
            _uiState.update { it.copy(imageError = "File gambar kosong.") }
            return
        }
        if (bytes.size > 5 * 1024 * 1024) {
            _uiState.update { it.copy(imageError = "Ukuran gambar maksimal 5 MB.") }
            return
        }
        // Bucket product-images hanya menerima JPEG/PNG/WebP.
        // Tolak format lain di sini dengan pesan yang jelas, jangan
        // tunggu server menolak dengan pesan generik.
        val normalizedType = when (contentType.lowercase().trim()) {
            "image/jpg" -> "image/jpeg"
            else -> contentType.lowercase().trim()
        }
        if (normalizedType !in setOf("image/jpeg", "image/png", "image/webp")) {
            _uiState.update {
                it.copy(imageError = "Format gambar tidak didukung (${contentType.ifBlank { "tidak diketahui" }}). Pilih foto JPG, PNG, atau WebP.")
            }
            return
        }
        pickedImageUri = uri
        pickedImageBytes = bytes
        pickedImageContentType = normalizedType
        pickedImageFileName = fileName.ifBlank { "product-${System.currentTimeMillis()}.jpg" }
        _uiState.update {
            it.copy(imageError = null, pickedImageName = pickedImageFileName, hasPickedImage = true)
        }
    }

    fun onImageRemoved() {
        pickedImageUri = null
        pickedImageBytes = null
        _uiState.update {
            it.copy(pickedImageName = null, hasPickedImage = false, existingImageUrl = "")
        }
    }

    fun save(productUuid: String?) {
        if (_uiState.value.isSaving || _uiState.value.isLoading || !_uiState.value.hasLoaded) return
        // --- Validasi ---
        var hasError = false
        if (name.value.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama produk wajib diisi.") }
            hasError = true
        }
        val price = priceText.value.toIntOrNull()
        if (price == null || price < 0) {
            _uiState.update { it.copy(priceError = "Harga wajib diisi dengan angka 0 atau lebih.") }
            hasError = true
        }
        val stock = stockText.value.toIntOrNull()
        if (stock == null || stock < 0) {
            _uiState.update { it.copy(stockError = "Stok wajib diisi dengan angka 0 atau lebih.") }
            hasError = true
        }
        if (categoryId.value.isBlank()) {
            _uiState.update { it.copy(categoryError = "Kategori wajib dipilih.") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // --- Langkah 1: upload gambar dulu (kalau ada yang baru) ---
                // Foto itu OPSIONAL: kalau upload gagal (misal Storage
                // menolak dengan HTTP 400), produk tetap disimpan tanpa
                // foto baru, bukan gagal total. Alasan upload dicatat
                // di pesan sukses supaya tetap kelihatan.
                var imageUrl: String? = _uiState.value.existingImageUrl.takeIf { it.isNotBlank() }
                var uploadWarning: String? = null
                val bytes = pickedImageBytes
                if (bytes != null && productRepository is SupabaseProductRepository) {
                    _uiState.update { it.copy(isUploading = true) }
                    // Nama file unik dari tipe konten, bukan nama asli galeri:
                    // nama galeri bisa sama dengan file lain ("images.jpg")
                    // atau berformat lain dari isinya.
                    val extension = when (pickedImageContentType) {
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> "jpg"
                    }
                    val uploadFileName = "product-${System.currentTimeMillis()}.$extension"
                    try {
                        imageUrl = productRepository.uploadImage(
                            fileName = uploadFileName,
                            bytes = bytes,
                            contentType = pickedImageContentType,
                        )
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        val detail = if (e is BackendException) {
                            "${e.message ?: "Upload gagal."} (HTTP ${e.statusCode}, ${e.errorCode})"
                        } else {
                            e.message ?: "Upload gagal."
                        }
                        uploadWarning = "Foto gagal di-upload ($detail). Produk disimpan tanpa foto baru."
                    }
                    _uiState.update { it.copy(isUploading = false) }
                }
                // --- Langkah 2: simpan produk dengan URL gambar itu ---
                productRepository.save(
                    productUuid = productUuid,
                    name = name.value.trim(),
                    description = description.value.trim().takeIf { it.isNotBlank() },
                    price = price!!,
                    stock = stock!!,
                    categoryId = categoryId.value,
                    imageUrl = imageUrl,
                    isActive = isActive.value,
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = (if (productUuid == null) "Produk berhasil ditambahkan."
                        else "Produk berhasil diperbarui.") +
                            (uploadWarning?.let { " $it" } ?: ""),
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Tampilkan kode HTTP asli dari server, jangan hanya pesan
                // generik SupabaseHttp — tanpa kode ini penyebab upload
                // gagal tidak bisa dibedakan (400, 403, 404, dsb).
                val detail = if (e is BackendException) {
                    "${e.message ?: "Upload gagal."} (HTTP ${e.statusCode}, ${e.errorCode})"
                } else {
                    e.message ?: "Produk gagal disimpan."
                }
                _uiState.update {
                    it.copy(
                        isSaving = false, isUploading = false,
                        errorMessage = detail,
                    )
                }
            }
        }
    }

    fun delete(productUuid: String, onDeleted: () -> Unit) {
        if (_uiState.value.isDeleting || _uiState.value.isSaving) return
        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                productRepository.delete(productUuid)
                _uiState.update {
                    it.copy(isDeleting = false, successMessage = "Produk berhasil dihapus.")
                }
                onDeleted()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isDeleting = false, errorMessage = e.message ?: "Produk gagal dihapus.")
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /** URL gambar untuk preview: gambar baru yang dipilih, kalau tidak ada pakai yang tersimpan. */
    fun previewImageUri(): Uri? = pickedImageUri
}

class AdminProductFormViewModelFactory(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass.isAssignableFrom(AdminProductFormViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return AdminProductFormViewModel(
            productRepository,
            categoryRepository,
            extras.createSavedStateHandle(),
        ) as T
    }
}
