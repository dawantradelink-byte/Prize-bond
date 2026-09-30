package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a 100 Taka prize bond owned/tracked by the user.
 *
 * Bangladesh Bank prize bond numbers are 7 digits (e.g. 0123456).
 * Series can be Bengali letters (e.g. কখ, খগ, ঘঙ) or English/all series.
 */
@Entity(
  tableName = "user_bonds",
  indices = [Index(value = ["bondNumber", "series"], unique = true)]
)
data class UserBond(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val bondNumber: String,
  val series: String = "All",
  val purchaseDate: String = "",
  val note: String = "",
  val addedTimestamp: Long = System.currentTimeMillis()
)
