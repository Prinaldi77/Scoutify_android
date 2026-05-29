package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.AttendanceRepository
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AttendanceViewModel : ViewModel() {

    private val repository = AttendanceRepository()

    private val _checkInState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val checkInState: LiveData<Resource<BaseResponse<Any>>> get() = _checkInState

    private val _checkOutState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val checkOutState: LiveData<Resource<BaseResponse<Any>>> get() = _checkOutState

    private val _todayState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val todayState: LiveData<Resource<BaseResponse<Any>>> get() = _todayState

    private val _historyState = MutableLiveData<Resource<BaseResponse<List<Any>>>>()
    val historyState: LiveData<Resource<BaseResponse<List<Any>>>> get() = _historyState

    private val _permitState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val permitState: LiveData<Resource<BaseResponse<Any>>> get() = _permitState

    fun checkIn(
        latitude: RequestBody,
        longitude: RequestBody,
        accuracy: RequestBody,
        kegiatanId: RequestBody,
        selfie: MultipartBody.Part?
    ) {
        _checkInState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.checkIn(latitude, longitude, accuracy, kegiatanId, selfie)
            _checkInState.value = result
        }
    }

    fun checkOut(request: CheckOutRequest) {
        _checkOutState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.checkOut(request)
            _checkOutState.value = result
        }
    }

    fun fetchTodayAttendance() {
        _todayState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getTodayAttendance()
            _todayState.value = result
        }
    }

    fun fetchAttendanceHistory() {
        _historyState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getAttendanceHistory()
            _historyState.value = result
        }
    }

    fun submitPermit(
        kegiatanId: RequestBody,
        reason: RequestBody,
        type: RequestBody,
        document: MultipartBody.Part?
    ) {
        _permitState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.submitPermit(kegiatanId, reason, type, document)
            _permitState.value = result
        }
    }
}
