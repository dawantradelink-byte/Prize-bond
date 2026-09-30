package com.example

import com.example.data.remote.OfficialBangladeshBankData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialBangladeshBankDataIntegrityTest {

  @Test
  fun hasEightOfficialDrawsWithFortySixUniqueWinnersEach() {
    val draws = OfficialBangladeshBankData.getOfficialDraws()
    val winners = OfficialBangladeshBankData.getWinningNumbers()

    assertEquals((117..124).toList(), draws.map { it.drawNumber }.sorted())
    assertEquals(368, winners.size)

    for (drawNumber in 117..124) {
      val drawWinners = winners.filter { it.drawNumber == drawNumber }
      assertEquals("draw $drawNumber must have exactly 46 winners", 46, drawWinners.size)
      assertEquals(
        "draw $drawNumber must have unique winning numbers",
        46,
        drawWinners.map { it.winningNumber }.toSet().size
      )
      assertEquals(
        mapOf(1 to 1, 2 to 1, 3 to 2, 4 to 2, 5 to 40),
        drawWinners.groupingBy { it.prizeTier }.eachCount()
      )
    }
  }

  @Test
  fun latestDrawMatchesBangladeshBank124thDraw() {
    val expected = listOf(
      "0786345", "0911829", "0486181", "0700725", "0101571", "0677548",
      "0050418", "0311203", "0559958", "0760359", "0900788",
      "0115181", "0393187", "0588156", "0775152", "0912209",
      "0139380", "0454894", "0605926", "0808651", "0924656",
      "0146127", "0483646", "0654415", "0825345", "0946563",
      "0152480", "0505976", "0669021", "0842904", "0952827",
      "0185648", "0534811", "0706178", "0870417", "0967420",
      "0293525", "0542962", "0740194", "0878442", "0970749",
      "0307909", "0544707", "0758869", "0891209", "0979041"
    )

    val actual = OfficialBangladeshBankData.getWinningNumbers()
      .filter { it.drawNumber == 124 }
      .map { it.winningNumber }

    assertEquals(expected, actual)
    assertTrue(actual.none { it == "0000001" || it == "1000000" })
  }
}
