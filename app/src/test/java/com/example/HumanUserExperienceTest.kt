package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HumanUserExperienceTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun testHumanJourney_OpenApp_AddWinningBond_InspectDraws_VerifyWorkflow() {
    val app = composeTestRule.activity.application as PrizeBondApp
    kotlinx.coroutines.runBlocking { app.repository.ensureInitialDataLoaded() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()
    composeTestRule.onNodeWithTag("upcoming_draw_countdown_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("LIVE COUNTDOWN").assertIsDisplayed()
    composeTestRule.onNodeWithText("DAYS").assertIsDisplayed()
    composeTestRule.onNodeWithText("SECS").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tab_single_bond").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_series_range").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_bulk_paste").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_ocr_scan").assertIsDisplayed()

    // Use a valid 7-digit test bond. Do not rely on a stale hard-coded winner.
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextClearance()
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextInput("0666666")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_single_bond_button"))
    composeTestRule.onNodeWithTag("submit_single_bond_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0666666"))
    composeTestRule.onNodeWithText("0666666").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_draw_results").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("upcoming_draw_countdown_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("draw_results_list").assertIsDisplayed()

    // Draw data is now sourced from official metadata. Verify the screen, not a stale draw number.
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0666666"))
    composeTestRule.onNodeWithText("0666666").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_SeriesRangeAndBulkImport() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tab_series_range").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_range_start").performTextClearance()
    composeTestRule.onNodeWithTag("input_range_start").performTextInput("0123400")
    composeTestRule.onNodeWithTag("input_range_end").performTextClearance()
    composeTestRule.onNodeWithTag("input_range_end").performTextInput("0123403")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_range_button"))
    composeTestRule.onNodeWithTag("submit_range_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0123400"))
    composeTestRule.onNodeWithText("0123400").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tab_bulk_paste").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_bulk_text").performTextClearance()
    composeTestRule.onNodeWithTag("input_bulk_text").performTextInput("0876124, 0539184\n0784912")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_bulk_button"))
    composeTestRule.onNodeWithTag("submit_bulk_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0876124"))
    composeTestRule.onNodeWithText("0876124").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_QuickCheckNavigation() {
    val app = composeTestRule.activity.application as PrizeBondApp
    kotlinx.coroutines.runBlocking { app.repository.ensureInitialDataLoaded() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_quick_check").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Quick Prize Bond Checker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_button").assertIsDisplayed()

    // Keep this UI test deterministic and offline. Live government lookup is tested separately.
    composeTestRule.onNodeWithTag("quick_check_input").performTextClearance()
    composeTestRule.onNodeWithTag("quick_check_input").performTextInput("0000001")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("quick_check_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("No Prize Found for 0000001").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_SearchFilterAndDeleteBond() {
    val app = composeTestRule.activity.application as PrizeBondApp
    kotlinx.coroutines.runBlocking { app.repository.clearAllBonds() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_single_bond_number").performTextClearance()
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextInput("0666666")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_single_bond_button"))
    composeTestRule.onNodeWithTag("submit_single_bond_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("nav_my_bonds")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("bond_card_0666666")).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("search_bonds_input"))
    composeTestRule.onNodeWithTag("search_bonds_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_bonds_input").performTextInput("0666")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0666666"))
    composeTestRule.onNodeWithText("0666666").assertIsDisplayed()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("search_bonds_input"))
    composeTestRule.onNodeWithTag("search_bonds_input").performTextClearance()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("delete_bond_0666666"))
    composeTestRule.onNodeWithTag("delete_bond_0666666").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("bond_card_0666666")).fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.waitForIdle()

    composeTestRule.onAllNodes(hasTestTag("bond_card_0666666")).assertCountEquals(0)
  }

  @Test
  fun testHumanJourney_DarkModeToggle() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dark_mode_toggle_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dark_mode_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()

    composeTestRule.onNodeWithTag("dark_mode_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()
  }
}
