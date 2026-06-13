package com.example.a220945_kasthoori_nelson_project2.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// ---------------------------------------------------------------------------
// DATA MODEL — one leaderboard entry per student
// ---------------------------------------------------------------------------

data class LeaderboardEntry(
    val name: String = "",
    val matricNumber: String = "",
    val totalXP: Int = 0,
    val currentTitle: String = "",
    val checkInCount: Int = 0
)

// ---------------------------------------------------------------------------
// FIREBASE REPOSITORY — all Firestore operations live here
//
// Why a separate repository class?
// Keeps Firebase logic out of the ViewModel and out of the UI.
// If we swap Firebase for something else later, only this file changes.
// ---------------------------------------------------------------------------

class FirebaseRepository {

    // Firebase.firestore is a lazy singleton — safe to call multiple times
    private val db = Firebase.firestore

    // Collection name in Firestore — think of it like a database table name
    private val leaderboardCollection = db.collection("campus_leaderboard")

    /**
     * PUSH student data to Firestore.
     *
     * We use the matric number as the document ID so each student has exactly
     * one document. If they check in again, it SETS (overwrites) their entry
     * with the latest XP — no duplicate documents.
     *
     * .await() is a coroutine extension from kotlinx-coroutines-play-services
     * that suspends until the Firestore Task completes.
     */
    suspend fun pushToLeaderboard(entry: LeaderboardEntry): Result<Unit> {
        return try {
            leaderboardCollection
                .document(entry.matricNumber)   // Document ID = matric number (unique per student)
                .set(entry)                     // set() creates or overwrites the document
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FETCH top 10 students ordered by XP descending.
     *
     * Returns a Result wrapper so the UI can handle success/failure cleanly
     * without needing try-catch in the ViewModel.
     */
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            val snapshot = leaderboardCollection
                .orderBy("totalXP", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            // Converts each Firestore document into a LeaderboardEntry data class
            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(LeaderboardEntry::class.java)
            }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}