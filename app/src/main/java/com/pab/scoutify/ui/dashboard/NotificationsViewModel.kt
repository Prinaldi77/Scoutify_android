package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.api.ApiService
import com.pab.scoutify.model.NotificationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        startPolling()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getNotifications()
                if (response.isSuccessful) {
                    val body = response.body()
                    val notifications = body?.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = notifications,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal memuat notifikasi: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Terjadi kesalahan"
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.markNotificationRead(notificationId)
                if (response.isSuccessful) {
                    // Update lokal agar langsung berubah tanpa harus reload
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notif ->
                                if (notif.id.toLong() == notificationId) {
                                    notif.copy(isRead = true)
                                } else {
                                    notif
                                }
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore silently for mark as read
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = apiService.markAllNotificationsRead()
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) }
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore silently
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Poll setiap 30 detik untuk mendapatkan notifikasi baru secara real-time
     */
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L) // Poll every 30 seconds
                try {
                    val response = apiService.getNotifications()
                    if (response.isSuccessful) {
                        val newNotifications = response.body()?.data ?: emptyList()
                        _uiState.update { it.copy(notifications = newNotifications) }
                    }
                } catch (_: Exception) {
                    // Silent fail for polling
                }
            }
        }
    }
}
