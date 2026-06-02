package com.pab.scoutify.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.MemberRepository
import com.pab.scoutify.model.MemberDetailUiState
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val repository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memberId: Int = checkNotNull(savedStateHandle["memberId"])

    private val _uiState = MutableStateFlow(MemberDetailUiState())
    val uiState: StateFlow<MemberDetailUiState> = _uiState.asStateFlow()

    init {
        getMemberDetail()
    }

    fun getMemberDetail() {
        viewModelScope.launch {
            repository.getMemberDetail(memberId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                member = resource.data,
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

    fun resetPassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.resetPassword(memberId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, resetPasswordSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }
    
    fun consumeResetPasswordSuccess() {
        _uiState.update { it.copy(resetPasswordSuccess = false) }
    }
}
