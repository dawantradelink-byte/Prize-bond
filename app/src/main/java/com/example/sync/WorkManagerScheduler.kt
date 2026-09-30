package com.example.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

  private const val PERIODIC_SYNC_WORK_NAME = "prizebond_periodic_sync"
  private const val IMMEDIATE_SYNC_WORK_NAME = "prizebond_immediate_sync"

  fun schedulePeriodicSync(context: Context) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val syncRequest = PeriodicWorkRequestBuilder<PrizeBondSyncWorker>(12, TimeUnit.HOURS)
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      PERIODIC_SYNC_WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      syncRequest
    )
  }

  fun triggerImmediateSync(context: Context) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val oneTimeRequest = OneTimeWorkRequestBuilder<PrizeBondSyncWorker>()
      .setConstraints(constraints)
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      IMMEDIATE_SYNC_WORK_NAME,
      ExistingWorkPolicy.REPLACE,
      oneTimeRequest
    )
  }
}
