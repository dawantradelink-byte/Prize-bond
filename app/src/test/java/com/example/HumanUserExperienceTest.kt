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
    kotlinx.coroutines.runBlocking {
      app.repository.ensureInitialDataLoaded()
    }
    composeTestRule.waitForIdle()

    // 1. Human user opens app: Verifies App Header, Live Upcoming Draw Countdown Clock
    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()
    composeTestRule.onNodeWithTag("upcoming_draw_countdown_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("LIVE COUNTDOWN").assertIsDisplayed()
    composeTestRule.onNodeWithText("DAYS").assertIsDisplayed()
    composeTestRule.onNodeWithText("SECS").assertIsDisplayed()

    // 2. Human user clicks "Add" tab on bottom navigation bar
    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    // 3. Human user is now on Add Bonds screen; sees "Single", "Series Range", "Bulk Paste", "OCR Scan 📷" tabs
    composeTestRule.onNodeWithTag("tab_single_bond").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_series_range").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_bulk_paste").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_ocr_scan").assertIsDisplayed()

    // 4. Human enters a real 100 Tk prize bond number: "0428319" (1st prize winner in 120th Draw)
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextClearance()
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextInput("0428319")
    composeTestRule.waitForIdle()

    // 5. Human scrolls to submit button and taps it
    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_single_bond_button"))
    composeTestRule.onNodeWithTag("submit_single_bond_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()

    // 6. App navigates back to My Bonds; human verifies bond card displays
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0428319"))
    composeTestRule.onNodeWithText("0428319").assertIsDisplayed()

    // 7. Human user switches to "Draws" screen to inspect the official archive
    composeTestRule.onNodeWithTag("nav_draw_results").performClick()
    composeTestRule.waitForIdle()

    // Human verifies the upcoming countdown clock is displayed at the top of the draws screen too
    composeTestRule.onNodeWithTag("upcoming_draw_countdown_card").assertIsDisplayed()

    // 8. Human views Draw Results archive and taps Draw #120
    composeTestRule.onNodeWithTag("draw_results_list").performScrollToNode(hasTestTag("draw_item_120"))
    composeTestRule.onNodeWithTag("draw_item_120").assertIsDisplayed()
    composeTestRule.onNodeWithTag("draw_item_120").performClick()
    composeTestRule.waitForIdle()

    // 9. Human user returns to My Bonds and sees their winning bond intact
    composeTestRule.onNodeWithTag("nav_my_bonds").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0428319"))
    composeTestRule.onNodeWithText("0428319").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_SeriesRangeAndBulkImport() {
    composeTestRule.waitForIdle()

    // 1. Navigate to Add Bonds
    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    // 2. Switch to Series Range tab
    composeTestRule.onNodeWithTag("tab_series_range").performClick()
    composeTestRule.waitForIdle()

    // 3. Enter Range 0123400 to 0123403 (4 bonds)
    composeTestRule.onNodeWithTag("input_range_start").performTextClearance()
    composeTestRule.onNodeWithTag("input_range_start").performTextInput("0123400")
    composeTestRule.onNodeWithTag("input_range_end").performTextClearance()
    composeTestRule.onNodeWithTag("input_range_end").performTextInput("0123403")
    composeTestRule.waitForIdle()

    // 4. Scroll to submit button and click it
    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_range_button"))
    composeTestRule.onNodeWithTag("submit_range_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()

    // 5. Verify range bonds were saved and display in portfolio
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0123400"))
    composeTestRule.onNodeWithText("0123400").assertIsDisplayed()

    // 6. Human tests Bulk Paste method
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

    // Verify 2nd prize winner 0876124 exists in portfolio
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0876124"))
    composeTestRule.onNodeWithText("0876124").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_QuickCheckNavigation() {
    val app = composeTestRule.activity.application as PrizeBondApp
    kotlinx.coroutines.runBlocking {
      app.repository.ensureInitialDataLoaded()
    }
    composeTestRule.waitForIdle()

    // 1. Switch to Quick Check
    composeTestRule.onNodeWithTag("nav_quick_check").performClick()
    composeTestRule.waitForIdle()

    // 2. Verify Quick Check screen is displayed
    composeTestRule.onNodeWithText("Quick Prize Bond Checker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_button").assertIsDisplayed()

    // 3. Human checks a winning bond number: "0428319" (1st prize winner in 120th Draw)
    composeTestRule.onNodeWithTag("quick_check_input").performTextClearance()
    composeTestRule.onNodeWithTag("quick_check_input").performTextInput("0428319")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("quick_check_button").performClick()
    composeTestRule.waitForIdle()

    // 4. Human verifies instant celebration card appears
    composeTestRule.onNodeWithTag("quick_check_list").performScrollToNode(hasTestTag("quick_check_win_card"))
    composeTestRule.onNodeWithTag("quick_check_win_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("WINNER DETECTED!").assertIsDisplayed()
    composeTestRule.onNodeWithText("1st Prize (৳ 6,00,000)").assertIsDisplayed()

    // 5. Human tests a non-winning number: "0000001"
    composeTestRule.onNodeWithTag("quick_check_list").performScrollToNode(hasTestTag("quick_check_input"))
    composeTestRule.onNodeWithTag("quick_check_input").performTextClearance()
    composeTestRule.onNodeWithTag("quick_check_input").performTextInput("0000001")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("quick_check_button").performClick()
    composeTestRule.waitForIdle()

    // 6. Human verifies clean informative non-winner notification
    composeTestRule.onNodeWithTag("quick_check_list").performScrollToNode(hasText("No Prize Found for 0000001"))
    composeTestRule.onNodeWithText("No Prize Found for 0000001").assertIsDisplayed()
  }

  @Test
  fun testHumanJourney_SearchFilterAndDeleteBond() {
    composeTestRule.waitForIdle()

    // 1. Add a single test bond "0555555"
    composeTestRule.onNodeWithTag("nav_add_bonds").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("input_single_bond_number").performTextClearance()
    composeTestRule.onNodeWithTag("input_single_bond_number").performTextInput("0555555")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("add_bonds_list").performScrollToNode(hasTestTag("submit_single_bond_button"))
    composeTestRule.onNodeWithTag("submit_single_bond_button").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("my_bonds_list")).fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.waitForIdle()

    // 2. Human tests search field: Searches for "0555"
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("search_bonds_input"))
    composeTestRule.onNodeWithTag("search_bonds_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_bonds_input").performTextInput("0555")
    composeTestRule.waitForIdle()

    // 3. Verifies "0555555" matches and is displayed
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasText("0555555"))
    composeTestRule.onNodeWithText("0555555").assertIsDisplayed()

    // 4. Human clears search
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("search_bonds_input"))
    composeTestRule.onNodeWithTag("search_bonds_input").performTextClearance()
    composeTestRule.waitForIdle()

    // 5. Human deletes the bond
    composeTestRule.onNodeWithTag("my_bonds_list").performScrollToNode(hasTestTag("delete_bond_0555555"))
    composeTestRule.onNodeWithTag("delete_bond_0555555").performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onAllNodes(hasTestTag("bond_card_0555555")).fetchSemanticsNodes().isEmpty()
    }
    composeTestRule.waitForIdle()

    // 6. Verifies bond card is gone
    composeTestRule.onAllNodes(hasTestTag("bond_card_0555555")).assertCountEquals(0)
  }

  @Test
  fun testHumanJourney_DarkModeToggle() {
    composeTestRule.waitForIdle()

    // Human taps dark mode toggle in top header
    composeTestRule.onNodeWithTag("dark_mode_toggle_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dark_mode_toggle_button").performClick()
    composeTestRule.waitForIdle()

    // Verifies app header remains visible and responsive
    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()

    // Toggle back to light mode
    composeTestRule.onNodeWithTag("dark_mode_toggle_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("PrizeBond BD").assertIsDisplayed()
  }
}
