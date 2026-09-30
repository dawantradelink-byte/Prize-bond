package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.model.BondMatch
import com.example.data.model.BondSummaryStats
import com.example.data.model.UserBond
import com.example.ui.screens.MyBondsScreen
import com.example.ui.screens.QuickCheckScreen
import com.example.ui.viewmodel.QuickCheckResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ScreenInteractionTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun quickCheckScreen_displaysCheckedNoPrizeState_withoutNetwork() {
    composeTestRule.setContent {
      QuickCheckScreen(
        quickCheckResult = QuickCheckResult(
          queryNumber = "0000001",
          matches = emptyList(),
          hasChecked = true
        ),
        onCheckNumber = {},
        onClear = {},
        isDarkMode = false
      )
    }

    composeTestRule.onNodeWithText("Quick Prize Bond Checker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_check_button").assertIsDisplayed()
    composeTestRule.onNodeWithText(
      "No prize was returned by the government result service for this number. You can check again after future draws."
    ).assertIsDisplayed()
  }

  @Test
  fun myBondsScreen_displaysBondAndDeleteCallbackWorks() {
    val bond = UserBond(
      id = 1L,
      bondNumber = "0666666",
      series = "All",
      note = ""
    )
    var deletedId: Long? = null

    composeTestRule.setContent {
      MyBondsScreen(
        bonds = listOf(BondMatch(bond = bond, winningDraws = emptyList())),
        stats = BondSummaryStats(
          totalBondsCount = 1,
          totalInvestmentAmount = 100L,
          winningBondsCount = 0,
          totalPrizeAmountWon = 0L,
          latestDrawNumber = 0,
          lastSyncTime = 0L
        ),
        searchQuery = "",
        onSearchQueryChange = {},
        filterWinnersOnly = false,
        onFilterWinnersChange = {},
        onAddBondsClick = {},
        onDeleteBond = { deletedId = it },
        isDarkMode = false
      )
    }

    composeTestRule.onNodeWithText("0666666").assertIsDisplayed()
    composeTestRule.onNodeWithTag("search_bonds_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("delete_bond_0666666").performClick()

    assertEquals(1L, deletedId)
  }
}
