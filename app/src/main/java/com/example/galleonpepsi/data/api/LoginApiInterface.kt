package com.example.galleonpepsi.data.api

import com.example.galleonpepsi.data.login.LoginResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginApiInterface {
    @FormUrlEncoded
    @POST("login/login.php")
    suspend fun login(
       @Field("LoginId") id: String,
       @Field("LoginPassword") pass: String
   ): Response<LoginResponseBody>

}