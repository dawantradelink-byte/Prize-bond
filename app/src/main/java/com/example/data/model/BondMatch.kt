package com.example.data.model

/**
 * Combines a UserBond with any matched winning draw records.
 */
data class BondMatch(
  val bond: UserBond,
  val winningDraws: List<WinningNumber> = emptyList()
) {
  val isWinner: Boolean
    get() = winningDraws.isNotEmpty()

  val totalPrizeWon: Long
    get() = winningDraws.sumOf { it.prizeAmount }

  val highestPrizeTier: Int?
    get() = winningDraws.minOfOrNull { it.prizeTier }
}

/**
 * Summary statistics for the user's bond portfolio.
 */
data class BondSummaryStats(
  val totalBondsCount: Int = 0,
  val totalInvestmentAmount: Long = 0L, // each bond costs 100 Taka
  val winningBondsCount: Int = 0,
  val totalPrizeAmountWon: Long = 0L,
  val latestDrawNumber: Int = 0,
  val lastSyncTime: Long = 0L
)
