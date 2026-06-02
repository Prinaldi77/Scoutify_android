package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.MemberRepository
import com.pab.scoutify.model.Anggota
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagementUiState(
    val isLoading: Boolean = false,
    val members: List<Anggota> = emptyList(),
    val filteredMembers: List<Anggota> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "All Members",
    val stats: MemberStats = MemberStats()
)

data class MemberStats(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
    val newRequests: Int = 0
)

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val repository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagementUiState())
    val uiState: StateFlow<ManagementUiState> = _uiState.asStateFlow()

    init {
        getMembers()
    }

    fun getMembers() {
        viewModelScope.launch {
            repository.getMembers().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val members = resource.data
                        val stats = calculateStats(members)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                members = members,
                                filteredMembers = members,
                                stats = stats,
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
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = currentState.members.filter { member ->
            val matchesSearch = member.nama?.contains(currentState.searchQuery, ignoreCase = true) == true ||
                    member.nisn?.contains(currentState.searchQuery, ignoreCase = true) == true
            
            val matchesFilter = if (currentState.selectedFilter == "All Members") {
                true
            } else {
                member.regu?.equals(currentState.selectedFilter, ignoreCase = true) == true
            }
            
            matchesSearch && matchesFilter
        }
        _uiState.update { it.copy(filteredMembers = filtered) }
    }

    private fun calculateStats(members: List<Anggota>): MemberStats {
        return MemberStats(
            total = members.size,
            active = members.count { it.status?.equals("Aktif", ignoreCase = true) == true },
            inactive = members.count { it.status?.equals("Non-aktif", ignoreCase = true) == true },
            newRequests = 12 // Dummy value as per design, or could be filtered from members if API provides it
        )
    }
}
