package com.example.a220945_kasthoori_nelson_project2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ROOM ENTITY — defines the "check_in_records" table structure.
 * Each field becomes a column. Room generates the SQL table automatically.
 *
 * @Entity tells Room this data class maps to a database table.
 * @PrimaryKey(autoGenerate = true) means Room assigns a unique ID for each row automatically.
 */
@Entity(tableName = "check_in_records")
data class CheckInRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentName: String,
    val matricNumber: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,      // e.g. "UKM Campus" or "Near Library"
    val xpEarned: Int,             // XP awarded for this check-in (50 XP per campus check-in)
    val timestamp: Long = System.currentTimeMillis()  // Epoch millis — easy to sort and format
)