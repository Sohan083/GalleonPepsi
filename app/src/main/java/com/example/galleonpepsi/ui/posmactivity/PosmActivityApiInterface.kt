package com.example.galleonpepsi.ui.posmactivity

import com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse.OutletListResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PosmActivityApiInterface {
    @FormUrlEncoded
    @POST("Android/get_retailers_by_empId.php")
    fun getOutletList(
        @Field("EmployeeId") EmployeeId: String
    ): retrofit2.Call<OutletListResponseBody>
}