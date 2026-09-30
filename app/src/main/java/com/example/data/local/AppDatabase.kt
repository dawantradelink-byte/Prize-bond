package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DrawResult
import com.example.data.model.UserBond
import com.example.data.model.WinningNumber

@Database(
  entities = [UserBond::class, DrawResult::class, WinningNumber::class],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun prizeBondDao(): PrizeBondDao

  companion object {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // v1 contained hard-coded/guessed draw results. Remove only those
        // official-result caches; user-owned bonds remain untouched.
        db.execSQL("DELETE FROM winning_numbers")
        db.execSQL("DELETE FROM draw_results")
      }
    }

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "prizebond_database"
        )
          .addMigrations(MIGRATION_1_2)
          .build()

        INSTANCE = instance
        instance
      }
    }
  }
}
