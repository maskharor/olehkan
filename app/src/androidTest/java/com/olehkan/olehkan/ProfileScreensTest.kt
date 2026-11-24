package com.olehkan.olehkan

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AccountUiState
import com.olehkan.olehkan.ui.screens.profile.EditProfileContent
import com.olehkan.olehkan.ui.screens.profile.ProfileContent
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** State dan callback terisolasi; tidak login, logout, atau mengubah akun Supabase nyata. */
class ProfileScreensTest {
    @get:Rule val compose = createComposeRule()
    private fun profile(role: UserRole) = UserProfile("profile-test", "Nama Awal", "akun@example.test", role)

    private fun tapLazy(text: String) {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        compose.onNodeWithText(text).performClick()
    }

    @Test fun adminMenuOpensDashboardAndDoesNotShowBuyerMenu() {
        var opened = false
        compose.setContent {
            OlehKanTheme {
                ProfileContent(profile(UserRole.ADMIN), AccountUiState(),
                    { opened = true }, {}, {}, {}, {}, {}, {})
            }
        }
        tapLazy("Dashboard Admin")
        compose.runOnIdle { assertTrue(opened) }
        compose.onNodeWithText("Ulasan Saya").assertDoesNotExist()
    }

    @Test fun buyerMenuDoesNotOfferAdministrativeActions() {
        var reviewed = false
        compose.setContent {
            OlehKanTheme {
                ProfileContent(profile(UserRole.BUYER), AccountUiState(),
                    {}, { reviewed = true }, {}, {}, {}, {}, {})
            }
        }
        tapLazy("Ulasan Saya")
        compose.runOnIdle { assertTrue(reviewed) }
        compose.onNodeWithText("Dashboard Admin").assertDoesNotExist()
        compose.onNodeWithText("Kelola Kategori").assertDoesNotExist()
    }

    @Test fun copyEmailAndConfirmedLogoutDispatchTheirActions() {
        var copies = 0
        var logouts = 0
        compose.setContent {
            OlehKanTheme {
                ProfileContent(profile(UserRole.ADMIN), AccountUiState(),
                    {}, {}, {}, {}, {}, { logouts++ }, { copies++ })
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Salin email"))
        compose.onNodeWithContentDescription("Salin email").performClick()
        compose.runOnIdle { assertEquals(1, copies) }
        tapLazy("Logout")
        compose.onNodeWithText("Keluar dari akun?").assertIsDisplayed()
        compose.onNodeWithText("Batal").performClick()
        compose.runOnIdle { assertEquals(0, logouts) }
        tapLazy("Logout")
        compose.onNodeWithText("Keluar", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertEquals(1, logouts) }
    }

    @Test fun editValidatesNameAndOnlySavesChangedTrimmedValue() {
        var saved: String? = null
        compose.setContent {
            var state by remember { mutableStateOf(AccountUiState(editName = "Nama Awal")) }
            OlehKanTheme {
                EditProfileContent(profile(UserRole.ADMIN), state,
                    { state = state.copy(editName = it) }, { saved = it }, {})
            }
        }
        compose.onNodeWithText("Simpan Profil").performScrollTo().assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performScrollTo().performTextClearance()
        compose.onNodeWithText("Simpan Profil").performScrollTo().assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("a".repeat(101))
        compose.onNodeWithText("Simpan Profil").performScrollTo().assertIsNotEnabled()
        compose.onNode(hasSetTextAction()).performScrollTo().performTextClearance()
        compose.onNode(hasSetTextAction()).performTextInput("  Nama Baru  ")
        compose.onNodeWithText("Simpan Profil").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("Nama Baru", saved) }
    }

    @Test fun unsavedProfileCanContinueEditingOrDiscard() {
        var backed = false
        compose.setContent {
            OlehKanTheme {
                EditProfileContent(profile(UserRole.BUYER), AccountUiState(editName = "Nama Baru"),
                    {}, {}, { backed = true })
            }
        }
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Buang perubahan?").assertIsDisplayed()
        compose.onNodeWithText("Lanjut edit").performClick()
        compose.runOnIdle { assertFalse(backed) }
        compose.onNodeWithContentDescription("Kembali").performClick()
        compose.onNodeWithText("Buang perubahan", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertTrue(backed) }
    }
}
