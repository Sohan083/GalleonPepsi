package com.example.galleonpepsi.ui.attendance

import com.example.galleonpepsi.ui.attendance.attendanceresponse.AttendanceStatusResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AttendanceApiInterface {
    @FormUrlEncoded
    @POST("Android/get_attendance.php")
    fun getAttendanceStatus(
        @Field("UserId") UserId: String,
        @Field("EmployeeId") EmployeeId: String,
        @Field("AttendanceType") AttendanceType: String
    ): retrofit2.Call<AttendanceStatusResponseBody>
}