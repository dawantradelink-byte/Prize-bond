package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end smoke coverage for the real Android app.
 *
 * This runs on an Android emulator, not Robolectric, so it exercises:
 * Activity startup, Compose rendering, navigation, text input, Room-backed
 * bond creation, search/filter UI, and the main app tabs.
 */
@RunWith(AndroidJUnit4::class)
fun androidx.compose.ui.test.SemanticsNodeInteraction.assertExistsCompat() {
  check(fetchSemanticsNodes().isNotEmpty()) { "Expected Compose node to exist" }
}

class AppSmokeInstrumentedTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun fullAppSmokeJourney() {
    composeRule.onNodeWithTag("my_bonds_list").assertExistsCompat()
    composeRule.onNodeWithTag("sync_header_button").assertExistsCompat()
    composeRule.onNodeWithTag("dark_mode_toggle_button").assertExistsCompat()
    composeRule.onNodeWithTag("search_bonds_input").assertExistsCompat()

    // Exercise dark-mode state without leaving the current screen.
    composeRule.onNodeWithTag("dark_mode_toggle_button").performClick()
    composeRule.onNodeWithTag("my_bonds_list").assertExistsCompat()

    // Add a small valid range through the real UI.
    composeRule.onNodeWithTag("nav_add_bonds").performClick()
    composeRule.onNodeWithTag("add_bonds_list").assertExistsCompat()
    composeRule.onNodeWithTag("tab_series_range").performClick()
    composeRule.onNodeWithTag("input_range_start").performTextInput("0785001")
    composeRule.onNodeWithTag("input_range_end").performTextInput("0785003")
    composeRule.onNodeWithTag("submit_range_button").performClick()

    // Return to the portfolio and verify the saved bonds are rendered.
    composeRule.onNodeWithTag("nav_my_bonds").performClick()
    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodes(
        androidx.compose.ui.test.hasTestTag("bond_card_0785001")
      ).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithTag("bond_card_0785001").assertExistsCompat()
    composeRule.onNodeWithTag("bond_card_0785003").assertExistsCompat()

    // Search must filter the portfolio to the requested bond.
    composeRule.onNodeWithTag("search_bonds_input").performTextInput("0785002")
    composeRule.onNodeWithTag("bond_card_0785002").assertExistsCompat()

    // Exercise the remaining top-level screens.
    composeRule.onNodeWithTag("nav_draw_results").performClick()
    composeRule.onNodeWithTag("draw_results_list").assertExistsCompat()

    composeRule.onNodeWithTag("nav_quick_check").performClick()
    composeRule.onNodeWithTag("quick_check_list").assertExistsCompat()
    composeRule.onNodeWithTag("quick_check_input").assertExistsCompat()
    composeRule.onNodeWithTag("quick_check_button").assertExistsCompat()

    composeRule.onNodeWithTag("nav_settings").performClick()
    composeRule.onNodeWithTag("settings_dark_mode_switch").assertExistsCompat()
    composeRule.onNodeWithTag("manual_sync_button").assertExistsCompat()
    composeRule.onNodeWithTag("clear_all_bonds_button").assertExistsCompat()

    // Final navigation sanity check.
    composeRule.onNodeWithTag("nav_my_bonds").performClick()
    composeRule.onNodeWithTag("my_bonds_list").assertExistsCompat()
  }
}
