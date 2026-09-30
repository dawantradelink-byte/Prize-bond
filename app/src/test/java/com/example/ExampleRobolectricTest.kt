package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PrizeBond BD", appName)
  }

  @Test
  fun `verify official bangladesh bank prize tiers`() {
    val winning = com.example.data.remote.OfficialBangladeshBankData.getWinningNumbers()
    assertTrue(winning.isNotEmpty())

    // 1st prize should be 600,000 Tk
    val firstPrize = winning.firstOrNull { it.prizeTier == 1 }
    assertNotNull(firstPrize)
    assertEquals(600000L, firstPrize?.prizeAmount)

    // 2nd prize should be 325,000 Tk
    val secondPrize = winning.firstOrNull { it.prizeTier == 2 }
    assertNotNull(secondPrize)
    assertEquals(325000L, secondPrize?.prizeAmount)
  }
}
