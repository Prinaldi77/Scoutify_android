package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.ReportRepository
import com.pab.scoutify.model.ReportUiState
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceReportViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    fun loadReportData() {
        val dateRange = _uiState.value.dateRange
        val troop = _uiState.value.selectedTroop

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Combine multiple flows or just call them sequentially
            combine(
                repository.getAttendanceSummary(dateRange, troop),
                repository.getReportActivities(dateRange, troop),
                repository.getTopScouts()
            ) { summary, activities, scouts ->
                Triple(summary, activities, scouts)
            }.collect { (summaryRes, activitiesRes, scoutsRes) ->
                _uiState.update { state ->
                    var newState = state.copy(isLoading = false)
                    
                    if (summaryRes is Resource.Success) {
                        newState = newState.copy(summary = summaryRes.data)
                    }
                    if (activitiesRes is Resource.Success) {
                        newState = newState.copy(recentActivities = activitiesRes.data)
                    }
                    if (scoutsRes is Resource.Success) {
                        newState = newState.copy(topScouts = scoutsRes.data)
                    }
                    
                    if (summaryRes is Resource.Error) {
                        newState = newState.copy(errorMessage = summaryRes.message)
                    } else if (activitiesRes is Resource.Error) {
                        newState = newState.copy(errorMessage = activitiesRes.message)
                    }

                    newState
                }
            }
        }
    }

    fun onDateRangeChange(newRange: String) {
        _uiState.update { it.copy(dateRange = newRange) }
        loadReportData()
    }

    fun onTroopChange(newTroop: String) {
        _uiState.update { it.copy(selectedTroop = newTroop) }
        loadReportData()
    }

    fun exportPdf() {
        // Implementation for PDF export
    }
}
