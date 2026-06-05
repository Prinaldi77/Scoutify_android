package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.api.DashboardApiService
import com.pab.scoutify.data.repository.ActivitiesRepository
import com.pab.scoutify.data.repository.DashboardRepository
import com.pab.scoutify.data.repository.MemberRepository
import com.pab.scoutify.model.*
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val activitiesRepository: ActivitiesRepository,
    private val memberRepository: MemberRepository,
    private val dashboardApiService: DashboardApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    private val _dashboardState = MutableLiveData<Resource<BaseResponse<DashboardData>>>()
    val dashboardState: LiveData<Resource<BaseResponse<DashboardData>>> = _dashboardState

    private val _kegiatanState = MutableLiveData<Resource<BaseResponse<List<Kegiatan>>>>()
    val kegiatanState: LiveData<Resource<BaseResponse<List<Kegiatan>>>> = _kegiatanState

    private val _createKegiatanState = MutableLiveData<Resource<BaseResponse<Kegiatan>>>()
    val createKegiatanState: LiveData<Resource<BaseResponse<Kegiatan>>> = _createKegiatanState

    private val _anggotasState = MutableLiveData<Resource<BaseResponse<List<Anggota>>>>()
    val anggotasState: LiveData<Resource<BaseResponse<List<Anggota>>>> = _anggotasState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val profileJob = launch {
                try {
                    val profile = repository.getProfile()
                    _uiState.update { it.copy(userProfile = profile) }
                } catch (e: Exception) {
                    // Handle individually if needed
                }
            }

            val summaryJob = launch {
                try {
                    val summary = repository.getDashboardSummary()
                    _uiState.update { it.copy(summary = summary) }
                } catch (e: Exception) {
                    // Handle individually if needed
                }
            }

            val upcomingJob = launch {
                try {
                    val upcoming = repository.getUpcomingActivities()
                    _uiState.update { it.copy(upcomingActivities = upcoming) }
                } catch (e: Exception) {
                    // Handle individually if needed
                }
            }

            val notificationsJob = launch {
                try {
                    val notifications = repository.getLatestNotifications()
                    _uiState.update { it.copy(latestNotifications = notifications) }
                } catch (e: Exception) {
                    // Handle individually if needed
                }
            }

            kotlinx.coroutines.joinAll(profileJob, summaryJob, upcomingJob, notificationsJob)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = Resource.Loading
            try {
                // We use DashboardApiService or similar here to get the DashboardData
                // Looking at ApiService.kt, it has getDashboard()
                // But DashboardViewModel uses DashboardRepository which uses DashboardApiService
                // Let's assume there's a way to get DashboardData. 
                // In ApiService.kt: @GET("dashboard") suspend fun getDashboard(): Response<BaseResponse<DashboardData>>
                
                // For now, let's just use a dummy or try to find where getDashboard is
                // Actually, I'll update DashboardRepository to include getDashboard if it's missing.
                val result = repository.getDashboard()
                _dashboardState.value = result
            } catch (e: Exception) {
                _dashboardState.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun fetchKegiatanData() {
        viewModelScope.launch {
            activitiesRepository.getActivities().collect { resource ->
                _kegiatanState.value = resource
            }
        }
    }

    fun createKegiatan(kegiatan: Kegiatan) {
        viewModelScope.launch {
            activitiesRepository.createActivity(kegiatan).collect { resource ->
                _createKegiatanState.value = resource
            }
        }
    }

    fun fetchAnggotas() {
        viewModelScope.launch {
            memberRepository.getMembers().collect { resource ->
                 when (resource) {
                    is Resource.Loading -> _anggotasState.value = Resource.Loading
                    is Resource.Success -> _anggotasState.value = Resource.Success(BaseResponse(true, "Success", resource.data))
                    is Resource.Error -> _anggotasState.value = Resource.Error(resource.message)
                }
            }
        }
    }
}
