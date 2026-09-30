package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.PrizeBondRepository
import com.example.sync.NotificationHelper
import com.example.sync.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrizeBondApp : Application() {

  val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  lateinit var database: AppDatabase
    private set

  lateinit var repository: PrizeBondRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this

    database = AppDatabase.getInstance(this)
    repository = PrizeBondRepository(database.prizeBondDao())

    // Create notification channel
    NotificationHelper.createNotificationChannel(this)

    // Schedule background sync (wrapped safely for unit tests / test environments)
    try {
      WorkManagerScheduler.schedulePeriodicSync(this)
    } catch (_: Exception) {
      // Safe fallback for testing environments where default WorkManager factory is mocked
    }
  }

  companion object {
    lateinit var instance: PrizeBondApp
      private set
  }
}
