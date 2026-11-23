package com.olehkan.olehkan

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.LocalCategoryRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.ui.navigation.Screen
import com.olehkan.olehkan.ui.navigation.categoryNavigation
import com.olehkan.olehkan.ui.screens.category.CategoryViewModelFactory
import java.util.UUID
import org.junit.After
import org.junit.Rule
import org.junit.Test

/** Menguji navigasi UI dengan penyimpanan lokal terisolasi, bukan backend produksi. */
class CategoryNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferenceName = "nav_test_${UUID.randomUUID()}"

    @After fun cleanup() { context.deleteSharedPreferences(preferenceName) }

    private fun launch(isAdmin: Boolean) {
        val products = object : ProductRepository {
            override suspend fun getProducts(): List<Product> = emptyList()
            override suspend fun getProductById(id: Int): Product? = null
        }
        val repository = LocalCategoryRepository(
            context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE), products)
        val factory = CategoryViewModelFactory(repository)
        compose.setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = Screen.Categories.route) {
                    categoryNavigation(navController, factory, isAdmin)
                }
            }
        }
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun adminCanNavigateCreateEditAndDeleteCategory() {
        launch(isAdmin = true)
        waitFor("Belum ada kategori. Tambahkan kategori pertama.")
        compose.onNodeWithText("Tambah Kategori").performClick()
        waitFor("Nama kategori")
        compose.onNodeWithText("Nama kategori").performTextInput("Kategori Uji")
        compose.onNodeWithText("Simpan Kategori").performClick()
        waitFor("Kategori Uji")
        compose.onNodeWithText("Kategori Uji").performClick()
        waitFor("Edit Kategori")
        compose.onNodeWithText("Edit Kategori").performClick()
        waitFor("Nama kategori")
        compose.onNodeWithText("Nama kategori").performTextClearance()
        compose.onNodeWithText("Nama kategori").performTextInput("Kategori Baru")
        compose.onNodeWithText("Simpan Kategori").performClick()
        waitFor("Kategori Baru")
        compose.onNodeWithText("Hapus Kategori").performClick()
        compose.onNodeWithText("Batal").performClick()
        compose.onNodeWithText("Kategori Baru").assertIsDisplayed()
        compose.onNodeWithText("Hapus Kategori").performClick()
        compose.onNodeWithText("Hapus", useUnmergedTree = true).performClick()
        waitFor("Belum ada kategori. Tambahkan kategori pertama.")
    }

    @Test fun buyerCannotOpenCategoryRouteDirectly() {
        launch(isAdmin = false)
        compose.onNodeWithText("Halaman ini hanya tersedia untuk admin toko.").assertIsDisplayed()
        compose.onNodeWithText("Tambah Kategori").assertDoesNotExist()
    }
}
