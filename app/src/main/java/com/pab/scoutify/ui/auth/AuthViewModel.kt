package com.pab.scoutify.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pab.scoutify.data.repository.AuthRepository
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.LoginResponse
import com.pab.scoutify.model.request.LoginRequest
import com.pab.scoutify.model.request.RegisterRequest
import com.pab.scoutify.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<Resource<LoginResponse>>()
    val loginState: LiveData<Resource<LoginResponse>> get() = _loginState

    private val _registerState = MutableLiveData<Resource<BaseResponse<Any>>>()
    val registerState: LiveData<Resource<BaseResponse<Any>>> get() = _registerState

    fun login(email: String, pass: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(email, pass))
            _loginState.value = result
        }
    }

    fun register(name: String, email: String, noGudep: String, pass: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.register(RegisterRequest(name, email, noGudep, pass))
            _registerState.value = result
        }
    }
}
