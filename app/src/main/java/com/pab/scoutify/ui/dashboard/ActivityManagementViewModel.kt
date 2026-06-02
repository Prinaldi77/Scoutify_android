package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.ActivityManagementRepository
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityManagementUiState(
    val isLoading: Boolean = false,
    val activities: List<Kegiatan> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: String = "Aktif",
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    init {
        loadActivities("Aktif")
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
        loadActivities(tab)
    }

    fun loadActivities(status: String) {
        viewModelScope.launch {
            repository.getManagementActivities(status).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, activities = resource.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                }
            }
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteActivity(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(deleteSuccess = true) }
                    loadActivities(_uiState.value.selectedTab)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }
    
    fun resetDeleteState() {
        _uiState.update { it.copy(deleteSuccess = false) }
    }
}
