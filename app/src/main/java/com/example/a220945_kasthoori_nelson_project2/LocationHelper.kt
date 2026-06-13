package com.example.a220945_kasthoori_nelson_project2.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * LOCATION HELPER — wraps Android's Fused Location Provider API.
 *
 * Why Fused Location Provider?
 * It automatically picks the best location source (GPS satellite, Wi-Fi, cell tower)
 * depending on accuracy needed and battery state. Much better than using GPS directly.
 *
 * We use a single 'getCurrentLocation()' call (not continuous updates) because
 * EduQuest only needs location at the moment the student taps "Check In".
 */
class LocationHelper(context: Context) {

    // FusedLocationProviderClient is the main entry point for location APIs
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Gets the device's current location as a one-shot request.
     * Returns null if permission is denied or location is unavailable.
     *
     * @SuppressLint("MissingPermission") — we suppress this lint warning because
     * we check permissions in the Composable BEFORE calling this function.
     * The user grants permission — we verify it in the UI layer first.
     *
     * 'suspend' means this runs on a coroutine background thread.
     * Priority.PRIORITY_HIGH_ACCURACY uses GPS for the most accurate result.
     * CancellationTokenSource allows the request to be cancelled if the coroutine is cancelled.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * CAMPUS DETECTION — checks if the student is within a set radius of campus.
     * Uses Android's Location.distanceBetween() utility (Haversine formula internally).
     * Returns true if within [radiusMeters] of the target coordinates.
     */
    fun isNearCampus(
        currentLat: Double,
        currentLon: Double,
        campusLat: Double = 2.9213,   // UKM Bangi main campus latitude
        campusLon: Double = 101.7741, // UKM Bangi main campus longitude
        radiusMeters: Float = 1000f   // 1km radius covers the full main campus
//        radiusMeters: Float = 500000f // 500km radius covers all of Malaysia (testing)
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(currentLat, currentLon, campusLat, campusLon, results)
        return results[0] <= radiusMeters
    }

    /**
     * Returns a human-readable location name based on proximity to campus.
     * Saved to Room and displayed in check-in history.
     */
    fun getLocationName(lat: Double, lon: Double): String {
        return if (isNearCampus(lat, lon)) "UKM Bangi Campus"
        else "Off Campus (%.4f, %.4f)".format(lat, lon)
    }
}