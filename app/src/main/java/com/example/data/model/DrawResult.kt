package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a quarterly draw conducted by Bangladesh Bank for 100 Taka Prize Bonds.
 * Draws occur on: 31 January, 30 April, 31 July, 31 October.
 * Results remain valid for claim for 2 years (8 draws).
 */
@Entity(tableName = "draw_results")
data class DrawResult(
  @PrimaryKey
  val drawNumber: Int,
  val drawDate: String,
  val drawPlace: String = "Dhaka",
  val seriesCount: Int = 80,
  val validUntil: String = "",
  val isOfficial: Boolean = true,
  val lastSyncedTimestamp: Long = System.currentTimeMillis()
)
