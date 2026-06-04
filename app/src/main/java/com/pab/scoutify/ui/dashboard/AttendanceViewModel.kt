package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pab.scoutify.data.repository.AttendanceRepository
import com.pab.scoutify.model.ActiveActivity
import com.pab.scoutify.model.AttendanceStatus
import com.pab.scoutify.model.AttendanceUiState
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.UserLocation
import com.pab.scoutify.utils.GeofenceHelper
import com.pab.scoutify.utils.LocationHelper
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val locationHelper: LocationHelper,
    private val geofenceHelper: GeofenceHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState = _uiState.asStateFlow()

    private val _checkInState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val checkInState: LiveData<Resource<BaseResponse<Any>>> = _checkInState

    init {
        loadInitialData()
        startLocationUpdates()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val activityResource = repository.getCurrentActivity()
            val statusResource = repository.getAttendanceStatus()

            if (activityResource is Resource.Success<ActiveActivity>) {
                val data = activityResource.data
                _uiState.update { state -> state.copy(activeActivity = data) }
                // Trigger radius check immediately if location already exists
                _uiState.value.userLocation?.let { loc -> validateRadius(data, loc) }
            }
            
            if (statusResource is Resource.Success<AttendanceStatus>) {
                val data = statusResource.data
                _uiState.update { state -> state.copy(attendanceStatus = data.status) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun startLocationUpdates() {
        locationHelper.getLocationUpdates(5000L)
            .onEach { location ->
                val userLoc = UserLocation(location.latitude, location.longitude, location.accuracy)
                _uiState.update { it.copy(userLocation = userLoc) }
                _uiState.value.activeActivity?.let { validateRadius(it, userLoc) }
            }
            .launchIn(viewModelScope)
    }

    private fun validateRadius(activity: ActiveActivity, userLocation: UserLocation) {
        val distance = geofenceHelper.getDistance(
            userLocation.latitude,
            userLocation.longitude,
            activity.latitude,
            activity.longitude
        )
        
        val isWithin = distance <= activity.radius
        
        _uiState.update { 
            it.copy(
                isWithinRadius = isWithin,
                distance = distance.toDouble()
            )
        }
    }

    fun checkIn() {
        val userLocation = _uiState.value.userLocation
        val activity = _uiState.value.activeActivity
        if (userLocation == null || activity == null) {
            _uiState.update { it.copy(errorMessage = "Lokasi atau kegiatan tidak ditemukan") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _checkInState.value = Resource.Loading
            
            val mediaType = "text/plain".toMediaTypeOrNull()
            val latBody = userLocation.latitude.toString().toRequestBody(mediaType)
            val lngBody = userLocation.longitude.toString().toRequestBody(mediaType)
            val accBody = userLocation.accuracy.toString().toRequestBody(mediaType)
            val kegiatanIdBody = activity.id.toString().toRequestBody(mediaType)
            
            val result = repository.checkIn(latBody, lngBody, accBody, kegiatanIdBody, null)
            _checkInState.value = result
            
            if (result is Resource.Success) {
                // Try to extract attendance ID from response data
                val attendanceId = tryExtractAttendanceId(result.data)
                _uiState.update { 
                    it.copy(
                        checkInSuccess = true, 
                        attendanceStatus = "Sudah Check In",
                        lastAttendanceId = attendanceId,
                        isLoading = false
                    ) 
                }
                refreshAttendanceStatus()
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
            }
        }
    }

    /**
     * Try to extract attendance ID from the check-in response data.
     * The API returns the created attendance record in data field.
     */
    private fun tryExtractAttendanceId(data: BaseResponse<Any>?): Long? {
        return try {
            val rawData = data?.data
            if (rawData is Map<*, *>) {
                (rawData["id"] as? Double)?.toLong() ?: (rawData["id"] as? Long)
            } else {
                // Try JSON parsing
                val json = Gson().toJson(rawData)
                val map = Gson().fromJson(json, Map::class.java)
                (map["id"] as? Double)?.toLong()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun resetCheckInSuccess() {
        _uiState.update { it.copy(checkInSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refreshAttendanceStatus() {
        viewModelScope.launch {
            val statusResource = repository.getAttendanceStatus()
            if (statusResource is Resource.Success<AttendanceStatus>) {
                val data = statusResource.data
                _uiState.update { state -> state.copy(attendanceStatus = data.status) }
            }
        }
    }
}
