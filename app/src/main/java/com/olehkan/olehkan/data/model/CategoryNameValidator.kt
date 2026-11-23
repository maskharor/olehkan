package com.olehkan.olehkan.data.model

object CategoryNameValidator {
    const val MAX_LENGTH = 40

    fun normalize(name: String): String = name.trim().replace(Regex("\\s+"), " ")

    fun error(name: String, existingNames: List<String> = emptyList()): String? {
        val normalized = normalize(name)
        return when {
            normalized.isBlank() -> "Nama kategori wajib diisi."
            normalized.length > MAX_LENGTH -> "Nama kategori maksimal $MAX_LENGTH karakter."
            normalized.equals("Semua", ignoreCase = true) ->
                "Nama Semua digunakan untuk filter aplikasi."
            existingNames.any { normalize(it).equals(normalized, ignoreCase = true) } ->
                "Nama kategori sudah digunakan."
            else -> null
        }
    }
}
