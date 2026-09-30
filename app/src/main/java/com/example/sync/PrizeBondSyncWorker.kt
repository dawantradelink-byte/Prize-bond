package com.example.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.BangladeshBankRemoteDataSource
import com.example.data.remote.RemoteSyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrizeBondSyncWorker(
  private val context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    try {
      val database = AppDatabase.getInstance(context)
      val dao = database.prizeBondDao()
      val remoteDataSource = BangladeshBankRemoteDataSource()

      // Fetch remote results
      val syncResult = remoteDataSource.fetchLatestDrawResults()
      if (syncResult is RemoteSyncResult.Success) {
        dao.insertDrawResults(syncResult.draws)
        dao.insertWinningNumbers(syncResult.winningNumbers)

        val latestDraw = syncResult.draws.firstOrNull()

        // Cross-match user bonds
        val userBonds = dao.getAllUserBondsSnapshot()
        var winnersCount = 0
        var totalPrizeWon = 0L

        for (bond in userBonds) {
          val matches = dao.getWinningMatchesForNumber(bond.bondNumber)
          if (matches.isNotEmpty()) {
            winnersCount++
            totalPrizeWon += matches.sumOf { it.prizeAmount }
          }
        }

        if (winnersCount > 0) {
          NotificationHelper.sendWinningAlertNotification(
            context = context,
            winnerCount = winnersCount,
            totalAmountWon = totalPrizeWon,
            drawNumber = latestDraw?.drawNumber ?: 120
          )
        } else if (latestDraw != null) {
          NotificationHelper.sendDrawSyncNotification(
            context = context,
            drawNumber = latestDraw.drawNumber,
            drawDate = latestDraw.drawDate
          )
        }

        Result.success()
      } else {
        Result.retry()
      }
    } catch (e: Exception) {
      Result.failure()
    }
  }
}
