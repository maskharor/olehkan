package com.olehkan.olehkan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.data.repository.AdminDashboardRepository
import com.olehkan.olehkan.ui.screens.admin.AdminDashboardContent
import com.olehkan.olehkan.ui.screens.admin.AdminDashboardState
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.io.IOException
import com.olehkan.olehkan.data.repository.AdminInventoryProduct
import com.olehkan.olehkan.ui.screens.admin.CategoryInventoryState

/** Respons dan state terisolasi; tidak menghubungi Supabase nyata. */
class AdminDashboardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun acceptsZeroAndLargeServerCounts() = runBlocking {
        val repository = AdminDashboardRepository { """{"category_count":0,"buyer_count":3000000000}""" }
        val overview = repository.loadOverview()
        assertEquals(0L, overview.categoryCount)
        assertEquals(3000000000L, overview.buyerCount)
    }

    @Test fun rejectsIncompleteOverviewRatherThanInventingCounts() = runBlocking {
        try {
            AdminDashboardRepository { """{"category_count":1}""" }.loadOverview()
            fail("Missing buyer count must fail")
        } catch (_: IllegalStateException) { }
    }

    @Test fun rejectsNegativeCounts() = runBlocking {
        try {
            AdminDashboardRepository { """{"category_count":-1,"buyer_count":0}""" }.loadOverview()
            fail("Negative count must fail")
        } catch (_: IllegalStateException) { }
    }

    @Test fun searchOpensMatchingCategoryEvenWhenOverviewFails() {
        var selected: String? = null
        compose.setContent {
            var state by remember { mutableStateOf(AdminDashboardState(
                loading = false, categoriesLoading = false,
                error = "Ringkasan tidak tersedia",
                categories = listOf(Category("a", "Makanan"), Category("b", "Minuman")),
            )) }
            OlehKanTheme {
                AdminDashboardContent(
                    UserProfile("admin", "Admin Uji", "admin@example.test", UserRole.ADMIN),
                    state, {}, {}, { selected = it }, {}, { state = state.copy(query = it) },
                    {}, {}, {}, {},
                )
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasSetTextAction())
        compose.onNode(hasSetTextAction()).performTextInput("  MINUM  ")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Minuman"))
        compose.onNodeWithText("Minuman").performClick()
        compose.runOnIdle { assertEquals("b", selected) }
        compose.onNodeWithText("Makanan").assertDoesNotExist()
    }

    @Test fun importRequiresConfirmationAndCanBeCancelled() {
        var imported = false
        compose.setContent {
            var state by remember { mutableStateOf(AdminDashboardState(
                loading = false, categoriesLoading = false, toolsExpanded = true,
            )) }
            OlehKanTheme {
                AdminDashboardContent(
                    UserProfile("admin", "Admin Uji", "admin@example.test", UserRole.ADMIN),
                    state, {}, {}, {}, {}, {}, {},
                    { state = state.copy(confirmImport = true) },
                    { state = state.copy(confirmImport = false) },
                    { imported = true; state = state.copy(confirmImport = false) },
                )
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Impor kategori lokal"))
        compose.onNodeWithText("Impor kategori lokal").performClick()
        compose.onNodeWithText("Impor kategori lokal?").assertIsDisplayed()
        compose.onNodeWithText("Batal").performClick()
        compose.runOnIdle { assertFalse(imported) }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Impor kategori lokal"))
        compose.onNodeWithText("Impor kategori lokal").performClick()
        compose.onNodeWithText("Impor", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertTrue(imported) }
    }

    @Test fun inventoryPageKeepsMoneyPrecisionAndUsesStablePagination() = runBlocking {
        var requested = ""
        val rows = JSONArray()
        repeat(21) { index ->
            rows.put(JSONObject().put("id", "product-$index").put("name", "Produk $index")
                .put("price", "45000.25").put("stock", index).put("image_url", JSONObject.NULL)
                .put("is_active", true))
        }
        val repository = AdminDashboardRepository({ "{}" }, { path -> requested = path; rows.toString() })
        val page = repository.loadProducts("category-a", 20)
        assertEquals(20, page.products.size)
        assertTrue(page.hasMore)
        assertEquals(BigDecimal("45000.25"), page.products.first().price)
        assertNull(page.products.first().imageUrl)
        assertTrue(requested.contains("category_id=eq.category-a"))
        assertTrue(requested.contains("order=created_at.asc,id.asc"))
        assertTrue(requested.contains("limit=21&offset=20"))
    }

    @Test fun emptyInventoryIsNotAnErrorAndHasNoNextPage() = runBlocking {
        val page = AdminDashboardRepository({ "{}" }, { "[]" }).loadProducts("a")
        assertTrue(page.products.isEmpty())
        assertFalse(page.hasMore)
    }

    @Test fun negativeStockIsRejected() = runBlocking {
        val response = """[{"id":"p","name":"Produk","price":100,"stock":-1,"is_active":true}]"""
        try {
            AdminDashboardRepository({ "{}" }, { response }).loadProducts("a")
            fail("Invalid stock should fail")
        } catch (_: IllegalStateException) { }
    }

    @Test fun inventoryFailureIsNotConvertedIntoEmptySuccess() = runBlocking {
        try {
            AdminDashboardRepository({ "{}" }, { throw IOException("Offline test") }).loadProducts("a")
            fail("Network error must remain an error")
        } catch (_: IOException) { }
    }

    @Test fun categoryCanExpandCollapseAndOpenAddForm() {
        var addClicked = false
        compose.setContent {
            var state by remember { mutableStateOf(AdminDashboardState(
                loading = false, categoriesLoading = false, categories = listOf(Category("a", "Minuman")),
                inventory = mapOf("a" to CategoryInventoryState(loaded = true, products = listOf(
                    AdminInventoryProduct("p", "Wedang Jahe", BigDecimal("15000"), 12, null, true)
                ))),
            )) }
            OlehKanTheme {
                AdminDashboardContent(
                    UserProfile("admin", "Admin Uji", "admin@example.test", UserRole.ADMIN),
                    state, {}, {}, {}, {}, {}, {}, {}, {}, {},
                    onAddCategory = { addClicked = true },
                    onToggleCategory = { id -> state = state.copy(
                        expandedCategoryId = if (state.expandedCategoryId == id) null else id) },
                )
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Minuman"))
        compose.onNodeWithText("Minuman").performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Wedang Jahe"))
        compose.onNodeWithText("Tersedia 12 unit").assertExists()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Minuman"))
        compose.onNodeWithText("Minuman").performClick()
        compose.onNodeWithText("Wedang Jahe").assertDoesNotExist()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Tambah kategori"))
        compose.onNodeWithText("Tambah kategori").performClick()
        compose.runOnIdle { assertTrue(addClicked) }
    }

    @Test fun failedInventoryOffersRetryInsteadOfDummyProducts() {
        var retried: String? = null
        compose.setContent {
            OlehKanTheme {
                AdminDashboardContent(
                    UserProfile("admin", "Admin Uji", "admin@example.test", UserRole.ADMIN),
                    AdminDashboardState(loading = false, categoriesLoading = false,
                        categories = listOf(Category("a", "Minuman")), expandedCategoryId = "a",
                        inventory = mapOf("a" to CategoryInventoryState(error = "Produk tidak dapat dibaca"))),
                    {}, {}, {}, {}, {}, {}, {}, {}, {},
                    onLoadProducts = { id, more -> if (!more) retried = id },
                )
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Coba lagi"))
        compose.onNodeWithText("Coba lagi").performClick()
        compose.runOnIdle { assertEquals("a", retried) }
    }

}
