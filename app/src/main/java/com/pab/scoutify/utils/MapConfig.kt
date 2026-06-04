package com.pab.scoutify.utils

import com.google.android.gms.maps.model.LatLng

/**
 * Konfigurasi peta dan geofencing untuk Scoutify.
 * Lokasi default: SMPN 2 Katapang, Jl. Kopo Permai, Katapang, Kab. Bandung
 */
object MapConfig {

    // ─── Lokasi Default (SMPN 2 Katapang) ─────────────────────────────────────
    const val DEFAULT_LATITUDE = -6.972600
    const val DEFAULT_LONGITUDE = 107.590800
    const val DEFAULT_LOCATION_NAME = "SMPN 2 Katapang"

    val defaultLatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)

    // ─── Zoom Level ────────────────────────────────────────────────────────────
    const val DEFAULT_ZOOM = 16f
    const val ATTENDANCE_MAP_ZOOM = 17f
    const val PICKER_MAP_ZOOM = 16f

    // ─── Radius Default (meter) ────────────────────────────────────────────────
    const val DEFAULT_RADIUS = 100f

    // ─── Batas Waktu Absensi ───────────────────────────────────────────────────
    const val DEFAULT_START_TIME = "07:00"
    const val DEFAULT_END_TIME = "08:00"
}
