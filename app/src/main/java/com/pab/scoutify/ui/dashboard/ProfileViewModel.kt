package com.pab.scoutify.ui.dashboard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.ProfileRepository
import com.pab.scoutify.model.ChangePasswordRequest
import com.pab.scoutify.model.ProfileUiState
import com.pab.scoutify.model.request.UpdateProfileRequest
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getProfile()) {
                is Resource.Success<*> -> {
                    val data = result.data as com.pab.scoutify.model.ProfileData
                    _uiState.update { it.copy(profile = data, isLoading = false) }
                    // Update local session
                    data.let {
                        sessionManager.saveUserName(it.name)
                        sessionManager.saveUserEmail(it.email)
                        sessionManager.savePhotoUrl(it.avatar)
                        sessionManager.saveRole(it.role)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, email: String, address: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Corrected to use model.request.UpdateProfileRequest with named parameters
            val request = UpdateProfileRequest(
                fullName = fullName,
                phoneNumber = phoneNumber,
                email = email,
                address = address
            )
            when (val result = repository.updateProfile(request)) {
                is Resource.Success<*> -> {
                    val data = result.data as com.pab.scoutify.model.ProfileData
                    _uiState.update { it.copy(profile = data, updateSuccess = true, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val file = uriToFile(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            when (val result = repository.uploadPhoto(body)) {
                is Resource.Success<*> -> {
                    val data = result.data as com.pab.scoutify.model.ProfileData
                    _uiState.update { it.copy(profile = data, isLoading = false) }
                    sessionManager.savePhotoUrl(data.avatar)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun changePassword(request: ChangePasswordRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.changePassword(request)) {
                is Resource.Success<*> -> {
                    _uiState.update { it.copy(updateSuccess = true, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (repository.logout()) {
                is Resource.Success<*> -> {
                    sessionManager.logout()
                    _uiState.update { it.copy(logoutSuccess = true, isLoading = false) }
                }
                is Resource.Error -> {
                    // Even if server logout fails, we clear local session
                    sessionManager.logout()
                    _uiState.update { it.copy(logoutSuccess = true, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "profile_temp.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    fun resetState() {
        _uiState.update { it.copy(updateSuccess = false, errorMessage = null) }
    }
}
