package com.example.a220945_kasthoori_nelson_project2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    /**
     * Load a specific student's profile by matric number.
     * Returns null if this matric number has never logged in before.
     */
    @Query("SELECT * FROM user_profile WHERE matricNumber = :matric")
    suspend fun getProfileByMatric(matric: String): UserProfileEntity?

    /**
     * Get all saved profiles — used to show a "returning user" list on ProfileSetup.
     */
    @Query("SELECT * FROM user_profile ORDER BY name ASC")
    suspend fun getAllProfiles(): List<UserProfileEntity>

    /**
     * LOGOUT — does NOT delete data. Just used to signal the app to clear
     * the in-memory session. The Room row stays intact for next login.
     * (Actual logout logic is handled in the ViewModel.)
     */

    /**
     * RESET — permanently deletes this student's profile row from Room.
     */
    @Query("DELETE FROM user_profile WHERE matricNumber = :matric")
    suspend fun deleteProfile(matric: String)

    /**
     * Wipe ALL profiles — used for full device reset.
     */
    @Query("DELETE FROM user_profile")
    suspend fun clearAllProfiles()
}