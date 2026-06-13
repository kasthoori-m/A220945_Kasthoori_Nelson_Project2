package com.example.a220945_kasthoori_nelson_project2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val matricNumber: String,
    val name: String,
    val program: String,
    val totalXP: Int,
    val currentStreak: Int,
    val lastActiveDate: String,
    val lessonHighScoresJson: String,
    val courseProgressJson: String = "{}"  // Stores course progress Map<courseId, Float>
)