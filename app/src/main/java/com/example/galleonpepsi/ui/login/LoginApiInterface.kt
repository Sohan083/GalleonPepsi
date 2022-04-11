package com.example.galleonpepsi.ui.login

import com.example.galleonpepsi.ui.login.loginResponse.LoginResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginApiInterface {
    @FormUrlEncoded
    @POST("Android/android_login.php")
    fun login(
       @Field("UserId") id: String,
       @Field("UserPassword") pass: String
   ): retrofit2.Call<LoginResponseBody>



}