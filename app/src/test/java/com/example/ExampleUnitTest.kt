package com.example

import com.example.ocr.PrizeBondOcrEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun `convert bengali numerals to latin numerals`() {
    val bengali = "০১২৩৪৫৬৭৮৯"
    val latin = PrizeBondOcrEngine.convertBengaliDigitsToLatin(bengali)
    assertEquals("0123456789", latin)
  }

  @Test
  fun `extract 7-digit bond numbers from text snippet`() {
    val sampleOcrText = """
      BANGLADESH BANK
      100 TAKA PRIZE BOND
      SERIES: কখ NO: 0428319
      ANOTHER BOND: 0876124 (গঘ)
      INVALID: 12345 99999999
    """.trimIndent()

    val candidates = PrizeBondOcrEngine.extractBondsFromRecognizedText(sampleOcrText)
    assertEquals(2, candidates.size)
    assertEquals("0428319", candidates[0].number)
    assertEquals("কখ", candidates[0].series)
    assertEquals("0876124", candidates[1].number)
    assertEquals("গঘ", candidates[1].series)
  }

  @Test
  fun `extract bengali numerals formatted bond`() {
    val bengaliOcrText = "সিরিজ: খগ নম্বর: ০৫৩৯১৮৪"
    val candidates = PrizeBondOcrEngine.extractBondsFromRecognizedText(bengaliOcrText)
    assertEquals(1, candidates.size)
    assertEquals("0539184", candidates[0].number)
    assertEquals("খগ", candidates[0].series)
  }

  @Test
  fun `calculate upcoming draw countdown accurately`() {
    val currentMs = System.currentTimeMillis()
    val upcoming = com.example.util.UpcomingDrawCalculator.getNextUpcomingDraw(currentMs)

    assertTrue(upcoming.drawNumber >= 120)
    assertTrue(upcoming.targetEpochMs > currentMs)
    assertTrue(upcoming.drawDateFormatted.contains("31") || upcoming.drawDateFormatted.contains("30"))

    val countdown = com.example.util.UpcomingDrawCalculator.calculateRemainingTime(upcoming.targetEpochMs, currentMs)
    assertTrue(countdown.totalRemainingSeconds > 0)
    assertTrue(countdown.days >= 0)
    assertTrue(countdown.hours in 0..23)
    assertTrue(countdown.minutes in 0..59)
    assertTrue(countdown.seconds in 0..59)
  }
}
