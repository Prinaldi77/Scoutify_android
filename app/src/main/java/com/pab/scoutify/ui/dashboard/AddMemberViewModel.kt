package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.MemberRepository
import com.pab.scoutify.model.Anggota
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMemberUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val nisn: String = "",
    val rank: String = "ANGGOTA",
    val regu: String = "",
    val status: String = "Aktif"
)

@HiltViewModel
class AddMemberViewModel @Inject constructor(
    private val repository: MemberRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val memberId: Int? = savedStateHandle.get<String>("memberId")?.toIntOrNull()
        ?: savedStateHandle.get<Int>("memberId")

    val isEditMode = memberId != null

    private val _uiState = MutableStateFlow(AddMemberUiState())
    val uiState: StateFlow<AddMemberUiState> = _uiState.asStateFlow()

    init {
        memberId?.let { id ->
            loadMemberDetail(id)
        }
    }

    private fun loadMemberDetail(id: Int) {
        viewModelScope.launch {
            repository.getMemberDetail(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        val member = resource.data
                        _uiState.update { it.copy(
                            isLoading = false,
                            name = member.name ?: "",
                            nisn = member.nisn ?: "",
                            rank = member.rank ?: "ANGGOTA",
                            regu = member.regu ?: "",
                            status = member.status ?: "Aktif"
                        ) }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    fun onNameChange(newName: String) = _uiState.update { it.copy(name = newName) }
    fun onNisnChange(newNisn: String) = _uiState.update { it.copy(nisn = newNisn) }
    fun onRankChange(newRank: String) = _uiState.update { it.copy(rank = newRank) }
    fun onReguChange(newRegu: String) = _uiState.update { it.copy(regu = newRegu) }
    fun onStatusChange(newStatus: String) = _uiState.update { it.copy(status = newStatus) }

    fun submitMember() {
        val state = _uiState.value
        if (state.name.isBlank() || state.nisn.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama dan NISN wajib diisi!") }
            return
        }

        viewModelScope.launch {
            val anggota = Anggota(
                id = memberId ?: 0,
                nama = state.name,
                nisn = state.nisn,
                jabatan = state.rank,
                regu = state.regu,
                status = state.status,
                kelas = null,
                angkatan = null,
                fotoUrl = null
            )

            val flow = if (memberId != null) {
                repository.updateMember(memberId, anggota)
            } else {
                repository.createMember(anggota)
            }

            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }
}
