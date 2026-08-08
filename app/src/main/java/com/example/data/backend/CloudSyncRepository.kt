package com.example.data.backend

import android.content.Context
import android.util.Log
import com.example.data.StatsEntity
import com.example.model.BrainLevelInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class CloudUserProfile(
    val userId: String = "",
    val displayName: String = "Joueur Zen",
    val brainLevel: Int = 1,
    val brainTitle: String = "Neurone Intuitif",
    val brainEmoji: String = "🌱",
    val totalPoints: Int = 0,
    val winRatePercentage: Int = 0,
    val gamesCompleted: Int = 0,
    val bestTimeEasy: Long = 0,
    val bestTimeMedium: Long = 0,
    val bestTimeHard: Long = 0,
    val bestTimeExpert: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

class CloudSyncRepository(private val context: Context) {

    private val tag = "CloudSyncRepository"

    val isFirebaseInitialized: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = if (isFirebaseInitialized) FirebaseAuth.getInstance() else null

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseInitialized) FirebaseFirestore.getInstance() else null

    fun getCurrentUserId(): String? {
        return auth?.currentUser?.uid
    }

    suspend fun ensureAnonymousAuth(): String? {
        val currentAuth = auth ?: return null
        return try {
            if (currentAuth.currentUser != null) {
                currentAuth.currentUser?.uid
            } else {
                val result = currentAuth.signInAnonymously().await()
                result.user?.uid
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed anonymous authentication: ${e.message}")
            null
        }
    }

    suspend fun syncStatsToCloud(stats: StatsEntity, brainInfo: BrainLevelInfo, customName: String = "Joueur Zen"): Boolean {
        val db = firestore ?: return false
        val uid = ensureAnonymousAuth() ?: return false

        val totalCompleted = stats.easyCompleted + stats.mediumCompleted + stats.hardCompleted + stats.expertCompleted

        val profile = CloudUserProfile(
            userId = uid,
            displayName = customName.ifBlank { "Joueur Zen ${uid.take(4)}" },
            brainLevel = brainInfo.level,
            brainTitle = brainInfo.title,
            brainEmoji = brainInfo.iconEmoji,
            totalPoints = brainInfo.currentPoints,
            winRatePercentage = brainInfo.winRatePercentage,
            gamesCompleted = totalCompleted,
            bestTimeEasy = stats.bestTimeEasy,
            bestTimeMedium = stats.bestTimeMedium,
            bestTimeHard = stats.bestTimeHard,
            bestTimeExpert = stats.bestTimeExpert,
            lastUpdated = System.currentTimeMillis()
        )

        return try {
            db.collection("leaderboard").document(uid).set(profile).await()
            db.collection("users").document(uid).collection("profile").document("stats").set(profile).await()
            Log.d(tag, "Successfully synced stats for user $uid")
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync stats to Firestore: ${e.message}")
            false
        }
    }

    fun getLeaderboardFlow(limit: Int = 20): Flow<List<CloudUserProfile>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("leaderboard")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error fetching leaderboard: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CloudUserProfile::class.java)
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }
}
