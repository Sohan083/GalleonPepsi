package com.example.galleonpepsi.presentation.view.login.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.api.LoginApiInterface
import com.example.galleonpepsi.data.login.LoginResponseBody
import com.example.galleonpepsi.data.login.SessionData
import com.example.galleonpepsi.domain.reposotories.UserRepository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _userData = MutableLiveData<SessionData>()
    val userData: LiveData<SessionData> = _userData

    private val userRepository = UserRepository(application)

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean>
        get() = _isLoggedIn

    init {
        checkIfLoggedIn()
    }

    fun updateSessionData() {
        val sessionData = userRepository.getUserSession()
        Log.d("ss",sessionData.toString())
        _userData.postValue(sessionData)
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        val sessionData = userRepository.login(username, password)
        if (sessionData != null) {
            // Update the LiveData
            _userData.postValue(sessionData)
            _isLoggedIn.postValue(true)
        } else {
            _isLoggedIn.postValue(false)
        }
    }

    fun updateSession(loginResponseBody: LoginResponseBody)
    {
        if(loginResponseBody.success)
        {
            val sessionData = loginResponseBody.sessionData
            _userData.postValue(sessionData)
            _isLoggedIn.postValue(true)
            userRepository.saveUserSession(sessionData)
        }
        else
        {
            _isLoggedIn.postValue(false)
        }
    }

    private fun checkIfLoggedIn() {
        _isLoggedIn.value = userRepository.isLoggedIn()
    }

    fun logout() {
        userRepository.clearSessionData()
        _isLoggedIn.value = false
    }
}
