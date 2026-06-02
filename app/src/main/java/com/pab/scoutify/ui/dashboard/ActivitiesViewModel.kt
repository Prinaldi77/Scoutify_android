package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.ActivitiesRepository
import com.pab.scoutify.model.ActivitiesUiState
import com.pab.scoutify.model.ActivityItem
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivitiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    fun loadActivities(category: String = _uiState.value.selectedCategory, search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCategory = category) }
            repository.getActivities(category, search).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val activityItems = resource.data.data.map { kegiatan ->
                            ActivityItem(
                                id = kegiatan.id?.toString() ?: "",
                                title = kegiatan.nama ?: "",
                                date = kegiatan.tanggal ?: "",
                                location = kegiatan.lokasi ?: "",
                                imageUrl = "", // Default empty
                                status = kegiatan.kategori ?: "Aktif",
                                type = "A",
                                category = kegiatan.kategori ?: "Aktif"
                            )
                        }
                        _uiState.update { 
                            it.copy(
                                activities = activityItems, 
                                isLoading = false, 
                                errorMessage = null 
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = resource.message
                            ) 
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        loadActivities(category = category)
    }

    fun onSearch(query: String) {
        loadActivities(search = query)
    }
}
