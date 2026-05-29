package com.pab.scoutify.utils

import android.content.Context
import android.location.Location
import android.os.Build
import android.provider.Settings
import java.util.*

object SecurityHardwareScanner {

    /**
     * Rigorous check for emulator detection based on hardware signatures.
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("vbox86")
                || Build.PRODUCT.contains("sdk_gphone_x86")
                || Build.HARDWARE.contains("virtio"))
    }

    /**
     * Checks if the location is coming from a mock provider (Fake GPS).
     */
    fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    /**
     * Extracts unique device signature for binding.
     */
    fun getDeviceDetails(context: Context): Map<String, String> {
        return mapOf(
            "device_id" to (Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"),
            "device_brand" to Build.MANUFACTURER,
            "device_model" to Build.MODEL,
            "android_version" to Build.VERSION.RELEASE,
            "api_level" to Build.VERSION.SDK_INT.toString()
        )
    }
}
