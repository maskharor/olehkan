package com.olehkan.olehkan.ui.screens.admin.productform

import com.olehkan.olehkan.data.model.Category

/**
 * State untuk Form Tambah/Edit Produk (Fitur 2, MVVM).
 * Satu form untuk tambah (productUuid == null) dan edit,
 * mengikuti pola CategoryFormScreen / CategoryViewModel.
 */
data class AdminProductFormUiState(
    val isLoading: Boolean = true,
    val hasLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val categoryError: String? = null,
    val imageError: String? = null,
    val successMessage: String? = null,
    val categories: List<Category> = emptyList(),
    /** URL gambar yang sudah tersimpan di Supabase (mode edit). */
    val existingImageUrl: String = "",
    /** Nama file gambar yang baru dipilih (belum di-upload). */
    val pickedImageName: String? = null,
    /** Ada gambar baru yang dipilih dan siap di-upload saat Simpan. */
    val hasPickedImage: Boolean = false,
    /** Produk tidak ditemukan (mode edit, uuid tidak ada di server). */
    val productNotFound: Boolean = false,
)
