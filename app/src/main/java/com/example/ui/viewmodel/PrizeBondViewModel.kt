package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BondMatch
import com.example.data.model.BondSummaryStats
import com.example.data.model.DrawResult
import com.example.data.model.WinningNumber
import com.example.data.model.UserBond
import com.example.data.remote.RemoteSyncResult
import com.example.data.repository.PrizeBondRepository
import com.example.ocr.PrizeBondOcrEngine
import com.example.util.PrizeBondNumberValidator
import com.example.ocr.ScannedBondCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppNavTab {
  MY_BONDS,
  ADD_BONDS,
  DRAW_RESULTS,
  QUICK_CHECK,
  SETTINGS
}

data class QuickCheckResult(
  val queryNumber: String = "",
  val matches: List<WinningNumber> = emptyList(),
  val hasChecked: Boolean = false
)

data class UiOperationState(
  val isLoading: Boolean = false,
  val userMessage: String? = null,
  val isError: Boolean = false
)

class PrizeBondViewModel(
  private val repository: PrizeBondRepository
) : ViewModel() {

  private val _currentTab = MutableStateFlow(AppNavTab.MY_BONDS)
  val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

  private val _isDarkMode = MutableStateFlow(false)
  val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

  private val _filterWinnersOnly = MutableStateFlow(false)
  val filterWinnersOnly: StateFlow<Boolean> = _filterWinnersOnly.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _operationState = MutableStateFlow(UiOperationState())
  val operationState: StateFlow<UiOperationState> = _operationState.asStateFlow()

  private val _quickCheckState = MutableStateFlow(QuickCheckResult())
  val quickCheckState: StateFlow<QuickCheckResult> = _quickCheckState.asStateFlow()

  private val _selectedDraw = MutableStateFlow<DrawResult?>(null)
  val selectedDraw: StateFlow<DrawResult?> = _selectedDraw.asStateFlow()

  private val _selectedDrawNumbers = MutableStateFlow<List<WinningNumber>>(emptyList())
  val selectedDrawNumbers: StateFlow<List<WinningNumber>> = _selectedDrawNumbers.asStateFlow()

  private val _scannedCandidates = MutableStateFlow<List<ScannedBondCandidate>>(emptyList())
  val scannedCandidates: StateFlow<List<ScannedBondCandidate>> = _scannedCandidates.asStateFlow()

  private val _isOcrScanning = MutableStateFlow(false)
  val isOcrScanning: StateFlow<Boolean> = _isOcrScanning.asStateFlow()

  val userBondsWithMatches: StateFlow<List<BondMatch>> = combine(
    repository.userBondsWithMatches,
    _filterWinnersOnly,
    _searchQuery
  ) { matches, winnersOnly, query ->
    var filtered = matches
    if (winnersOnly) {
      filtered = filtered.filter { it.isWinner }
    }
    if (query.isNotBlank()) {
      filtered = filtered.filter {
        it.bond.bondNumber.contains(query) || it.bond.series.contains(query, ignoreCase = true)
      }
    }
    filtered
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = emptyList()
  )

  val summaryStats: StateFlow<BondSummaryStats> = repository.summaryStats.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = BondSummaryStats()
  )

  val drawResults: StateFlow<List<DrawResult>> = repository.allDrawResults.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = emptyList()
  )

  init {
    viewModelScope.launch {
      repository.ensureInitialDataLoaded()
    }
  }

  fun setTab(tab: AppNavTab) {
    _currentTab.value = tab
    _operationState.update { it.copy(userMessage = null) }
  }

  fun toggleDarkMode() {
    _isDarkMode.value = !_isDarkMode.value
  }

  fun setFilterWinnersOnly(enabled: Boolean) {
    _filterWinnersOnly.value = enabled
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun clearMessage() {
    _operationState.update { it.copy(userMessage = null) }
  }

  fun addSingleBond(number: String, series: String, note: String) {
    viewModelScope.launch {
      _operationState.value = UiOperationState(isLoading = true)
      val result = repository.addSingleBond(number, series, note)
      if (result.isSuccess) {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = "Bond ${PrizeBondNumberValidator.normalize(number)} ($series) saved successfully!",
          isError = false
        )
        _currentTab.value = AppNavTab.MY_BONDS
      } else {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = result.exceptionOrNull()?.message ?: "Failed to add bond",
          isError = true
        )
      }
    }
  }

  fun addSeriesRange(startNumber: String, endNumber: String, series: String) {
    viewModelScope.launch {
      _operationState.value = UiOperationState(isLoading = true)
      val result = repository.addSeriesRange(startNumber, endNumber, series)
      if (result.isSuccess) {
        val count = result.getOrDefault(0)
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = "Added series of $count bonds ($series: $startNumber to $endNumber)!",
          isError = false
        )
        _currentTab.value = AppNavTab.MY_BONDS
      } else {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = result.exceptionOrNull()?.message ?: "Failed to add range",
          isError = true
        )
      }
    }
  }

  fun addBulkText(rawText: String, series: String) {
    viewModelScope.launch {
      _operationState.value = UiOperationState(isLoading = true)
      val result = repository.addBulkText(rawText, series)
      if (result.isSuccess) {
        val count = result.getOrDefault(0)
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = "Imported $count bonds successfully!",
          isError = false
        )
        _currentTab.value = AppNavTab.MY_BONDS
      } else {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = result.exceptionOrNull()?.message ?: "Failed to import bulk bonds",
          isError = true
        )
      }
    }
  }

  fun deleteBond(id: Long) {
    viewModelScope.launch {
      repository.deleteBond(id)
      _operationState.value = UiOperationState(
        userMessage = "Bond removed",
        isError = false
      )
    }
  }

  fun clearAllBonds() {
    viewModelScope.launch {
      repository.clearAllBonds()
      _operationState.value = UiOperationState(
        userMessage = "All saved bonds cleared",
        isError = false
      )
    }
  }

  fun syncNow() {
    viewModelScope.launch {
      _operationState.value = UiOperationState(isLoading = true)
      val result = repository.syncWithRemote()
      if (result is RemoteSyncResult.Success) {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = result.message,
          isError = false
        )
      } else {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = (result as RemoteSyncResult.Error).error,
          isError = true
        )
      }
    }
  }

  fun quickCheck(number: String) {
    viewModelScope.launch {
      val matches = repository.quickCheckNumber(number)
      _quickCheckState.value = QuickCheckResult(
        queryNumber = number,
        matches = matches,
        hasChecked = true
      )
    }
  }

  fun clearQuickCheck() {
    _quickCheckState.value = QuickCheckResult()
  }

  fun selectDraw(draw: DrawResult) {
    _selectedDraw.value = draw
    viewModelScope.launch {
      repository.getWinningNumbersForDraw(draw.drawNumber).collect { numbers ->
        _selectedDrawNumbers.value = numbers
      }
    }
  }

  fun scanBitmap(bitmap: android.graphics.Bitmap) {
    viewModelScope.launch {
      _isOcrScanning.value = true
      try {
        val candidates = PrizeBondOcrEngine.scanBitmap(bitmap)
        _scannedCandidates.value = candidates
        if (candidates.isEmpty()) {
          _operationState.value = UiOperationState(
            userMessage = "No 7-digit bond numbers detected in the image. Please take a clearer, well-lit photo of the bond.",
            isError = true
          )
        } else {
          _operationState.value = UiOperationState(
            userMessage = "Recognized ${candidates.size} bond number(s) from image!",
            isError = false
          )
        }
      } catch (e: Exception) {
        _operationState.value = UiOperationState(
          userMessage = "OCR processing error: ${e.message}",
          isError = true
        )
      } finally {
        _isOcrScanning.value = false
      }
    }
  }

  fun scanUri(context: android.content.Context, uri: android.net.Uri) {
    viewModelScope.launch {
      _isOcrScanning.value = true
      try {
        val candidates = PrizeBondOcrEngine.scanUri(context, uri)
        _scannedCandidates.value = candidates
        if (candidates.isEmpty()) {
          _operationState.value = UiOperationState(
            userMessage = "No 7-digit bond numbers detected in the image. Please take a clearer, well-lit photo of the bond.",
            isError = true
          )
        } else {
          _operationState.value = UiOperationState(
            userMessage = "Recognized ${candidates.size} bond number(s) from image!",
            isError = false
          )
        }
      } catch (e: Exception) {
        _operationState.value = UiOperationState(
          userMessage = "OCR processing error: ${e.message}",
          isError = true
        )
      } finally {
        _isOcrScanning.value = false
      }
    }
  }

  fun addRealtimeDetectedCandidates(newCandidates: List<ScannedBondCandidate>) {
    if (newCandidates.isEmpty()) return
    _scannedCandidates.update { current ->
      val existingNumbers = current.map { it.number }.toSet()
      val toAdd = newCandidates.filter { it.number !in existingNumbers }
      if (toAdd.isNotEmpty()) {
        current + toAdd
      } else {
        current
      }
    }
  }

  fun toggleCandidateSelection(id: String) {
    _scannedCandidates.update { list ->
      list.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }
    }
  }

  fun removeCandidate(id: String) {
    _scannedCandidates.update { list -> list.filter { it.id != id } }
  }

  fun clearScannedCandidates() {
    _scannedCandidates.value = emptyList()
  }

  fun importSelectedScannedBonds() {
    val selected = _scannedCandidates.value.filter { it.isSelected }
    if (selected.isEmpty()) return

    viewModelScope.launch {
      _operationState.value = UiOperationState(isLoading = true)
      val bonds = selected.map { candidate ->
        UserBond(
          bondNumber = PrizeBondNumberValidator.requireValid(candidate.number),
          series = candidate.series.ifBlank { "All" },
          note = "Scanned with ML Kit OCR"
        )
      }
      try {
        val importedCount = repository.insertUserBonds(bonds)
        _scannedCandidates.value = emptyList()
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = if (importedCount == bonds.size) {
            "Imported $importedCount scanned bond(s) into your portfolio!"
          } else {
            "Imported $importedCount new bond(s). Duplicate bonds were skipped."
          },
          isError = false
        )
        _currentTab.value = AppNavTab.MY_BONDS
      } catch (e: Exception) {
        _operationState.value = UiOperationState(
          isLoading = false,
          userMessage = "Failed to save scanned bonds: ${e.message}",
          isError = true
        )
      }
    }
  }
}

class PrizeBondViewModelFactory(
  private val repository: PrizeBondRepository
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(PrizeBondViewModel::class.java)) {
      return PrizeBondViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
