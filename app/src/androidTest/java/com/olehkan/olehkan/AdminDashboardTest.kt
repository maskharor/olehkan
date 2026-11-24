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
}
