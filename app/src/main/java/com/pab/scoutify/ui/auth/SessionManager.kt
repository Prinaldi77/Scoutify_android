package com.pab.scoutify.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_scoutify_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to standard SharedPreferences if encrypted fails due to keystore corruption
        context.getSharedPreferences("scoutify_session_fallback", Context.MODE_PRIVATE)
    }

    companion object {
        const val KEY_TOKEN = "jwt_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_IS_LOGIN = "is_logged_in"
        const val KEY_DEVICE_ID = "bound_device_id"
        const val KEY_USER_NAME = "user_full_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_PHOTO_URL = "user_photo_url"
        const val KEY_ROLE = "user_role"
        const val KEY_GUGUS_DEPAN = "user_gugus_depan"
        const val KEY_NOMOR_INDUK = "user_nomor_induk"
        const val KEY_JABATAN = "user_jabatan"
        const val KEY_MEMBER_ACCESS = "member_access"
        const val KEY_MEMBER_CASH = "member_cash_balance"
        const val KEY_MEMBER_KAS = "member_kas_transactions"
        
        const val KEY_USER_AVATAR_EMOJI = "user_avatar_emoji"
        const val KEY_USER_AVATAR_COLOR = "user_avatar_color"
        
        const val KEY_GEOFENCE_LAT = "geofence_lat"
        const val KEY_GEOFENCE_LNG = "geofence_lng"
        const val KEY_GEOFENCE_RADIUS = "geofence_radius"
        const val KEY_ATTENDANCE_START = "attendance_start"
        const val KEY_ATTENDANCE_END = "attendance_end"
    }

    fun saveAuthToken(token: String, refreshToken: String? = null) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putBoolean(KEY_IS_LOGIN, true)
            apply()
        }
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }
    
    // User Info
    fun saveUserId(id: Int) = prefs.edit().putInt("user_id", id).apply()
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    
    fun saveUserName(name: String?) = prefs.edit().putString(KEY_USER_NAME, name).apply()
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Scoutify User") ?: "Scoutify User"
    
    fun saveUserEmail(email: String?) = prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "user@scoutify.com") ?: "user@scoutify.com"
    
    fun savePhotoUrl(url: String?) = prefs.edit().putString(KEY_PHOTO_URL, url).apply()
    fun getPhotoUrl(): String? = prefs.getString(KEY_PHOTO_URL, null)
    
    fun saveGugusDepan(gugusDepan: String?) = prefs.edit().putString(KEY_GUGUS_DEPAN, gugusDepan).apply()
    fun getGugusDepan(): String = prefs.getString(KEY_GUGUS_DEPAN, "-") ?: "-"

    fun saveNomorInduk(nomorInduk: String?) = prefs.edit().putString(KEY_NOMOR_INDUK, nomorInduk).apply()
    fun getNomorInduk(): String = prefs.getString(KEY_NOMOR_INDUK, "-") ?: "-"

    fun saveJabatan(jabatan: String?) = prefs.edit().putString(KEY_JABATAN, jabatan).apply()
    fun getJabatan(): String = prefs.getString(KEY_JABATAN, "-") ?: "-"
    
    fun saveRole(role: String?) = prefs.edit().putString(KEY_ROLE, role).apply()
    fun getRole(): String = prefs.getString(KEY_ROLE, "member") ?: "member"
    
    fun setMemberAccess(hasAccess: Boolean) = prefs.edit().putBoolean(KEY_MEMBER_ACCESS, hasAccess).apply()
    fun hasMemberAccess(): Boolean = prefs.getBoolean(KEY_MEMBER_ACCESS, false)
    
    fun saveMemberCashBalance(balance: String?) = prefs.edit().putString(KEY_MEMBER_CASH, balance).apply()
    fun getMemberCashBalance(): String = prefs.getString(KEY_MEMBER_CASH, "0") ?: "0"

    fun saveMemberKasTransactions(transactions: String?) = prefs.edit().putString(KEY_MEMBER_KAS, transactions).apply()
    fun getMemberKasTransactions(): String = prefs.getString(KEY_MEMBER_KAS, "[]") ?: "[]"

    fun saveUserAvatarEmoji(emoji: String) = prefs.edit().putString(KEY_USER_AVATAR_EMOJI, emoji).apply()
    fun getUserAvatarEmoji(): String = prefs.getString(KEY_USER_AVATAR_EMOJI, "⛺") ?: "⛺"

    fun saveUserAvatarColor(color: String) = prefs.edit().putString(KEY_USER_AVATAR_COLOR, color).apply()
    fun getUserAvatarColor(): String = prefs.getString(KEY_USER_AVATAR_COLOR, "#3E5F44") ?: "#3E5F44"

    // Settings
    fun saveGeofenceLat(lat: Double) = prefs.edit().putFloat(KEY_GEOFENCE_LAT, lat.toFloat()).apply()
    fun getGeofenceLat(): Double = prefs.getFloat(KEY_GEOFENCE_LAT, -6.200000f).toDouble()
    
    fun saveGeofenceLng(lng: Double) = prefs.edit().putFloat(KEY_GEOFENCE_LNG, lng.toFloat()).apply()
    fun getGeofenceLng(): Double = prefs.getFloat(KEY_GEOFENCE_LNG, 106.816666f).toDouble()
    
    fun saveGeofenceRadius(radius: Float) = prefs.edit().putFloat(KEY_GEOFENCE_RADIUS, radius).apply()
    fun getGeofenceRadius(): Float = prefs.getFloat(KEY_GEOFENCE_RADIUS, 100f)
    
    fun saveAttendanceStartTime(time: String?) = prefs.edit().putString(KEY_ATTENDANCE_START, time).apply()
    fun getAttendanceStartTime(): String = prefs.getString(KEY_ATTENDANCE_START, "00:00") ?: "00:00"
    
    fun saveAttendanceEndTime(time: String?) = prefs.edit().putString(KEY_ATTENDANCE_END, time).apply()
    fun getAttendanceEndTime(): String = prefs.getString(KEY_ATTENDANCE_END, "23:59") ?: "23:59"

    fun fetchAuthToken(): String? = prefs.getString(KEY_TOKEN, null)
    
    fun fetchRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)

    fun isUserLogin(): Boolean = prefs.getBoolean(KEY_IS_LOGIN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun clearSession() = logout()
}
