package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.UserBond
import com.example.data.model.WinningNumber
import com.example.data.remote.OfficialBangladeshBankData
import com.example.data.repository.PrizeBondRepository
import com.example.ocr.PrizeBondOcrEngine
import com.example.sync.NotificationHelper
import com.example.util.UpcomingDrawCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrizeBondComprehensiveQaTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: PrizeBondRepository
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = PrizeBondRepository(database.prizeBondDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  // --- CUJ 1: Room Local Storage CRUD Operations ---

  @Test
  fun testSaveSingleBondAndRetrieve() = runBlocking {
    val result = repository.addSingleBond("0123456", "কখ", "Bank Purchase")
    assertTrue(result.isSuccess)

    val matches = repository.userBondsWithMatches.first()
    assertEquals(1, matches.size)
    assertEquals("0123456", matches[0].bond.bondNumber)
    assertEquals("কখ", matches[0].bond.series)
    assertEquals("Bank Purchase", matches[0].bond.note)
  }

  @Test
  fun testAddSeriesRangeBonds() = runBlocking {
    val rangeResult = repository.addSeriesRange("0001000", "0001009", "খগ")
    assertTrue(rangeResult.isSuccess)
    assertEquals(10, rangeResult.getOrNull())

    val bonds = repository.userBondsWithMatches.first()
    assertEquals(10, bonds.size)
    assertEquals("0001000", bonds.first().bond.bondNumber)
    assertEquals("0001009", bonds.last().bond.bondNumber)
  }

  @Test
  fun testBulkTextImport() = runBlocking {
    val rawText = "0539184, 0184920; 0328194\n0784912 0491823"
    val result = repository.addBulkText(rawText, "All")
    assertTrue(result.isSuccess)
    assertEquals(5, result.getOrNull())

    val bonds = repository.userBondsWithMatches.first()
    assertEquals(5, bonds.size)
  }

  @Test
  fun testDeleteBondAndClearAll() = runBlocking {
    repository.addSingleBond("0111111", "কখ", "")
    repository.addSingleBond("0222222", "খগ", "")

    var bonds = repository.userBondsWithMatches.first()
    assertEquals(2, bonds.size)

    val bondToDelete = bonds[0].bond.id
    repository.deleteBond(bondToDelete)

    bonds = repository.userBondsWithMatches.first()
    assertEquals(1, bonds.size)

    repository.clearAllBonds()
    bonds = repository.userBondsWithMatches.first()
    assertTrue(bonds.isEmpty())
  }

  // --- CUJ 2: Winning Bond Matching Logic ---

  @Test
  fun testAutomaticWinningBondMatching() = runBlocking {
    repository.ensureInitialDataLoaded()

    // 0428319 is the 1st prize winner in 120th Draw (৳ 6,00,000)
    repository.addSingleBond("0428319", "কখ", "Winner bond")
    // 0876124 is the 2nd prize winner in 120th Draw (৳ 3,25,000)
    repository.addSingleBond("0876124", "গঘ", "Second prize")
    // 0999999 is a non-winner
    repository.addSingleBond("0999999", "All", "Regular bond")

    val matches = repository.userBondsWithMatches.first()
    assertEquals(3, matches.size)

    val firstPrizeMatch = matches.find { it.bond.bondNumber == "0428319" }
    assertNotNull(firstPrizeMatch)
    assertTrue(firstPrizeMatch!!.isWinner)
    assertEquals(1, firstPrizeMatch.highestPrizeTier)
    assertEquals(600000L, firstPrizeMatch.totalPrizeWon)

    val secondPrizeMatch = matches.find { it.bond.bondNumber == "0876124" }
    assertNotNull(secondPrizeMatch)
    assertTrue(secondPrizeMatch!!.isWinner)
    assertEquals(2, secondPrizeMatch.highestPrizeTier)
    assertEquals(325000L, secondPrizeMatch.totalPrizeWon)

    val nonWinnerMatch = matches.find { it.bond.bondNumber == "0999999" }
    assertNotNull(nonWinnerMatch)
    assertFalse(nonWinnerMatch!!.isWinner)
    assertEquals(0L, nonWinnerMatch.totalPrizeWon)
  }

  @Test
  fun testQuickCheckNumber() = runBlocking {
    repository.ensureInitialDataLoaded()

    // Winning query
    val winMatches = repository.quickCheckNumber("0428319")
    assertTrue(winMatches.isNotEmpty())
    assertEquals(1, winMatches[0].prizeTier)
    assertEquals(600000L, winMatches[0].prizeAmount)

    // Non-winning query
    val lossMatches = repository.quickCheckNumber("0000001")
    assertTrue(lossMatches.isEmpty())
  }

  // --- CUJ 3: Upcoming Draw & Countdown Calculations ---

  @Test
  fun testUpcomingDrawCalculation() {
    val currentTimeMs = System.currentTimeMillis()
    val upcoming = UpcomingDrawCalculator.getNextUpcomingDraw(currentTimeMs)

    assertTrue(upcoming.drawNumber >= 120)
    assertTrue(upcoming.targetEpochMs > currentTimeMs)
    assertEquals("Dhaka", upcoming.venue)

    val timeRemaining = UpcomingDrawCalculator.calculateRemainingTime(upcoming.targetEpochMs, currentTimeMs)
    assertFalse(timeRemaining.isDrawDay)
    assertTrue(timeRemaining.totalRemainingSeconds > 0)
    assertTrue(timeRemaining.hours in 0..23)
    assertTrue(timeRemaining.minutes in 0..59)
    assertTrue(timeRemaining.seconds in 0..59)
  }

  // --- CUJ 4: OCR Engine & Bengali Numeral Translation ---

  @Test
  fun testBengaliNumeralConversion() {
    val bengaliDigits = "০১২৩৪৫৬৭৮৯"
    val converted = PrizeBondOcrEngine.convertBengaliDigitsToLatin(bengaliDigits)
    assertEquals("0123456789", converted)
  }

  @Test
  fun testOcrTextExtractionMultipleCandidates() {
    val ocrOutput = """
      গণপ্রজাতন্ত্রী বাংলাদেশ সরকার
      ১০০ টাকা প্রাইজবন্ড
      সিরিজ: কখ
      নম্বর: ০৪২৮৩১৯
      Another serial: GH 0876124
      Unrelated text: Date 31/10/2026 Code 4819
    """.trimIndent()

    val candidates = PrizeBondOcrEngine.extractBondsFromRecognizedText(ocrOutput)
    assertTrue(candidates.size >= 2)
    assertEquals("0428319", candidates[0].number)
    assertEquals("কখ", candidates[0].series)
    assertEquals("0876124", candidates[1].number)
  }

  // --- CUJ 5: Notification Channel Setup ---

  @Test
  fun testNotificationChannelCreated() {
    NotificationHelper.createNotificationChannel(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
    assertNotNull(channel)
    assertEquals("Prize Bond Draws & Alerts", channel.name)
  }
}
