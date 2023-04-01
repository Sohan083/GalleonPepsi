package com.example.galleonpepsi.data.login

data class LoginResponseBody(
    val message: String,
    val sessionData: SessionData,
    val success: Boolean
)