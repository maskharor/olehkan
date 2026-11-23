package com.olehkan.olehkan.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminRoute(isAdmin: Boolean, onBack: () -> Unit, content: @Composable () -> Unit) {
    if (isAdmin) content() else Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Halaman ini hanya tersedia untuk admin toko.")
        Button(onClick = onBack) { Text("Kembali") }
    }
}
