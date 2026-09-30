package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a winning bond number in a specific Bangladesh Bank draw.
 *
 * Official Bangladesh Bank Prize Tiers per series:
 * 1st Prize: 1 winner of 6,00,000 BDT
 * 2nd Prize: 1 winner of 3,25,000 BDT
 * 3rd Prize: 2 winners of 1,00,000 BDT each
 * 4th Prize: 2 winners of 50,000 BDT each
 * 5th Prize: 40 winners of 10,000 BDT each
 */
@Entity(
  tableName = "winning_numbers",
  indices = [
    Index(value = ["winningNumber", "drawNumber"], unique = true),
    Index(value = ["winningNumber"]),
    Index(value = ["drawNumber"])
  ]
)
data class WinningNumber(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val drawNumber: Int,
  val prizeTier: Int, // 1, 2, 3, 4, or 5
  val prizeAmount: Long, // 600000, 325000, 100000, 50000, 10000
  val winningNumber: String, // 7-digit string (e.g. 0123456)
  val prizeDescription: String = "",
  val drawDate: String = ""
) {
  val formattedPrize: String
    get() = when (prizeTier) {
      1 -> "1st Prize (৳ 6,00,000)"
      2 -> "2nd Prize (৳ 3,25,000)"
      3 -> "3rd Prize (৳ 1,00,000)"
      4 -> "4th Prize (৳ 50,000)"
      5 -> "5th Prize (৳ 10,000)"
      else -> "Prize (৳ $prizeAmount)"
    }
}
