package com.olehkan.olehkan

import android.content.Context
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.LocalCategoryRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.ui.navigation.Screen
import com.olehkan.olehkan.ui.navigation.categoryNavigation
import com.olehkan.olehkan.ui.screens.category.CategoryViewModelFactory
import kotlinx.coroutines.runBlocking
import java.util.UUID
import org.junit.After
import org.junit.Rule
import org.junit.Test

/** Graph kategori dengan penyimpanan lokal terisolasi, bukan backend produksi. */
class CategoryNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferenceName = "nav_test_${UUID.randomUUID()}"

    @After fun cleanup() { context.deleteSharedPreferences(preferenceName) }

    private fun launch(isAdmin: Boolean, names: List<String> = emptyList()) {
        val products = object : ProductRepository {
            override suspend fun getProducts(): List<Product> = emptyList()
            override suspend fun getProductById(id: Int): Product? = null
        }
        val repository = LocalCategoryRepository(
            context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE), products)
        runBlocking { names.forEach { repository.save(null, it) } }
        val factory = CategoryViewModelFactory(repository)
        compose.setContent {
            OlehKanTheme {
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

    private fun tap(text: String) {
        compose.waitForIdle()
        if (compose.onAllNodesWithTag("category-list").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("category-list").performScrollToNode(hasText(text))
        } else {
            compose.onNodeWithText(text).performScrollTo()
        }
        compose.onNodeWithText(text).performClick()
    }

    @Test fun adminCanNavigateCreateEditAndDeleteCategory() {
        launch(isAdmin = true)
        waitFor("Kelola Kategori")
        tap("Tambah Kategori")
        waitFor("Nama kategori")
        compose.onNodeWithText("Nama kategori").performScrollTo().performTextInput("Kategori Uji")
        tap("Simpan Kategori")
        waitFor("Kelola Kategori")
        tap("Kategori Uji")
        tap("Edit Kategori")
        waitFor("Nama kategori")
        compose.onNodeWithText("Nama kategori").performScrollTo().performTextClearance()
        compose.onNodeWithText("Nama kategori").performTextInput("Kategori Baru")
        tap("Simpan Kategori")
        waitFor("Detail Kategori")
        tap("Hapus Kategori")
        compose.onNodeWithText("Batal").performClick()
        compose.onNodeWithText("Kategori Baru").assertExists()
        tap("Hapus Kategori")
        compose.onNodeWithText("Hapus", useUnmergedTree = true).performClick()
        waitFor("Kelola Kategori")
        compose.onNodeWithTag("category-list").performScrollToNode(hasText("Belum ada kategori. Tambahkan kategori pertama."))
        compose.onNodeWithText("Belum ada kategori. Tambahkan kategori pertama.").assertIsDisplayed()
    }

    @Test fun buyerCannotOpenCategoryRouteDirectly() {
        launch(isAdmin = false)
        compose.onNodeWithText("Halaman ini hanya tersedia untuk admin toko.").assertIsDisplayed()
        compose.onNodeWithText("Tambah Kategori").assertDoesNotExist()
    }

    @Test fun searchAndFilterCanBeReset() {
        launch(true, listOf("Minuman", "Makanan"))
        compose.onNodeWithTag("category-list").performScrollToNode(hasSetTextAction())
        compose.onNode(hasSetTextAction()).performTextInput("minum")
        compose.onNodeWithTag("category-list").performScrollToNode(hasText("Minuman"))
        compose.onNodeWithText("Makanan").assertDoesNotExist()
        tap("Dilindungi")
        tap("Reset filter")
        tap("Makanan")
        waitFor("Detail Kategori")
    }

    @Test fun unsavedFormRequiresDiscardConfirmation() {
        launch(true)
        tap("Tambah Kategori")
        compose.onNodeWithText("Nama kategori").performScrollTo().performTextInput("Belum disimpan")
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Buang perubahan?").assertIsDisplayed()
        compose.onNodeWithText("Lanjut edit").performClick()
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Buang perubahan", useUnmergedTree = true).performClick()
        waitFor("Kelola Kategori")
    }
}
