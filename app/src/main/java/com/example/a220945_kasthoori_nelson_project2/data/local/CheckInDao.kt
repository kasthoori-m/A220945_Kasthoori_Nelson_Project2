package com.example.a220945_kasthoori_nelson_project2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ROOM DAO (Data Access Object) — defines HOW we talk to the database.
 * Each function here becomes a safe, coroutine-friendly database operation.
 * We never write raw SQL strings in the ViewModel — all queries live here.
 *
 * @Dao marks this interface for Room to generate the implementation automatically.
 */
@Dao
interface CheckInDao {

    /**
     * INSERT a new check-in record.
     * OnConflictStrategy.REPLACE means if somehow the same ID is inserted twice,
     * the old row is replaced rather than crashing.
     * 'suspend' means this runs on a background thread (coroutine), not the UI thread.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(record: CheckInRecord)

    /**
     * SELECT all check-in records, newest first.
     * Returns a Flow<List<...>> so the UI automatically updates whenever new rows are added —
     * just like StateFlow in the ViewModel, but backed by the real database.
     */
    @Query("SELECT * FROM check_in_records ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<CheckInRecord>>

    /**
     * SELECT only this student's check-ins using their matric number.
     * Used on the Campus Check-In screen to show personal history.
     */
    @Query("SELECT * FROM check_in_records WHERE matricNumber = :matric ORDER BY timestamp DESC")
    fun getCheckInsByMatric(matric: String): Flow<List<CheckInRecord>>

    /**
     * COUNT how many times this student has checked in today.
     * Used to prevent duplicate XP farming — only 1 bonus per day allowed.
     * :startOfDay and :endOfDay are epoch milliseconds for midnight to 11:59 PM.
     */
    @Query("SELECT COUNT(*) FROM check_in_records WHERE matricNumber = :matric AND timestamp BETWEEN :startOfDay AND :endOfDay")
    suspend fun countCheckInsToday(matric: String, startOfDay: Long, endOfDay: Long): Int

    /**
     * COUNT the total number of check-ins this student has ever made.
     * Used for the Firebase leaderboard's checkInCount field.
     */
    @Query("SELECT COUNT(*) FROM check_in_records WHERE matricNumber = :matric")
    suspend fun countTotalCheckIns(matric: String): Int

    /**
     * DELETE all records — used when the user resets the app.
     */
    @Query("DELETE FROM check_in_records")
    suspend fun clearAll()
}