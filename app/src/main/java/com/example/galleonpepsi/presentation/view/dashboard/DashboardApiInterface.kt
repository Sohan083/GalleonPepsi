package com.example.galleonpepsi.ui.dashboard

import com.example.galleonpepsi.ui.dashboard.response.DashboardResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DashboardApiInterface {
    @FormUrlEncoded
    @POST("Android/get_dashboard_mobile.php")
    fun getDashboardData(
        @Field("EmployeeId") EmployeeId: String
    ): retrofit2.Call<DashboardResponseBody>
}