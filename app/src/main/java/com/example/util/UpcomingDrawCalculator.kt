package com.example.util

import java.util.Calendar
import java.util.TimeZone

data class UpcomingDrawInfo(
  val drawNumber: Int,
  val drawDateFormatted: String,
  val targetEpochMs: Long,
  val venue: String = "Dhaka",
  val seriesCount: Int = 84,
  val firstPrizeAmount: Long = 600000L,
  val totalPrizeTiers: Int = 46
)

data class CountdownTimeRemaining(
  val days: Long,
  val hours: Long,
  val minutes: Long,
  val seconds: Long,
  val isDrawDay: Boolean = false,
  val totalRemainingSeconds: Long
)

object UpcomingDrawCalculator {

  private val DHAKA_TIMEZONE = TimeZone.getTimeZone("GMT+6")

  /**
   * Calculates the next upcoming Bangladesh Bank quarterly draw.
   *
   * Official Schedule:
   * - 31 January (Draw 121 in 2026, 125 in 2027...)
   * - 30 April   (Draw 122 in 2026, 126 in 2027...)
   * - 31 July    (Draw 123 in 2026, 127 in 2027...)
   * - 31 October (Draw 120 was 2025, 124 in 2026...)
   */
  fun getNextUpcomingDraw(currentTimeMs: Long = System.currentTimeMillis()): UpcomingDrawInfo {
    val cal = Calendar.getInstance(DHAKA_TIMEZONE).apply {
      timeInMillis = currentTimeMs
    }

    val currentYear = cal.get(Calendar.YEAR)

    // Quarterly draw dates (month is 0-indexed: 0=Jan, 3=Apr, 6=Jul, 9=Oct)
    // Draw 120 base is 31 Oct 2025 (year 2025, month 9, day 31)
    val drawSchedule = listOf(
      Triple(0, 31, "January"),
      Triple(3, 30, "April"),
      Triple(6, 31, "July"),
      Triple(9, 31, "October")
    )

    // Check draws for current year and next year to find the earliest future one
    for (year in currentYear..(currentYear + 2)) {
      for ((monthIndex, dayOfMonth, monthName) in drawSchedule) {
        val drawCal = Calendar.getInstance(DHAKA_TIMEZONE).apply {
          set(Calendar.YEAR, year)
          set(Calendar.MONTH, monthIndex)
          set(Calendar.DAY_OF_MONTH, dayOfMonth)
          set(Calendar.HOUR_OF_DAY, 10) // 10:00 AM draw ceremony
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }

        if (drawCal.timeInMillis > currentTimeMs) {
          // Calculate draw number offset from 120 (31 Oct 2025)
          // 2025 Oct = 120
          // Quarters elapsed since Oct 2025:
          val baseYear = 2025
          val baseQuarterIndex = 3 // 0: Jan, 1: Apr, 2: Jul, 3: Oct
          val targetQuarterIndex = when (monthIndex) {
            0 -> 0
            3 -> 1
            6 -> 2
            else -> 3
          }
          val quarterDifference = (year - baseYear) * 4 + (targetQuarterIndex - baseQuarterIndex)
          val calculatedDrawNumber = 120 + quarterDifference

          return UpcomingDrawInfo(
            drawNumber = calculatedDrawNumber,
            drawDateFormatted = "$dayOfMonth $monthName $year",
            targetEpochMs = drawCal.timeInMillis,
            venue = "Dhaka",
            seriesCount = 82 + (calculatedDrawNumber - 120) * 2
          )
        }
      }
    }

    // Fallback default
    return UpcomingDrawInfo(
      drawNumber = 124,
      drawDateFormatted = "31 October 2026",
      targetEpochMs = currentTimeMs + 30L * 24 * 3600 * 1000
    )
  }

  fun calculateRemainingTime(targetEpochMs: Long, currentEpochMs: Long = System.currentTimeMillis()): CountdownTimeRemaining {
    val diffMs = targetEpochMs - currentEpochMs
    if (diffMs <= 0) {
      return CountdownTimeRemaining(
        days = 0,
        hours = 0,
        minutes = 0,
        seconds = 0,
        isDrawDay = true,
        totalRemainingSeconds = 0
      )
    }

    val totalSec = diffMs / 1000
    val days = totalSec / 86400
    val hours = (totalSec % 86400) / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60

    return CountdownTimeRemaining(
      days = days,
      hours = hours,
      minutes = minutes,
      seconds = seconds,
      isDrawDay = false,
      totalRemainingSeconds = totalSec
    )
  }
}
