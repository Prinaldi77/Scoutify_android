package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.AttendanceRepository
import com.pab.scoutify.model.AttendanceUiState
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.CheckInRequest
import com.pab.scoutify.model.UserLocation
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.utils.GeofenceHelper
import com.pab.scoutify.utils.LocationHelper
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    private val _checkOutState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val checkOutState: LiveData<Resource<BaseResponse<Any>>> = _checkOutState

    private val _todayState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val todayState: LiveData<Resource<BaseResponse<Any>>> = _todayState

    private val _permitState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val permitState: LiveData<Resource<BaseResponse<Any>>> = _permitState

    init {
        loadInitialData()
        startLocationUpdates()
        fetchTodayAttendance()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val activityResource = repository.getCurrentActivity()
            val statusResource = repository.getAttendanceStatus()

            if (activityResource is Resource.Success) {
                _uiState.update { it.copy(activeActivity = activityResource.data) }
            }
            
            if (statusResource is Resource.Success) {
                _uiState.update { it.copy(attendanceStatus = statusResource.data.status) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun startLocationUpdates() {
        locationHelper.getLocationUpdates(5000L)
            .onEach { location ->
                val userLoc = UserLocation(location.latitude, location.longitude, location.accuracy)
                _uiState.update { it.copy(userLocation = userLoc) }
                validateRadius(userLoc)
            }
            .launchIn(viewModelScope)
    }

    private fun validateRadius(userLocation: UserLocation) {
        val activity = _uiState.value.activeActivity ?: return
        
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

    fun checkIn(
        latitude: RequestBody,
        longitude: RequestBody,
        accuracy: RequestBody,
        kegiatanId: RequestBody,
        selfie: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _checkInState.value = Resource.Loading
            val result = repository.checkIn(latitude, longitude, accuracy, kegiatanId, selfie)
            _checkInState.value = result
            if (result is Resource.Success) {
                _uiState.update { it.copy(checkInSuccess = true) }
                refreshAttendanceStatus()
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
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
            _checkInState.value = Resource.Loading
            
            val mediaType = "text/plain".toMediaTypeOrNull()
            val latBody = userLocation.latitude.toString().toRequestBody(mediaType)
            val lngBody = userLocation.longitude.toString().toRequestBody(mediaType)
            val accBody = userLocation.accuracy.toString().toRequestBody(mediaType)
            val kegiatanIdBody = activity.id.toString().toRequestBody(mediaType)
            
            val result = repository.checkIn(latBody, lngBody, accBody, kegiatanIdBody, null)
            _checkInState.value = result
            if (result is Resource.Success) {
                _uiState.update { it.copy(checkInSuccess = true) }
                refreshAttendanceStatus()
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun checkOut(request: CheckOutRequest) {
        viewModelScope.launch {
            _checkOutState.value = Resource.Loading
            _checkOutState.value = repository.checkOut(request)
        }
    }

    fun submitPermit(
        kegiatanId: RequestBody,
        reason: RequestBody,
        type: RequestBody,
        document: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _permitState.value = Resource.Loading
            _permitState.value = repository.submitPermit(kegiatanId, reason, type, document)
        }
    }

    fun fetchTodayAttendance() {
        viewModelScope.launch {
            _todayState.value = Resource.Loading
            _todayState.value = repository.getTodayAttendance()
        }
    }

    fun refreshAttendanceStatus() {
        viewModelScope.launch {
            val statusResource = repository.getAttendanceStatus()
            if (statusResource is Resource.Success) {
                _uiState.update { it.copy(attendanceStatus = statusResource.data.status) }
            }
        }
    }
}
