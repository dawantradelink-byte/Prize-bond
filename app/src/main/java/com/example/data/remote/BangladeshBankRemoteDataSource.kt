package com.example.data.remote

import com.example.data.model.DrawResult
import com.example.data.model.WinningNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
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

  suspend fun checkNumber(number: String): List<WinningNumber> = withContext(Dispatchers.IO) {
    try {
      val body = FormBody.Builder().add("from", number).build()
      val request = Request.Builder()
        .url("https://prizebond.ird.gov.bd/hybrid_action_b2e.php")
        .header("User-Agent", "PrizeBondBD-Android/1.0")
        .header("Accept", "text/html")
        .post(body)
        .build()
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        parsePbrisResults(response.body?.string().orEmpty(), number)
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun parsePbrisResults(html: String, number: String): List<WinningNumber> {
    val normalizedHtml = normalizeBengaliDigits(html)
    val rowPattern = Regex("""<tr>\s*<td>[^<]+</td>\s*<td>([^<]+)</td>\s*<td>(\d+)</td>\s*<td>(\d{4}-\d{2}-\d{2})</td>""", RegexOption.IGNORE_CASE)
    return rowPattern.findAll(normalizedHtml).mapNotNull { match ->
      val tier = when (match.groupValues[1].trim()) {
        "1ম", "1st" -> 1
        "2য়", "2nd" -> 2
        "3য়", "3rd" -> 3
        "4র্থ", "4th" -> 4
        "5ম", "5th" -> 5
        else -> return@mapNotNull null
      }
      val amount = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
      val date = match.groupValues[3]
      WinningNumber(
        drawNumber = 0,
        prizeTier = tier,
        prizeAmount = amount,
        winningNumber = number,
        prizeDescription = when (tier) { 1 -> "1st Prize"; 2 -> "2nd Prize"; 3 -> "3rd Prize"; 4 -> "4th Prize"; else -> "5th Prize" },
        drawDate = date
      )
    }.toList()
  }

  private fun normalizeBengaliDigits(input: String): String {
    val bengali = "০১২৩৪৫৬৭৮৯"
    val latin = "0123456789"
    return input.map { ch ->
      val index = bengali.indexOf(ch)
      if (index >= 0) latin[index] else ch
    }.joinToString("")
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
