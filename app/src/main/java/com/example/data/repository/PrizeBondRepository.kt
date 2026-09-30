package com.example.data.repository

import com.example.data.local.PrizeBondDao
import com.example.data.model.BondMatch
import com.example.data.model.BondSummaryStats
import com.example.data.model.DrawResult
import com.example.data.model.UserBond
import com.example.data.model.WinningNumber
import com.example.data.remote.BangladeshBankRemoteDataSource
import com.example.util.PrizeBondNumberValidator
import com.example.data.remote.RemoteSyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PrizeBondRepository(
  private val dao: PrizeBondDao,
  private val remoteDataSource: BangladeshBankRemoteDataSource = BangladeshBankRemoteDataSource()
) {

  // Reactive stream of user bonds combined with their winning status across all draws
  val userBondsWithMatches: Flow<List<BondMatch>> = combine(
    dao.getAllUserBonds(),
    dao.getAllWinningNumbers()
  ) { bonds, winningList ->
    val winningByNumber = winningList.groupBy { it.winningNumber }
    bonds.map { bond ->
      val matches = winningByNumber[bond.bondNumber] ?: emptyList()
      BondMatch(bond = bond, winningDraws = matches)
    }
  }.flowOn(Dispatchers.Default)

  val allDrawResults: Flow<List<DrawResult>> = dao.getAllDrawResults()

  // Reactive summary statistics
  val summaryStats: Flow<BondSummaryStats> = userBondsWithMatches.combine(dao.getAllDrawResults()) { bondMatches, draws ->
    val totalCount = bondMatches.size
    val totalInvestment = totalCount * 100L // 100 Taka per bond
    val winners = bondMatches.filter { it.isWinner }
    val totalWinnings = winners.sumOf { it.totalPrizeWon }
    val latestDraw = draws.firstOrNull()?.drawNumber ?: 120
    val lastSync = draws.firstOrNull()?.lastSyncedTimestamp ?: System.currentTimeMillis()

    BondSummaryStats(
      totalBondsCount = totalCount,
      totalInvestmentAmount = totalInvestment,
      winningBondsCount = winners.size,
      totalPrizeAmountWon = totalWinnings,
      latestDrawNumber = latestDraw,
      lastSyncTime = lastSync
    )
  }.flowOn(Dispatchers.Default)

  suspend fun ensureInitialDataLoaded() = withContext(Dispatchers.IO) {
    // No guessed or stale prize-bond results are seeded on first launch.
    // Official data must be fetched and verified before it enters Room.
  }

  suspend fun syncWithRemote(): RemoteSyncResult = withContext(Dispatchers.IO) {
    val result = remoteDataSource.fetchLatestDrawResults()
    if (result is RemoteSyncResult.Success) {
      if (result.draws.isNotEmpty()) {
        dao.insertDrawResults(result.draws)
      }
      if (result.winningNumbers.isNotEmpty()) {
        dao.insertWinningNumbers(result.winningNumbers)
      }
    }
    result
  }

  suspend fun addSingleBond(number: String, series: String = "All", note: String = ""): Result<Long> = withContext(Dispatchers.IO) {
    val cleaned = PrizeBondNumberValidator.validate(number)
      ?: return@withContext Result.failure(
        IllegalArgumentException("Bond number must contain exactly 7 digits (e.g. 0123456)")
      )
    val bond = UserBond(
      bondNumber = cleaned,
      series = series.ifBlank { "All" },
      note = note
    )
    try {
      val id = dao.insertBond(bond)
      if (id == -1L) {
        Result.failure(IllegalArgumentException("This bond number and series are already saved."))
      } else {
        Result.success(id)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun addSeriesRange(startNumber: String, endNumber: String, series: String = "All"): Result<Int> = withContext(Dispatchers.IO) {
    val cleanedStart = PrizeBondNumberValidator.validate(startNumber)
      ?: return@withContext Result.failure(IllegalArgumentException("Start number must contain exactly 7 digits"))
    val cleanedEnd = PrizeBondNumberValidator.validate(endNumber)
      ?: return@withContext Result.failure(IllegalArgumentException("End number must contain exactly 7 digits"))

    val startLong = cleanedStart.toLongOrNull() ?: return@withContext Result.failure(IllegalArgumentException("Invalid start number"))
    val endLong = cleanedEnd.toLongOrNull() ?: return@withContext Result.failure(IllegalArgumentException("Invalid end number"))

    if (endLong < startLong) {
      return@withContext Result.failure(IllegalArgumentException("End number cannot be smaller than Start number"))
    }

    val count = endLong - startLong + 1
    if (count > 500) {
      return@withContext Result.failure(IllegalArgumentException("Maximum 500 bonds can be added in a single range"))
    }

    val bonds = (startLong..endLong).map { num ->
      val formatted = num.toString().padStart(7, '0')
      UserBond(
        bondNumber = formatted,
        series = series.ifBlank { "All" },
        note = "Range $cleanedStart - $cleanedEnd"
      )
    }

    val insertedIds = dao.insertBonds(bonds).count { it != -1L }
    Result.success(insertedIds)
  }

  suspend fun addBulkText(rawText: String, defaultSeries: String = "All"): Result<Int> = withContext(Dispatchers.IO) {
    // Delimit by comma, newline, spaces, semicolons
    val tokens = rawText.split(Regex("[,\\s;]+")).map { it.trim() }.filter { it.isNotBlank() }
    val validBonds = mutableListOf<UserBond>()

    for (token in tokens) {
      val validNumber = PrizeBondNumberValidator.validate(token)
      if (validNumber != null) {
        validBonds.add(
          UserBond(
            bondNumber = validNumber,
            series = defaultSeries.ifBlank { "All" }
          )
        )
      }
    }

    if (validBonds.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("No valid bond numbers found. Enter 7-digit numbers separated by commas or new lines."))
    }

    val uniqueBonds = validBonds.distinctBy { it.bondNumber to it.series }
    val insertedIds = dao.insertBonds(uniqueBonds)
    Result.success(insertedIds.size)
  }

  suspend fun insertUserBonds(bonds: List<UserBond>): Int = withContext(Dispatchers.IO) {
    dao.insertBonds(bonds).count { it != -1L }
  }

  suspend fun deleteBond(id: Long) = withContext(Dispatchers.IO) {
    dao.deleteBond(id)
  }

  suspend fun clearAllBonds() = withContext(Dispatchers.IO) {
    dao.deleteAllBonds()
  }

  suspend fun quickCheckNumber(number: String): List<WinningNumber> = withContext(Dispatchers.IO) {
    val formatted = formatBondNumber(number)
    if (formatted.length != 7) return@withContext emptyList()
    dao.getWinningMatchesForNumber(formatted)
  }

  fun getWinningNumbersForDraw(drawNumber: Int): Flow<List<WinningNumber>> {
    return dao.getWinningNumbersForDraw(drawNumber)
  }

}
