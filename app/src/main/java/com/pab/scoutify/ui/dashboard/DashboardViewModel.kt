package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.DashboardRepository
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.DashboardData
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    private val _dashboardState = MutableLiveData<Resource<BaseResponse<DashboardData>>>()
    val dashboardState: LiveData<Resource<BaseResponse<DashboardData>>> get() = _dashboardState

    private val _kegiatanState = MutableLiveData<Resource<BaseResponse<List<Kegiatan>>>>()
    val kegiatanState: LiveData<Resource<BaseResponse<List<Kegiatan>>>> get() = _kegiatanState

    fun fetchDashboardData() {
        _dashboardState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getDashboard()
            _dashboardState.value = result
        }
    }

    fun fetchKegiatanData() {
        _kegiatanState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getKegiatan()
            _kegiatanState.value = result
        }
    }

    private val _createKegiatanState = MutableLiveData<Resource<BaseResponse<Kegiatan>>>()
    val createKegiatanState: LiveData<Resource<BaseResponse<Kegiatan>>> get() = _createKegiatanState

    fun createKegiatan(kegiatan: Kegiatan) {
        _createKegiatanState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.createKegiatan(kegiatan)
            _createKegiatanState.value = result
        }
    }

    private val _checkInState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val checkInState: LiveData<Resource<BaseResponse<Any>>> get() = _checkInState

    fun checkIn(request: com.pab.scoutify.model.request.AbsensiRequest) {
        _checkInState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.checkIn(request)
            _checkInState.value = result
        }
    }

    private val _anggotasState = MutableLiveData<Resource<BaseResponse<List<com.pab.scoutify.model.Anggota>>>>()
    val anggotasState: LiveData<Resource<BaseResponse<List<com.pab.scoutify.model.Anggota>>>> get() = _anggotasState

    fun fetchAnggotas() {
        _anggotasState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getAnggotas()
            _anggotasState.value = result
        }
    }
}
