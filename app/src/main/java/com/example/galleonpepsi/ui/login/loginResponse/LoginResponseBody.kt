package com.example.galleonpepsi.ui.login.loginResponse

data class LoginResponseBody(
    val message: String,
    val success: Boolean,
    val userData: UserData
)