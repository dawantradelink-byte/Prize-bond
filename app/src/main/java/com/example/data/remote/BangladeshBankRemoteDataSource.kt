package com.example.data.remote

import com.example.data.model.DrawResult
import com.example.data.model.WinningNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class BangladeshBankRemoteDataSource {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  private val officialPrizeBondUrl = "https://www.bb.org.bd/en/index.php/Investfacility/prizebond"

  suspend fun fetchLatestDrawResults(): RemoteSyncResult = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url(officialPrizeBondUrl)
        .header("User-Agent", "PrizeBondBD-Android/1.0")
        .header("Accept", "text/html")
        .build()
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext RemoteSyncResult.Error("Bangladesh Bank sync failed: HTTP ${response.code}")
        val html = response.body?.string().orEmpty()
        val draws = parseDrawMetadata(html)
        if (draws.isEmpty()) return@withContext RemoteSyncResult.Error("Bangladesh Bank page was reached, but no verifiable draw records were found.")
        RemoteSyncResult.Success(
          draws = draws,
          winningNumbers = emptyList(),
          message = "Bangladesh Bank draw metadata synchronized. Winning-number data was not changed because it could not be verified from the official page."
        )
      }
    } catch (e: Exception) {
      RemoteSyncResult.Error("Bangladesh Bank sync unavailable: ${e.message ?: "network error"}")
    }
  }

  private fun parseDrawMetadata(html: String): List<DrawResult> {
    val pattern = Regex("""(?i)(\d+)(?:st|nd|rd|th)\s+Draw\s*\((\d{1,2})(?:st|nd|rd|th)?\s+([A-Za-z]+)\s+(\d{4})\)""")
    val monthNumbers = mapOf(
      "January" to 1, "February" to 2, "March" to 3, "April" to 4,
      "May" to 5, "June" to 6, "July" to 7, "August" to 8,
      "September" to 9, "October" to 10, "November" to 11, "December" to 12
    )
    return pattern.findAll(html).mapNotNull { match ->
      val drawNumber = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
      val day = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
      val month = match.groupValues[3].replaceFirstChar { it.uppercase() }
      val year = match.groupValues[4].toIntOrNull() ?: return@mapNotNull null
      if (month !in monthNumbers || drawNumber <= 0 || day !in 1..31) return@mapNotNull null
      DrawResult(
        drawNumber = drawNumber,
        drawDate = "%02d %s %04d".format(day, month, year),
        drawPlace = "Dhaka",
        seriesCount = 0,
        validUntil = "",
        isOfficial = true
      )
    }.distinctBy { it.drawNumber }.sortedByDescending { it.drawNumber }.toList()
  }
}

sealed class RemoteSyncResult {
  data class Success(
    val draws: List<DrawResult>,
    val winningNumbers: List<WinningNumber>,
    val message: String
  ) : RemoteSyncResult()
  data class Error(val error: String) : RemoteSyncResult()
}
