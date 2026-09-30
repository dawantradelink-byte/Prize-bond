package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DrawResult
import com.example.data.model.UserBond
import com.example.data.model.WinningNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface PrizeBondDao {

  // --- User Bonds ---
  @Query("SELECT * FROM user_bonds ORDER BY addedTimestamp DESC")
  fun getAllUserBonds(): Flow<List<UserBond>>

  @Query("SELECT * FROM user_bonds")
  suspend fun getAllUserBondsSnapshot(): List<UserBond>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertBond(bond: UserBond): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertBonds(bonds: List<UserBond>): List<Long>

  @Query("DELETE FROM user_bonds WHERE id = :id")
  suspend fun deleteBond(id: Long)

  @Query("DELETE FROM user_bonds")
  suspend fun deleteAllBonds()

  // --- Draw Results ---
  @Query("SELECT * FROM draw_results ORDER BY drawNumber DESC")
  fun getAllDrawResults(): Flow<List<DrawResult>>

  @Query("SELECT * FROM draw_results ORDER BY drawNumber DESC LIMIT 1")
  suspend fun getLatestDraw(): DrawResult?

  @Query("SELECT COUNT(*) FROM draw_results")
  suspend fun getDrawCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrawResult(draw: DrawResult)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrawResults(draws: List<DrawResult>)

  // --- Winning Numbers ---
  @Query("SELECT * FROM winning_numbers ORDER BY drawNumber DESC, prizeTier ASC")
  fun getAllWinningNumbers(): Flow<List<WinningNumber>>

  @Query("SELECT * FROM winning_numbers WHERE winningNumber = :number ORDER BY drawNumber DESC")
  suspend fun getWinningMatchesForNumber(number: String): List<WinningNumber>

  @Query("SELECT * FROM winning_numbers WHERE drawNumber = :drawNumber ORDER BY prizeTier ASC")
  fun getWinningNumbersForDraw(drawNumber: Int): Flow<List<WinningNumber>>

  @Query("SELECT * FROM winning_numbers WHERE drawNumber = :drawNumber ORDER BY prizeTier ASC")
  suspend fun getWinningNumbersForDrawSnapshot(drawNumber: Int): List<WinningNumber>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWinningNumbers(numbers: List<WinningNumber>)

  @Query("SELECT COUNT(*) FROM winning_numbers")
  suspend fun getWinningNumberCount(): Int
}
