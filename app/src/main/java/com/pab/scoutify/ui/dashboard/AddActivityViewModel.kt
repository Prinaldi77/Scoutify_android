package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.ActivitiesRepository
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddActivityUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val date: String = "",
    val time: String = "",
    val locationName: String = "",
    val description: String = "",
    val category: String = "Aktif",
    val latitude: Double = -6.200000,
    val longitude: Double = 106.816666,
    val radius: Float = 100f
)

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    private val repository: ActivitiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddActivityUiState())
    val uiState: StateFlow<AddActivityUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) = _uiState.update { it.copy(name = newName) }
    fun onDateChange(newDate: String) = _uiState.update { it.copy(date = newDate) }
    fun onTimeChange(newTime: String) = _uiState.update { it.copy(time = newTime) }
    fun onLocationNameChange(newLocation: String) = _uiState.update { it.copy(locationName = newLocation) }
    fun onDescriptionChange(newDesc: String) = _uiState.update { it.copy(description = newDesc) }
    fun onCategoryChange(newCategory: String) = _uiState.update { it.copy(category = newCategory) }
    fun onLocationChange(lat: Double, lng: Double) = _uiState.update { it.copy(latitude = lat, longitude = lng) }
    fun onRadiusChange(newRadius: Float) = _uiState.update { it.copy(radius = newRadius) }

    fun submitActivity() {
        val state = _uiState.value
        if (state.name.isBlank() || state.date.isBlank() || state.locationName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Harap isi semua field wajib!") }
            return
        }

        viewModelScope.launch {
            val kegiatan = Kegiatan(
                nama = state.name,
                tanggal = state.date,
                waktu = state.time,
                lokasi = state.locationName,
                deskripsi = state.description,
                kategori = state.category,
                latitude = state.latitude,
                longitude = state.longitude,
                radius = state.radius
            )

            repository.createActivity(kegiatan).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { AddActivityUiState() }
    }
}
