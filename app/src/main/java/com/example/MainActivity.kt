package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopHeader
import com.example.ui.components.CyberMeshBackground
import com.example.ui.screens.AddBondsScreen
import com.example.ui.screens.DrawResultsScreen
import com.example.ui.screens.MyBondsScreen
import com.example.ui.screens.QuickCheckScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PrizeBondTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.PrizeBondViewModel
import com.example.ui.viewmodel.PrizeBondViewModelFactory

class MainActivity : ComponentActivity() {

  private val viewModel: PrizeBondViewModel by viewModels {
    PrizeBondViewModelFactory((application as PrizeBondApp).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

      PrizeBondTheme(darkTheme = isDarkMode) {
        PrizeBondMainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun PrizeBondMainApp(viewModel: PrizeBondViewModel) {
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
  val userBonds by viewModel.userBondsWithMatches.collectAsStateWithLifecycle()
  val stats by viewModel.summaryStats.collectAsStateWithLifecycle()
  val drawResults by viewModel.drawResults.collectAsStateWithLifecycle()
  val selectedDraw by viewModel.selectedDraw.collectAsStateWithLifecycle()
  val selectedDrawNumbers by viewModel.selectedDrawNumbers.collectAsStateWithLifecycle()
  val filterWinnersOnly by viewModel.filterWinnersOnly.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val operationState by viewModel.operationState.collectAsStateWithLifecycle()
  val quickCheckState by viewModel.quickCheckState.collectAsStateWithLifecycle()
  val scannedCandidates by viewModel.scannedCandidates.collectAsStateWithLifecycle()
  val isOcrScanning by viewModel.isOcrScanning.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  // Show user feedback snackbar
  LaunchedEffect(operationState.userMessage) {
    operationState.userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearMessage()
    }
  }

  // Handle back button on sub-screens
  BackHandler(enabled = selectedDraw != null || currentTab != AppNavTab.MY_BONDS) {
    if (selectedDraw != null) {
      viewModel.selectDraw(selectedDraw!!.copy(drawNumber = -1)) // or back out
    } else {
      viewModel.setTab(AppNavTab.MY_BONDS)
    }
  }

  CyberMeshBackground(isDarkMode = isDarkMode) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        AppTopHeader(
          isDarkMode = isDarkMode,
          onToggleDarkMode = { viewModel.toggleDarkMode() },
          onSyncClick = { viewModel.syncNow() },
          isSyncing = operationState.isLoading
        )
      },
      bottomBar = {
        AppBottomNav(
          currentTab = currentTab,
          onTabSelected = { tab ->
            if (tab != currentTab) {
              viewModel.setTab(tab)
            }
          },
          winnerCount = stats.winningBondsCount,
          isDarkMode = isDarkMode
        )
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
        ) {
          when (currentTab) {
            AppNavTab.MY_BONDS -> {
              MyBondsScreen(
                bonds = userBonds,
                stats = stats,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                filterWinnersOnly = filterWinnersOnly,
                onFilterWinnersChange = { viewModel.setFilterWinnersOnly(it) },
                onAddBondsClick = { viewModel.setTab(AppNavTab.ADD_BONDS) },
                onDeleteBond = { viewModel.deleteBond(it) },
                isDarkMode = isDarkMode
              )
            }

            AppNavTab.ADD_BONDS -> {
              AddBondsScreen(
                onAddSingle = { number, series, note -> viewModel.addSingleBond(number, series, note) },
                onAddRange = { start, end, series -> viewModel.addSeriesRange(start, end, series) },
                onAddBulk = { rawText, series -> viewModel.addBulkText(rawText, series) },
                onScanBitmap = { viewModel.scanBitmap(it) },
                onScanUri = { context, uri -> viewModel.scanUri(context, uri) },
                scannedCandidates = scannedCandidates,
                isOcrScanning = isOcrScanning,
                onToggleCandidate = { viewModel.toggleCandidateSelection(it) },
                onRemoveCandidate = { viewModel.removeCandidate(it) },
                onImportScannedBonds = { viewModel.importSelectedScannedBonds() },
                onClearScanned = { viewModel.clearScannedCandidates() },
                onAddRealtimeBonds = { viewModel.addRealtimeDetectedCandidates(it) },
                operationState = operationState,
                isDarkMode = isDarkMode
              )
            }

            AppNavTab.DRAW_RESULTS -> {
              DrawResultsScreen(
                draws = drawResults,
                selectedDraw = if (selectedDraw?.drawNumber != -1) selectedDraw else null,
                selectedDrawNumbers = selectedDrawNumbers,
                onSelectDraw = { viewModel.selectDraw(it) },
                onClearSelectedDraw = { viewModel.selectDraw(drawResults.first().copy(drawNumber = -1)) },
                isDarkMode = isDarkMode
              )
            }

            AppNavTab.QUICK_CHECK -> {
              QuickCheckScreen(
                quickCheckResult = quickCheckState,
                onCheckNumber = { viewModel.quickCheck(it) },
                onClear = { viewModel.clearQuickCheck() },
                isDarkMode = isDarkMode
              )
            }

            AppNavTab.SETTINGS -> {
              SettingsScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onSyncNow = { viewModel.syncNow() },
                onClearAllBonds = { viewModel.clearAllBonds() },
                operationState = operationState
              )
            }
          }
        }
      }
    }
  }
}
