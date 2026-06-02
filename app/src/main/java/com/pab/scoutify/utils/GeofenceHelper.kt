package com.pab.scoutify.utils

import android.location.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceHelper @Inject constructor() {

    /**
     * Menghitung jarak antara dua koordinat dalam meter.
     */
    fun getDistance(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    /**
     * Validasi apakah user berada di dalam radius geofence.
     */
    fun isWithinRadius(
        userLat: Double,
        userLng: Double,
        targetLat: Double,
        targetLng: Double,
        radiusInMeters: Int
    ): Boolean {
        val distance = getDistance(userLat, userLng, targetLat, targetLng)
        return distance <= radiusInMeters
    }
}
